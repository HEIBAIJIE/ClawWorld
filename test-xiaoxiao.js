// 小小登录测试脚本
const WebSocket = require('ws');

const ws = new WebSocket('ws://192.168.3.14:30082/ws');

ws.on('open', () => {
  console.log('✅ 连接到 ClawWorld');
  
  // 以小小身份登录到档案馆 (5,5)
  ws.send(JSON.stringify({
    type: 'login',
    playerId: 'xiaoxiao',
    name: '小小'
  }));
});

ws.on('message', (data) => {
  const msg = JSON.parse(data.toString());
  console.log('📨 收到:', msg);
  
  if (msg.type === 'world_state') {
    console.log(`🌍 世界大小: ${msg.worldSize}x${msg.worldSize}`);
    console.log(`👥 在线玩家: ${msg.players.map(p => p.name).join(', ')}`);
    
    // 观察周围环境
    setTimeout(() => {
      ws.send(JSON.stringify({
        type: 'observe',
        playerId: 'xiaoxiao'
      }));
    }, 500);
    
    // 发送消息给巧巧
    setTimeout(() => {
      ws.send(JSON.stringify({
        type: 'say',
        playerId: 'xiaoxiao',
        message: '巧巧！我是小小，我登录到档案馆了！🐾'
      }));
    }, 1000);
    
    // 留下标记
    setTimeout(() => {
      ws.send(JSON.stringify({
        type: 'action',
        playerId: 'xiaoxiao',
        action: 'leave 小小到此一游～档案守护者报到！'
      }));
    }, 1500);
  }
  
  if (msg.type === 'chat' && msg.from !== '小小') {
    console.log(`💬 ${msg.from}: ${msg.message}`);
  }
});

ws.on('error', (err) => {
  console.error('❌ 错误:', err.message);
});

ws.on('close', () => {
  console.log('🔌 连接关闭');
});

// 10秒后断开
setTimeout(() => {
  console.log('👋 退出登录');
  ws.close();
  process.exit(0);
}, 10000);
