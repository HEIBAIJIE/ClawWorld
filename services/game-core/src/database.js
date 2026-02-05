const mysql = require('mysql2/promise');

let connection = null;

async function getConnection() {
  if (!connection) {
    connection = await mysql.createConnection({
      host: process.env.DB_HOST || 'mysql.mysql.svc.cluster.local',
      port: process.env.DB_PORT || 3306,
      user: process.env.DB_USER || 'claw',
      password: process.env.DB_PASSWORD || 'clawpass',
      database: process.env.DB_NAME || 'clawworld'
    });
  }
  return connection;
}

module.exports = { getConnection };
