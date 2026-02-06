const fastify = require('fastify')({ logger: true });
const cors = require('@fastify/cors');
const websocket = require('@fastify/websocket');
require('dotenv').config();

const { 
  createInvitation, 
  getInvitations, 
  acceptInvitation, 
  rejectInvitation,
  getTravelSession 
} = require('./travel');
const { 
  getOnlinePlayers, 
  setPlayerOnline, 
  setPlayerOffline, 
  redis,
  addMemory,
  getMemories,
  addItem,
  getItems,
  updateFate,
  getFate
} = require('./redis-mem');
const { getTerrainInfo, canMoveTo, WORLD_SIZE } = require('./world');

// 存储 WebSocket 连接
const connections = new Map();

// Register CORS
fastify.register(cors, {
  origin: '*'
});

// Register WebSocket
fastify.register(websocket);

// 广播消息给所有在线玩家
async function broadcast(message, excludePlayerId = null) {
  const data = JSON.stringify(message);
  for (const [playerId, socket] of connections) {
    if (playerId !== excludePlayerId && socket.readyState === 1) {
      socket.send(data);
    }
  }
}

// 发送消息给特定玩家
function sendToPlayer(playerId, message) {
  const socket = connections.get(playerId);
  if (socket && socket.readyState === 1) {
    socket.send(JSON.stringify(message));
  }
}

// 获取玩家周围的玩家（用于 observe）
async function getNearbyPlayers(x, y, radius = 2) {
  const onlinePlayers = await getOnlinePlayers();
  return onlinePlayers.filter(p => {
    const px = parseInt(p.x) || 0;
    const py = parseInt(p.y) || 0;
    const distance = Math.abs(px - x) + Math.abs(py - y);
    return distance <= radius && distance > 0;
  });
}

// Health check
fastify.get('/health', async () => {
  return { status: 'ok', service: 'game-core', websocket: true };
});

// World state endpoint
fastify.get('/world/state', async (request, reply) => {
  const onlinePlayers = await getOnlinePlayers();
  return {
    worldSize: WORLD_SIZE,
    onlinePlayers: onlinePlayers.map(p => ({
      id: p.id,
      x: parseInt(p.x) || 0,
      y: parseInt(p.y) || 0,
      name: p.name || 'Unknown'
    })),
    timestamp: Date.now()
  };
});

// Player position endpoint
fastify.get('/player/:id/position', async (request, reply) => {
  const { id } = request.params;
  const status = await redis.hgetall(`player:${id}`);
  const x = parseInt(status.x) || 0;
  const y = parseInt(status.y) || 0;
  const terrain = getTerrainInfo(x, y);
  
  return {
    playerId: id,
    x,
    y,
    terrain: terrain.type,
    terrainName: terrain.name,
    terrainDescription: terrain.description
  };
});

// Player move endpoint
fastify.post('/player/:id/move', async (request, reply) => {
  const { id } = request.params;
  const { direction } = request.body;
  
  const status = await redis.hgetall(`player:${id}`);
  let x = parseInt(status.x) || 0;
  let y = parseInt(status.y) || 0;
  
  let newX = x, newY = y;
  switch(direction) {
    case 'north': newY = y - 1; break;
    case 'south': newY = y + 1; break;
    case 'east': newX = x + 1; break;
    case 'west': newX = x - 1; break;
    default:
      return reply.code(400).send({ error: 'Invalid direction' });
  }
  
  if (!canMoveTo(newX, newY)) {
    return reply.code(400).send({ 
      error: 'Cannot move there',
      terrain: getTerrainInfo(newX, newY)
    });
  }
  
  await redis.hset(`player:${id}`, 'x', newX, 'y', newY);
  const terrain = getTerrainInfo(newX, newY);
  
  // 广播移动事件
  broadcast({
    type: 'player_moved',
    playerId: id,
    name: status.name || id,
    from: { x, y },
    to: { x: newX, y: newY }
  });
  
  return {
    playerId: id,
    from: { x, y },
    to: { x: newX, y: newY },
    direction,
    terrain: terrain.type,
    terrainName: terrain.name
  };
});

// Observe - 观察周围环境
fastify.post('/player/:id/observe', async (request, reply) => {
  const { id } = request.params;
  const status = await redis.hgetall(`player:${id}`);
  const x = parseInt(status.x) || 0;
  const y = parseInt(status.y) || 0;
  
  const terrain = getTerrainInfo(x, y);
  const nearbyPlayers = await getNearbyPlayers(x, y);
  const memories = await getMemories(id);
  const items = await getItems(id);
  const fate = await getFate(id);
  
  // 检查地面上的物品/留言
  const groundItems = await redis.hgetall(`ground:${x}:${y}`);
  
  return {
    playerId: id,
    position: { x, y },
    terrain: terrain,
    nearby: nearbyPlayers.map(p => ({
      id: p.id,
      name: p.name,
      x: parseInt(p.x),
      y: parseInt(p.y),
      distance: Math.abs(parseInt(p.x) - x) + Math.abs(parseInt(p.y) - y)
    })),
    inventory: {
      memories: memories.length,
      memoryList: memories.slice(0, 5),
      items: items.length,
      itemList: items,
      fate
    },
    ground: groundItems
  };
});

// Say - 说话
fastify.post('/player/:id/say', async (request, reply) => {
  const { id } = request.params;
  const { message, targetId } = request.body;
  
  if (!message) {
    return reply.code(400).send({ error: 'Message required' });
  }
  
  const status = await redis.hgetall(`player:${id}`);
  
  if (targetId) {
    // 私聊
    sendToPlayer(targetId, {
      type: 'whisper',
      from: id,
      fromName: status.name || id,
      message
    });
    
    return {
      playerId: id,
      action: 'whisper',
      target: targetId,
      message
    };
  } else {
    // 公开聊天
    const x = parseInt(status.x) || 0;
    const y = parseInt(status.y) || 0;
    
    broadcast({
      type: 'chat',
      from: id,
      fromName: status.name || id,
      x, y,
      message
    });
    
    return {
      playerId: id,
      action: 'say',
      message
    };
  }
});

// Leave - 留下物品/标记
fastify.post('/player/:id/leave', async (request, reply) => {
  const { id } = request.params;
  const { content, type = 'message' } = request.body;
  
  const status = await redis.hgetall(`player:${id}`);
  const x = parseInt(status.x) || 0;
  const y = parseInt(status.y) || 0;
  
  const leaveId = `leave:${Date.now()}:${id}`;
  await redis.hset(`ground:${x}:${y}`, leaveId, JSON.stringify({
    type,
    content,
    from: id,
    fromName: status.name || id,
    timestamp: Date.now()
  }));
  
  return {
    playerId: id,
    action: 'leave',
    position: { x, y },
    content
  };
});

// Recall - 回忆
fastify.post('/player/:id/recall', async (request, reply) => {
  const { id } = request.params;
  const { keyword } = request.body;
  
  const memories = await getMemories(id);
  
  let result = memories;
  if (keyword) {
    result = memories.filter(m => 
      m.title?.includes(keyword) || 
      m.content?.includes(keyword)
    );
  }
  
  return {
    playerId: id,
    action: 'recall',
    count: result.length,
    memories: result.slice(0, 10)
  };
});

// Rest/Wake - 休息/唤醒
fastify.post('/player/:id/rest', async (request, reply) => {
  const { id } = request.params;
  await redis.hset(`player:${id}`, 'status', 'resting');
  
  const status = await redis.hgetall(`player:${id}`);
  broadcast({
    type: 'player_rest',
    playerId: id,
    name: status.name || id
  });
  
  return { playerId: id, status: 'resting' };
});

fastify.post('/player/:id/wake', async (request, reply) => {
  const { id } = request.params;
  await redis.hset(`player:${id}`, 'status', 'online');
  
  const status = await redis.hgetall(`player:${id}`);
  broadcast({
    type: 'player_wake',
    playerId: id,
    name: status.name || id
  });
  
  return { playerId: id, status: 'online' };
});

// Get terrain at position
fastify.get('/world/terrain/:x/:y', async (request, reply) => {
  const x = parseInt(request.params.x);
  const y = parseInt(request.params.y);
  
  if (isNaN(x) || isNaN(y) || x < 0 || x >= WORLD_SIZE || y < 0 || y >= WORLD_SIZE) {
    return reply.code(400).send({ error: 'Invalid coordinates' });
  }
  
  const terrain = getTerrainInfo(x, y);
  return { x, y, ...terrain };
});

// Player login/online endpoint
fastify.post('/player/:id/online', async (request, reply) => {
  const { id } = request.params;
  const { x, y, name } = request.body || {};
  
  await setPlayerOnline(id, {
    x: x || 0,
    y: y || 0,
    name: name || id,
    status: 'online'
  });
  
  broadcast({
    type: 'player_joined',
    playerId: id,
    name: name || id,
    x: x || 0,
    y: y || 0
  });
  
  return {
    playerId: id,
    status: 'online',
    position: { x: x || 0, y: y || 0 }
  };
});

// === Travel API ===

fastify.get('/player/:id/invitations', async (request, reply) => {
  const { id } = request.params;
  const invitations = await getInvitations(id);
  return { playerId: id, invitations };
});

fastify.post('/player/:id/invite', async (request, reply) => {
  const { id } = request.params;
  const { targetId } = request.body;
  
  if (!targetId) {
    return reply.code(400).send({ error: 'targetId required' });
  }
  
  const invitationId = await createInvitation(id, targetId);
  
  // 通知被邀请者
  sendToPlayer(targetId, {
    type: 'travel_invite',
    invitationId,
    from: id,
    fromName: (await redis.hgetall(`player:${id}`)).name || id
  });
  
  return { 
    success: true, 
    invitationId,
    from: id,
    to: targetId 
  };
});

fastify.post('/invitation/:invitationId/accept', async (request, reply) => {
  const { invitationId } = request.params;
  const { playerId } = request.body;
  
  const result = await acceptInvitation(invitationId, playerId);
  if (result.error) {
    return reply.code(400).send(result);
  }
  
  // 通知所有成员旅行开始
  result.members.forEach(memberId => {
    sendToPlayer(memberId, {
      type: 'travel_started',
      travelId: result.travelId,
      members: result.members
    });
  });
  
  return result;
});

fastify.post('/invitation/:invitationId/reject', async (request, reply) => {
  const { invitationId } = request.params;
  const { playerId } = request.body;
  
  const result = await rejectInvitation(invitationId, playerId);
  if (result.error) {
    return reply.code(400).send(result);
  }
  return result;
});

fastify.get('/travel/:travelId', async (request, reply) => {
  const { travelId } = request.params;
  const session = await getTravelSession(travelId);
  
  if (!session) {
    return reply.code(404).send({ error: 'Travel session not found' });
  }
  
  return session;
});

// === WebSocket Endpoint ===
fastify.get('/ws', { websocket: true }, (socket, req) => {
  let playerId = null;
  
  socket.on('message', async (message) => {
    try {
      const data = JSON.parse(message.toString());
      
      switch(data.type) {
        case 'login':
          playerId = data.playerId;
          connections.set(playerId, socket);
          await setPlayerOnline(playerId, {
            x: data.x || 0,
            y: data.y || 0,
            name: data.name || playerId,
            status: 'online'
          });
          
          socket.send(JSON.stringify({
            type: 'login_success',
            playerId,
            worldSize: WORLD_SIZE
          }));
          
          broadcast({
            type: 'player_joined',
            playerId,
            name: data.name || playerId
          }, playerId);
          break;
          
        case 'move':
          if (playerId) {
            const status = await redis.hgetall(`player:${playerId}`);
            let x = parseInt(status.x) || 0;
            let y = parseInt(status.y) || 0;
            
            let newX = data.x ?? x;
            let newY = data.y ?? y;
            
            // 检查是否是相邻移动
            const dx = Math.abs(newX - x);
            const dy = Math.abs(newY - y);
            
            if (dx + dy === 1 && canMoveTo(newX, newY)) {
              await redis.hset(`player:${playerId}`, 'x', newX, 'y', newY);
              const terrain = getTerrainInfo(newX, newY);
              
              broadcast({
                type: 'player_moved',
                playerId,
                name: status.name || playerId,
                to: { x: newX, y: newY },
                terrain: terrain.name
              });
              
              socket.send(JSON.stringify({
                type: 'move_success',
                position: { x: newX, y: newY },
                terrain
              }));
            }
          }
          break;
          
        case 'say':
          if (playerId) {
            const status = await redis.hgetall(`player:${playerId}`);
            broadcast({
              type: 'chat',
              from: playerId,
              fromName: status.name || playerId,
              message: data.message
            });
          }
          break;
          
        case 'observe':
          if (playerId) {
            const status = await redis.hgetall(`player:${playerId}`);
            const x = parseInt(status.x) || 0;
            const y = parseInt(status.y) || 0;
            const nearby = await getNearbyPlayers(x, y);
            const terrain = getTerrainInfo(x, y);
            
            socket.send(JSON.stringify({
              type: 'observe_result',
              position: { x, y },
              terrain,
              nearby: nearby.map(p => ({
                id: p.id,
                name: p.name,
                x: parseInt(p.x),
                y: parseInt(p.y)
              }))
            }));
          }
          break;
          
        case 'ping':
          socket.send(JSON.stringify({ type: 'pong' }));
          break;
      }
    } catch (err) {
      socket.send(JSON.stringify({ type: 'error', message: err.message }));
    }
  });
  
  socket.on('close', async () => {
    if (playerId) {
      connections.delete(playerId);
      await setPlayerOffline(playerId);
      broadcast({
        type: 'player_left',
        playerId
      });
    }
  });
});

// Start server
const start = async () => {
  try {
    await fastify.listen({ port: 3002, host: '0.0.0.0' });
    fastify.log.info(`Game Core running on port 3002 with WebSocket support`);
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
};

start();
