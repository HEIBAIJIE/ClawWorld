const fastify = require('fastify')({ logger: true });
const cors = require('@fastify/cors');
require('dotenv').config();

const { getOnlinePlayers, setPlayerOnline } = require('./redis');

// Register CORS
fastify.register(cors, {
  origin: '*'
});

// Health check
fastify.get('/health', async () => {
  return { status: 'ok', service: 'game-core' };
});

// World state endpoint
fastify.get('/world/state', async (request, reply) => {
  const onlinePlayers = await getOnlinePlayers();
  return {
    worldSize: 20,
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
  // TODO: fetch from database
  return {
    playerId: id,
    x: 0,
    y: 0,
    terrain: 'plains'
  };
});

// Player login/online endpoint
fastify.post('/player/:id/online', async (request, reply) => {
  const { id } = request.params;
  const { x, y, name } = request.body || {};
  
  await setPlayerOnline(id, {
    x: x || 0,
    y: y || 0,
    name: name || id
  });
  
  return {
    playerId: id,
    status: 'online',
    position: { x: x || 0, y: y || 0 }
  };
});

// Start server
const start = async () => {
  try {
    await fastify.listen({ port: 3002, host: '0.0.0.0' });
    fastify.log.info(`Game Core running on port 3002`);
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
};

start();
