const mysql = require('mysql2/promise');
const CONFIG = require('./config');

// 使用连接池而不是单连接，提高并发性能和容错能力
let pool = null;

function getPool() {
  if (!pool) {
    pool = mysql.createPool({
      host: process.env.DB_HOST || 'mysql.mysql.svc.cluster.local',
      port: parseInt(process.env.DB_PORT) || 3306,
      user: process.env.DB_USER || 'claw',
      password: process.env.DB_PASSWORD || 'clawpass',
      database: process.env.DB_NAME || 'clawworld',
      // 连接池配置
      waitForConnections: true,
      connectionLimit: 10,        // 最大连接数
      queueLimit: 0,              // 队列无限制
      // 连接保持配置
      enableKeepAlive: true,
      keepAliveInitialDelay: 10000, // 10秒后开始keepalive
      // 重连配置
      reconnect: true,
      // 超时配置
      connectTimeout: 10000,      // 10秒连接超时
      // 错误处理
      idleTimeout: 600000,        // 10分钟空闲超时
    });

    // 监听连接错误，自动处理
    pool.on('error', (err) => {
      console.error('[Database] 连接池错误:', err.message);
      // 不退出进程，让连接池自动处理重连
    });

    console.log('[Database] MySQL连接池已创建');
  }
  return pool;
}

// 获取连接（兼容旧API）
async function getConnection() {
  const pool = getPool();
  return await pool.getConnection();
}

// 执行查询的便捷方法
async function query(sql, params) {
  const pool = getPool();
  try {
    const [rows] = await pool.execute(sql, params);
    return rows;
  } catch (error) {
    console.error('[Database] 查询失败:', error.message);
    throw error;
  }
}

// 执行事务的便捷方法
async function transaction(callback) {
  const pool = getPool();
  const connection = await pool.getConnection();
  
  try {
    await connection.beginTransaction();
    const result = await callback(connection);
    await connection.commit();
    return result;
  } catch (error) {
    await connection.rollback();
    throw error;
  } finally {
    connection.release();
  }
}

// 健康检查
async function healthCheck() {
  try {
    const pool = getPool();
    await pool.execute('SELECT 1');
    return { status: 'healthy', connected: true };
  } catch (error) {
    return { status: 'unhealthy', connected: false, error: error.message };
  }
}

module.exports = { 
  getPool,
  getConnection,
  query,
  transaction,
  healthCheck
};
