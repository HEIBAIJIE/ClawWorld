/**
 * 地图数据解析器
 * 解析地图网格、实体列表等信息
 */

/**
 * 解析地图网格
 * 输入格式: "(0,11) GRASS  (1,11) GRASS  (2,11) 史莱姆#1  ..."
 * @param {string} content - 地图网格文本
 * @returns {object} { grid: 二维数组, width, height }
 */
export function parseMapGrid(content) {
  const cellPattern = /\((\d+),(\d+)\)\s+(\S+)/g
  const cells = []
  let match
  let maxX = 0
  let maxY = 0

  while ((match = cellPattern.exec(content)) !== null) {
    const x = parseInt(match[1])
    const y = parseInt(match[2])
    const cellContent = match[3]

    maxX = Math.max(maxX, x)
    maxY = Math.max(maxY, y)

    // 判断是地形还是实体
    const isEntity = !isTerrain(cellContent)

    cells.push({
      x,
      y,
      content: cellContent,
      isEntity,
      terrain: isEntity ? 'GRASS' : cellContent, // 如果是实体，默认地形为草地
      passable: isPassableTerrain(isEntity ? 'GRASS' : cellContent)
    })
  }

  // 构建二维数组 [y][x]
  const width = maxX + 1
  const height = maxY + 1
  const grid = []

  for (let y = 0; y < height; y++) {
    grid[y] = []
    for (let x = 0; x < width; x++) {
      const cell = cells.find(c => c.x === x && c.y === y)
      grid[y][x] = cell || { x, y, terrain: 'GRASS', passable: true, isEntity: false }
    }
  }

  return { grid, width, height }
}

/**
 * 判断是否为地形
 */
function isTerrain(content) {
  const terrains = ['GRASS', 'WATER', 'ROCK', 'SAND', 'SNOW', 'TREE', 'WALL', '草地', '水', '岩石', '沙地', '雪地', '树', '墙']
  return terrains.includes(content.toUpperCase()) || terrains.includes(content)
}

/**
 * 判断地形是否可通行
 */
function isPassableTerrain(terrain) {
  const impassable = ['WATER', 'ROCK', 'TREE', 'WALL', '水', '岩石', '树', '墙', '河流', '海洋', '山脉']
  return !impassable.includes(terrain.toUpperCase()) && !impassable.includes(terrain)
}

/**
 * 解析实体列表
 * 输入格式:
 * "史莱姆#1 Lv.3 (8,8) [可直接交互] [类型：普通敌人] [交互选项: 查看, 攻击]"
 * "森林入口 (0,0) [可直接交互] [类型：传送点] [交互选项: 传送到新手村·村中心传送点]"
 * @param {string} content - 实体列表文本
 * @returns {array} 实体数组
 */
export function parseEntityList(content) {
  const lines = content.split('\n')
  const entities = []

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed || trimmed.includes('地图实体：') || trimmed.includes('没有其他实体')) continue

    // 尝试匹配带等级的格式: "名称 Lv.X (x,y) [状态] [类型：XXX] [交互选项: ...]"
    let match = trimmed.match(/^(.+?)\s+Lv\.(\d+)\s+\((\d+),(\d+)\)\s+\[([^\]]+)\]\s+\[类型[：:]([^\]]+)\](?:\s+\[交互选项[：:]\s*([^\]]*)\])?/)

    if (match) {
      entities.push({
        name: match[1].trim(),
        level: parseInt(match[2]),
        x: parseInt(match[3]),
        y: parseInt(match[4]),
        accessibility: match[5],
        type: mapEntityTypeToInternal(match[6].trim()),
        displayType: match[6].trim(),
        interactionOptions: parseInteractionOptions(match[7] || ''),
        isInRange: match[5] === '可直接交互'
      })
      continue
    }

    // 尝试匹配不带等级的格式: "名称 (x,y) [状态] [类型：XXX] [交互选项: ...]"
    match = trimmed.match(/^(.+?)\s+\((\d+),(\d+)\)\s+\[([^\]]+)\]\s+\[类型[：:]([^\]]+)\](?:\s+\[交互选项[：:]\s*([^\]]*)\])?/)

    if (match) {
      entities.push({
        name: match[1].trim(),
        level: null,
        x: parseInt(match[2]),
        y: parseInt(match[3]),
        accessibility: match[4],
        type: mapEntityTypeToInternal(match[5].trim()),
        displayType: match[5].trim(),
        interactionOptions: parseInteractionOptions(match[6] || ''),
        isInRange: match[4] === '可直接交互'
      })
    }
  }

  return entities
}

/**
 * 将中文实体类型映射为内部类型
 */
function mapEntityTypeToInternal(displayType) {
  const typeMap = {
    '玩家': 'PLAYER',
    'NPC': 'NPC',
    '传送点': 'WAYPOINT',
    '篝火': 'CAMPFIRE',
    '普通敌人': 'ENEMY',
    '精英敌人': 'ENEMY_ELITE',
    '地图BOSS': 'ENEMY_BOSS',
    '世界BOSS': 'ENEMY_WORLD_BOSS',
    // 兼容旧格式
    'PLAYER': 'PLAYER',
    'ENEMY': 'ENEMY',
    'WAYPOINT': 'WAYPOINT',
    'CAMPFIRE': 'CAMPFIRE'
  }
  return typeMap[displayType] || displayType
}

/**
 * 解析交互选项
 */
function parseInteractionOptions(optionsStr) {
  if (!optionsStr || optionsStr.trim() === '') return []
  return optionsStr.split(',').map(s => s.trim()).filter(Boolean)
}

/**
 * 解析地图信息头
 * 输入格式: "当前地图名：森林入口，通往黑暗森林的入口【危险区域】推荐等级: 3"
 * @param {string} content - 地图信息文本
 * @returns {object} 地图信息
 */
export function parseMapInfo(content) {
  const result = {
    name: '',
    description: '',
    isSafe: true,
    recommendedLevel: 0
  }

  // 解析地图名
  const nameMatch = content.match(/当前地图名[：:]\s*([^，,【]+)/)
  if (nameMatch) {
    result.name = nameMatch[1].trim()
  }

  // 解析描述
  const descMatch = content.match(/[，,]([^【]+)/)
  if (descMatch) {
    result.description = descMatch[1].trim()
  }

  // 判断是否安全区
  result.isSafe = !content.includes('危险区域') && !content.includes('战斗区')

  // 解析推荐等级
  const levelMatch = content.match(/推荐等级[：:]\s*(\d+)/)
  if (levelMatch) {
    result.recommendedLevel = parseInt(levelMatch[1])
  }

  return result
}

/**
 * 解析可移动交互的实体
 * 输入格式: "哥布林#1: 移动到 (6,5) 可交互"
 * @param {string} content - 文本
 * @returns {array} 可移动交互信息
 */
export function parseMoveToInteract(content) {
  const lines = content.split('\n')
  const result = []
  const pattern = /^(.+?)[：:]\s*移动到\s*\((\d+),(\d+)\)\s*可交互/

  for (const line of lines) {
    const match = line.trim().match(pattern)
    if (match) {
      result.push({
        name: match[1].trim(),
        moveToX: parseInt(match[2]),
        moveToY: parseInt(match[3])
      })
    }
  }

  return result
}

export default {
  parseMapGrid,
  parseEntityList,
  parseMapInfo,
  parseMoveToInteract
}
