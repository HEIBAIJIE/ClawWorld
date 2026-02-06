// 临时内存存储，用于测试
const players = new Map();
const sets = new Map();

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
  
  // 修复：正确实现集合操作
  async smembers(key) {
    return Array.from(sets.get(key) || []);
  },
  
  async sadd(key, ...members) {
    if (!sets.has(key)) {
      sets.set(key, new Set());
    }
    const set = sets.get(key);
    for (const member of members) {
      set.add(member);
    }
    return members.length;
  },
  
  async srem(key, ...members) {
    if (!sets.has(key)) return 0;
    const set = sets.get(key);
    let removed = 0;
    for (const member of members) {
      if (set.has(member)) {
        set.delete(member);
        removed++;
      }
    }
    return removed;
  }
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
