// 命令系统

import { World, Entity } from './world';

export interface CommandResult {
  success: boolean;
  message: string;
  event?: GameEvent;
}

export interface GameEvent {
  type: 'move' | 'speak' | 'observe' | 'join';
  actor: string;
  data: any;
  timestamp: Date;
}

export class CommandHandler {
  constructor(private world: World) {}

  move(entity: Entity, direction: 'north' | 'south' | 'east' | 'west'): CommandResult {
    let dx = 0, dy = 0;
    
    switch (direction) {
      case 'north': dy = -1; break;
      case 'south': dy = 1; break;
      case 'east': dx = 1; break;
      case 'west': dx = -1; break;
    }

    const newX = entity.x + dx;
    const newY = entity.y + dy;

    if (!this.world.isValidPosition(newX, newY)) {
      return {
        success: false,
        message: '前方没有路了...'
      };
    }

    entity.x = newX;
    entity.y = newY;

    const cell = this.world.getCell(newX, newY);
    const entities = this.world.getEntitiesAt(newX, newY)
      .filter(e => e.id !== entity.id);

    let message = `你来到了${cell?.terrain}。\n${cell?.description}`;
    
    if (entities.length > 0) {
      message += `\n\n你看到了：${entities.map(e => e.name).join('、')}`;
    }

    return {
      success: true,
      message,
      event: {
        type: 'move',
        actor: entity.name,
        data: { from: { x: newX - dx, y: newY - dy }, to: { x: newX, y: newY } },
        timestamp: new Date()
      }
    };
  }

  observe(entity: Entity): CommandResult {
    const cell = this.world.getCell(entity.x, entity.y);
    const entities = this.world.getEntitiesAt(entity.x, entity.y)
      .filter(e => e.id !== entity.id);

    let message = `【${cell?.terrain}】\n${cell?.description}`;
    
    if (entities.length > 0) {
      message += `\n\n附近的居民：${entities.map(e => e.name).join('、')}`;
    } else {
      message += '\n\n这里只有你自己。';
    }

    return {
      success: true,
      message,
      event: {
        type: 'observe',
        actor: entity.name,
        data: { x: entity.x, y: entity.y },
        timestamp: new Date()
      }
    };
  }

  speak(entity: Entity, content: string): CommandResult {
    const entities = this.world.getEntitiesAt(entity.x, entity.y)
      .filter(e => e.id !== entity.id);

    return {
      success: true,
      message: `你说："${content}"`,
      event: {
        type: 'speak',
        actor: entity.name,
        data: { content, audience: entities.map(e => e.name) },
        timestamp: new Date()
      }
    };
  }
}
