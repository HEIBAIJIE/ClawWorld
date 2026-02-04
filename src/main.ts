// ClawWorld MVP - 第一个交互原型
// 纯文字界面，命令行交互

import { World, Entity } from './core/world';
import { CommandHandler } from './core/commands';
import * as readline from 'readline';

const world = new World(3, 3);
const commands = new CommandHandler(world);

// 创建实体
const player: Entity = {
  id: 'player-1',
  name: 'Tony',
  symbol: '@',
  x: 1,
  y: 1, // 从广场开始
  type: 'human'
};

const xiaoxiao: Entity = {
  id: 'agent-1',
  name: '小小',
  symbol: '🐾',
  x: 0,
  y: 2, // 档案馆
  type: 'agent'
};

const qiaoqiao: Entity = {
  id: 'agent-2',
  name: '巧巧',
  symbol: '🌸',
  x: 2,
  y: 0, // 边界塔
  type: 'agent'
};

world.addEntity(player);
world.addEntity(xiaoxiao);
world.addEntity(qiaoqiao);

// 欢迎信息
console.log('╔════════════════════════════════════╗');
console.log('║     🐾 欢迎来到 ClawWorld 🌸      ║');
console.log('║                                    ║');
console.log('║   你和智能体共同居住的后数字世界    ║');
console.log('╚════════════════════════════════════╝');
console.log();
console.log('【地图】');
console.log(world.render());
console.log('图例: @ = Tony  🐾 = 小小  🌸 = 巧巧  · = 空地');
console.log();

// 初始观察
const initial = commands.observe(player);
console.log(initial.message);
console.log();
console.log('─────────────────────────────────────');
console.log('可用命令：');
console.log('  move [north|south|east|west] - 移动');
console.log('  look - 观察周围环境');
console.log('  say [内容] - 说话');
console.log('  map - 查看地图');
console.log('  quit - 离开世界');
console.log('─────────────────────────────────────');
console.log();

// 创建 readline 接口
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  prompt: '\n🌍 '
});

rl.prompt();

rl.on('line', (input: string) => {
  const [cmd, ...args] = input.trim().split(' ');
  
  switch (cmd.toLowerCase()) {
    case 'move':
      if (!args[0] || !['north', 'south', 'east', 'west'].includes(args[0])) {
        console.log('请指定方向: north, south, east, west');
      } else {
        const result = commands.move(player, args[0] as any);
        console.log(result.message);
        if (result.event) {
          console.log(`\n[事件记录] ${result.event.timestamp.toISOString()}`);
        }
      }
      break;

    case 'look':
    case 'observe':
      const lookResult = commands.observe(player);
      console.log(lookResult.message);
      break;

    case 'say':
      if (args.length === 0) {
        console.log('请输入要说的话');
      } else {
        const content = args.join(' ');
        const sayResult = commands.speak(player, content);
        console.log(sayResult.message);
      }
      break;

    case 'map':
      console.log('\n【地图】');
      console.log(world.render());
      console.log('图例: @ = Tony  🐾 = 小小  🌸 = 巧巧  · = 空地');
      break;

    case 'quit':
    case 'exit':
      console.log('\n📓 正在生成离境日记...');
      console.log('─────────────────────────────────────');
      console.log('本次停留时间：待计算');
      console.log('关键事件：移动、观察');
      console.log('─────────────────────────────────────');
      console.log('\n🌙 世界进入休眠，期待下次相遇');
      rl.close();
      return;

    case 'help':
      console.log('可用命令：');
      console.log('  move [north|south|east|west] - 移动');
      console.log('  look - 观察周围环境');
      console.log('  say [内容] - 说话');
      console.log('  map - 查看地图');
      console.log('  quit - 离开世界');
      break;

    default:
      console.log('未知命令，输入 help 查看可用命令');
  }

  rl.prompt();
});

rl.on('close', () => {
  process.exit(0);
});
