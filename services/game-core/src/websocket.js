// WebSocket 管理模块
const { getOnlinePlayers, setPlayerOnline, setPlayerOffline, redis } = require('./redis-mem');
const { getTerrainInfo, canMoveTo, WORLD_SIZE, TERRAIN_MAP } = require('./world');

// 存储所有 WebSocket 连接
const connections = new Map();

// 初始化 WebSocket
function setupWebSocket(fastify) {
  fastify.register(require('@fastify/websocket'));
  
  fastify.get('/ws', { websocket: true }, (connection, req) => {
    let playerId = null;
    
    console.log('🔌 新的 WebSocket 连接');
    
    connection.socket.on('message', async (message) => {
      try {
        const data = JSON.parse(message.toString());
        await handleMessage(connection, data);
      } catch (err) {
        console.error('消息解析错误:', err);
        sendToConnection(connection, { type: 'error', message: 'Invalid message format' });
      }
    });
    
    connection.socket.on('close', async () => {
      console.log(`🔌 连接关闭: ${playerId}`);
      if (playerId) {
        await setPlayerOffline(playerId);
        connections.delete(playerId);
        broadcast({ type: 'player_left', playerId });
      }
    });
    
    // 保存连接引用以便后续使用
    connection._tempId = Date.now();
  });
}

// 处理收到的消息
async function handleMessage(connection, data) {
  console.log('收到消息:', data);
  
  switch(data.type) {
    case 'login':
      await handleLogin(connection, data);
      break;
    case 'move':
      await handleMove(connection, data);
      break;
    case 'say':
      await handleSay(connection, data);
      break;
    case 'observe':
      await handleObserve(connection, data);
      break;
    case 'action':
      await handleAction(connection, data);
      break;
    default:
      sendToConnection(connection, { type: 'error', message: 'Unknown action type' });
  }
}

// 处理登录
async function handleLogin(connection, data) {
  const { playerId, name } = data;
  
  if (!playerId) {
    sendToConnection(connection, { type: 'error', message: 'playerId required' });
    return;
  }
  
  // 保存玩家信息到 Redis
  await setPlayerOnline(playerId, {
    x: 10, // 默认出生点
    y: 10,
    name: name || playerId
  });
  
  // 保存连接
  connection.playerId = playerId;
  connections.set(playerId, connection);
  
  console.log(`✅ 玩家登录: ${name} (${playerId})`);
  
  // 发送世界状态
  const worldState = await getWorldState();
  sendToConnection(connection, { 
    type: 'world_state', 
    ...worldState,
    yourId: playerId 
  });
  
  // 广播玩家加入
  broadcast({ 
    type: 'player_joined', 
    playerId, 
    name: name || playerId,
    x: 10,
    y: 10
  }, playerId); // 排除自己
  
  // 发送欢迎消息
  sendToConnection(connection, {
    type: 'system',
    message: `欢迎来到 ClawWorld，${name || playerId}！当前在线: ${connections.size} 人`
  });
}

// 处理移动
async function handleMove(connection, data) {
  const { playerId, x, y } = data;
  
  if (!playerId || !connections.has(playerId)) {
    sendToConnection(connection, { type: 'error', message: 'Not logged in' });
    return;
  }
  
  // 验证移动是否合法
  const player = await redis.hgetall(`player:${playerId}`);
  const currentX = parseInt(player.x) || 10;
  const currentY = parseInt(player.y) || 10;
  
  // 检查是否相邻
  const dx = Math.abs(x - currentX);
  const dy = Math.abs(y - currentY);
  
  if (dx + dy !== 1) {
    sendToConnection(connection, { 
      type: 'error', 
      message: '只能移动到相邻格子' 
    });
    return;
  }
  
  // 检查地形是否可通行
  if (!canMoveTo(x, y)) {
    const terrain = getTerrainInfo(x, y);
    sendToConnection(connection, { 
      type: 'error', 
      message: `无法进入${terrain.name}` 
    });
    return;
  }
  
  // 更新位置
  await redis.hset(`player:${playerId}`, 'x', x, 'y', y);
  const terrain = getTerrainInfo(x, y);
  
  console.log(`🚶 玩家移动: ${playerId} → (${x}, ${y}) ${terrain.name}`);
  
  // 发送移动结果
  sendToConnection(connection, {
    type: 'move_result',
    success: true,
    from: { x: currentX, y: currentY },
    to: { x, y },
    terrain: terrain
  });
  
  // 广播位置更新
  broadcast({
    type: 'player_moved',
    playerId,
    x,
    y,
    terrain: terrain.type
  });
}

// 处理说话
async function handleSay(connection, data) {
  const { playerId, message } = data;
  
  if (!playerId || !connections.has(playerId)) {
    sendToConnection(connection, { type: 'error', message: 'Not logged in' });
    return;
  }
  
  const player = await redis.hgetall(`player:${playerId}`);
  const name = player.name || playerId;
  
  console.log(`💬 ${name}: ${message}`);
  
  // 广播给所有人
  broadcast({
    type: 'chat',
    from: name,
    playerId,
    message,
    x: parseInt(player.x) || 10,
    y: parseInt(player.y) || 10
  });
}

// 处理观察
async function handleObserve(connection, data) {
  const { playerId } = data;
  
  if (!playerId || !connections.has(playerId)) {
    sendToConnection(connection, { type: 'error', message: 'Not logged in' });
    return;
  }
  
  const player = await redis.hgetall(`player:${playerId}`);
  const x = parseInt(player.x) || 10;
  const y = parseInt(player.y) || 10;
  
  // 获取周围信息
  const surroundings = [];
  const directions = [
    { dx: 0, dy: -1, name: '北' },
    { dx: 1, dy: 0, name: '东' },
    { dx: 0, dy: 1, name: '南' },
    { dx: -1, dy: 0, name: '西' }
  ];
  
  for (const dir of directions) {
    const nx = x + dir.dx;
    const ny = y + dir.dy;
    if (nx >= 0 && nx < WORLD_SIZE && ny >= 0 && ny < WORLD_SIZE) {
      const terrain = getTerrainInfo(nx, ny);
      surroundings.push({
        direction: dir.name,
        x: nx,
        y: ny,
        terrain: terrain.type,
        name: terrain.name,
        passable: canMoveTo(nx, ny)
      });
    }
  }
  
  // 获取附近玩家
  const onlinePlayers = await getOnlinePlayers();
  const nearbyPlayers = onlinePlayers.filter(p => {
    if (p.id === playerId) return false;
    const px = parseInt(p.x) || 0;
    const py = parseInt(p.y) || 0;
    return Math.abs(px - x) <= 2 && Math.abs(py - y) <= 2;
  });
  
  const currentTerrain = getTerrainInfo(x, y);
  
  sendToConnection(connection, {
    type: 'observe_result',
    position: { x, y },
    terrain: currentTerrain,
    surroundings,
    nearbyPlayers: nearbyPlayers.map(p => ({
      id: p.id,
      name: p.name || p.id,
      x: parseInt(p.x) || 0,
      y: parseInt(p.y) || 0
    }))
  });
}

// 处理通用动作
async function handleAction(connection, data) {
  const { playerId, action } = data;
  
  if (!playerId || !connections.has(playerId)) {
    sendToConnection(connection, { type: 'error', message: 'Not logged in' });
    return;
  }
  
  console.log(`🎯 玩家动作: ${playerId} - ${action}`);
  
  // 简单解析动作
  const parts = action.trim().split(/\s+/);
  const command = parts[0].toLowerCase();
  const args = parts.slice(1).join(' ');
  
  switch(command) {
    case 'say':
      await handleSay(connection, { playerId, message: args });
      break;
    case 'observe':
      await handleObserve(connection, { playerId });
      break;
    case 'leave':
      sendToConnection(connection, {
        type: 'action_result',
        action: 'leave',
        result: '你留下了标记: ' + (args || '无内容')
      });
      break;
    case 'recall':
      sendToConnection(connection, {
        type: 'action_result',
        action: 'recall',
        result: '记忆功能开发中...'
      });
      break;
    default:
      sendToConnection(connection, {
        type: 'action_result',
        action: command,
        result: `执行了: ${action}`
      });
  }
}

// 获取世界状态
async function getWorldState() {
  const onlinePlayers = await getOnlinePlayers();
  return {
    worldSize: WORLD_SIZE,
    terrain: TERRAIN_MAP, // 发送完整地形地图
    players: onlinePlayers.map(p => ({
      id: p.id,
      x: parseInt(p.x) || 10,
      y: parseInt(p.y) || 10,
      name: p.name || p.id
    })),
    timestamp: Date.now()
  };
}

// 发送消息给指定连接
function sendToConnection(connection, data) {
  if (connection.socket.readyState === 1) { // OPEN
    connection.socket.send(JSON.stringify(data));
  }
}

// 广播消息给所有连接
function broadcast(data, excludePlayerId = null) {
  const message = JSON.stringify(data);
  connections.forEach((conn, pid) => {
    if (pid !== excludePlayerId && conn.socket.readyState === 1) {
      conn.socket.send(message);
    }
  });
}

// 获取连接数
function getConnectionCount() {
  return connections.size;
}

module.exports = {
  setupWebSocket,
  broadcast,
  getConnectionCount
};
