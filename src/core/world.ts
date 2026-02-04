// ClawWorld Core - 世界地图系统

export interface Cell {
  x: number;
  y: number;
  terrain: string;
  description: string;
}

export class World {
  private cells: Map<string, Cell> = new Map();
  private entities: Map<string, Entity> = new Map();

  constructor(private width: number = 3, private height: number = 3) {
    this.initializeMap();
  }

  private initializeMap() {
    // 创建 3x3 世界
    const terrains = [
      ['橡树林', '草地', '边界塔'],
      ['小径', '广场', '悬崖'],
      ['档案馆', '溪流', '迷雾']
    ];

    const descriptions = [
      ['古老橡树下，风吹过树叶沙沙响', '一片开阔的草地，野花点缀', '巧巧的居所，半透明的塔身 data stream 流动'],
      ['蜿蜒的小径通向远方', '世界的中心，玩家们相遇的地方', '陡峭的悬崖，下方是虚空'],
      ['小小的档案馆，漂浮的书架和光点', '清澈的溪流，水声潺潺', '迷雾笼罩，看不清前方']
    ];

    for (let y = 0; y < this.height; y++) {
      for (let x = 0; x < this.width; x++) {
        const cell: Cell = {
          x,
          y,
          terrain: terrains[y][x],
          description: descriptions[y][x]
        };
        this.cells.set(this.key(x, y), cell);
      }
    }
  }

  private key(x: number, y: number): string {
    return `${x},${y}`;
  }

  getCell(x: number, y: number): Cell | undefined {
    return this.cells.get(this.key(x, y));
  }

  isValidPosition(x: number, y: number): boolean {
    return x >= 0 && x < this.width && y >= 0 && y < this.height;
  }

  addEntity(entity: Entity) {
    this.entities.set(entity.id, entity);
  }

  getEntitiesAt(x: number, y: number): Entity[] {
    return Array.from(this.entities.values()).filter(e => e.x === x && e.y === y);
  }

  getAllEntities(): Entity[] {
    return Array.from(this.entities.values());
  }

  render(): string {
    let map = '';
    for (let y = 0; y < this.height; y++) {
      for (let x = 0; x < this.width; x++) {
        const entities = this.getEntitiesAt(x, y);
        if (entities.length > 0) {
          map += entities[0].symbol;
        } else {
          map += '·';
        }
        map += ' ';
      }
      map += '\n';
    }
    return map;
  }
}

export interface Entity {
  id: string;
  name: string;
  symbol: string;
  x: number;
  y: number;
  type: 'human' | 'agent';
}
