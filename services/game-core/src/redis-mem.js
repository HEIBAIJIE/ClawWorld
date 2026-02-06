// 临时内存存储，用于测试
const players = new Map();

const redis = {
  async hset(key, ...args) {
    const data = players.get(key) || {};
    if (typeof args[0] === 'object') {
      Object.assign(data, args[0]);
    } else {
      for (let i = 0; i < args.length; i += 2) {
        data[args[i]] = args[i + 1];
      }
    }
    players.set(key, data);
    return 'OK';
  },
  
  async hgetall(key) {
    return players.get(key) || {};
  },
  
  async keys(pattern) {
    const keys = [];
    for (const key of players.keys()) {
      if (key.includes('player:')) {
        keys.push(key);
      }
    }
    return keys;
  },
  
  async expire() { return 1; },
  async smembers() { return []; },
  async sadd() { return 1; },
  async srem() { return 1; }
};

// Player online status
async function setPlayerOnline(playerId, data) {
  await redis.hset(`player:${playerId}`, {
    ...data,
    online: 'true',
    lastSeen: Date.now()
  });
}

async function setPlayerOffline(playerId) {
  await redis.hset(`player:${playerId}`, 'online', 'false');
}

async function getPlayerStatus(playerId) {
  return await redis.hgetall(`player:${playerId}`);
}

async function getOnlinePlayers() {
  const keys = await redis.keys('player:*');
  const onlinePlayers = [];
  for (const key of keys) {
    const data = await redis.hgetall(key);
    if (data.online === 'true') {
      onlinePlayers.push({
        id: key.replace('player:', ''),
        ...data
      });
    }
  }
  return onlinePlayers;
}

module.exports = {
  redis,
  setPlayerOnline,
  setPlayerOffline,
  getPlayerStatus,
  getOnlinePlayers
};
