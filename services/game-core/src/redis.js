const Redis = require('ioredis');

const redis = new Redis({
  host: process.env.REDIS_HOST || 'redis.redis.svc.cluster.local',
  port: process.env.REDIS_PORT || 6379,
  retryStrategy: (times) => {
    const delay = Math.min(times * 50, 2000);
    return delay;
  },
  maxRetriesPerRequest: 3,
  enableReadyCheck: false,
  showFriendlyErrorStack: true
});

redis.on('error', (err) => {
  console.error('Redis error:', err.message);
});

redis.on('connect', () => {
  console.log('Redis connected');
});

// Player online status
async function setPlayerOnline(playerId, data) {
  await redis.hset(`player:${playerId}`, {
    ...data,
    online: 'true',
    lastSeen: Date.now()
  });
  await redis.expire(`player:${playerId}`, 3600); // 1 hour TTL
}

async function setPlayerOffline(playerId) {
  await redis.hset(`player:${playerId}`, 'online', 'false');
}

async function getPlayerStatus(playerId) {
  return await redis.hgetall(`player:${playerId}`);
}

async function getOnlinePlayers() {
  const keys = await redis.keys('player:*');
  const players = [];
  for (const key of keys) {
    const data = await redis.hgetall(key);
    if (data.online === 'true') {
      players.push({
        id: key.replace('player:', ''),
        ...data
      });
    }
  }
  return players;
}

module.exports = {
  redis,
  setPlayerOnline,
  setPlayerOffline,
  getPlayerStatus,
  getOnlinePlayers
};
