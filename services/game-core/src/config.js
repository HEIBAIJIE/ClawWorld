// ClawWorld 配置文件
// 所有环境相关的配置集中管理

const CONFIG = {
  // 服务地址配置
  REFEREE_URL: process.env.REFEREE_URL || 'http://192.168.3.14:3004',
  GAME_CORE_URL: process.env.GAME_CORE_URL || 'http://192.168.3.14:30082',
  WEB_URL: process.env.WEB_URL || 'http://192.168.3.14:8080',
  
  // Redis 配置（未来接入真实 Redis 时使用）
  REDIS: {
    HOST: process.env.REDIS_HOST || 'localhost',
    PORT: process.env.REDIS_PORT || 6379,
    PASSWORD: process.env.REDIS_PASSWORD || null,
    DB: process.env.REDIS_DB || 0
  },
  
  // 游戏系统配置
  GAME: {
    WORLD_SIZE: 20,
    MAX_PARTY_SIZE: 4,           // 旅行队伍最大人数
    TRAVEL_ROUNDS_MIN: 3,        // 旅行最少回合数
    TRAVEL_ROUNDS_MAX: 5,        // 旅行最大回合数
    INVITATION_TIMEOUT_MS: 30 * 1000,  // 邀请超时时间
    DEFAULT_FATE: 10             // 初始缘分值
  },
  
  // WebSocket 配置
  WS: {
    HEARTBEAT_INTERVAL_MS: 30 * 1000,   // 心跳间隔
    RECONNECT_MAX_ATTEMPTS: 5           // 最大重连次数
  },
  
  // 内存清理配置
  MEMORY: {
    CLEANUP_INTERVAL_MS: 5 * 60 * 1000,  // 每5分钟清理一次
    OFFLINE_TTL_MS: 30 * 60 * 1000       // 离线30分钟后清理数据
  },
  
  // 日志配置
  LOG: {
    LEVEL: process.env.LOG_LEVEL || 'info',
    ENABLE_CONSOLE: true
  }
};

module.exports = CONFIG;
