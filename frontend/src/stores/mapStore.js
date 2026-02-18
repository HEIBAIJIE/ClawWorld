import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMapStore = defineStore('map', () => {
  // 地图基础信息
  const id = ref('')
  const name = ref('')
  const description = ref('')
  const width = ref(10)
  const height = ref(10)
  const isSafe = ref(true)
  const recommendedLevel = ref(0)

  // 地图网格数据 - 二维数组 [y][x]
  const grid = ref([])

  // 实体列表
  const entities = ref([])

  // 当前窗口类型
  const windowType = ref('map') // 'map' | 'combat' | 'trade' | 'shop' | 'register'

  // 计算属性：按类型分组的实体
  const entitiesByType = computed(() => {
    const grouped = {
      PLAYER: [],
      ENEMY: [],
      NPC: [],
      WAYPOINT: [],
      CAMPFIRE: []
    }
    entities.value.forEach(entity => {
      if (grouped[entity.type]) {
        grouped[entity.type].push(entity)
      }
    })
    return grouped
  })

  // 获取指定位置的实体
  function getEntityAt(x, y) {
    return entities.value.find(e => e.x === x && e.y === y)
  }

  // 获取指定位置的所有实体
  function getEntitiesAt(x, y) {
    return entities.value.filter(e => e.x === x && e.y === y)
  }

  // 检查位置是否可通行
  function isPassable(x, y) {
    if (x < 0 || x >= width.value || y < 0 || y >= height.value) {
      return false
    }
    // 检查地形
    const cell = grid.value[y]?.[x]
    if (cell && !cell.passable) {
      return false
    }
    // 检查是否有不可通行的实体（如敌人）
    const entitiesAtPos = getEntitiesAt(x, y)
    for (const entity of entitiesAtPos) {
      if (entity.type === 'ENEMY' && !entity.isDead) {
        return false
      }
    }
    return true
  }

  // 计算两点间距离
  function getDistance(x1, y1, x2, y2) {
    return Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))
  }

  // 更新地图信息
  function updateMapInfo(data) {
    if (data.id !== undefined) id.value = data.id
    if (data.name !== undefined) name.value = data.name
    if (data.description !== undefined) description.value = data.description
    if (data.width !== undefined) width.value = data.width
    if (data.height !== undefined) height.value = data.height
    if (data.isSafe !== undefined) isSafe.value = data.isSafe
    if (data.recommendedLevel !== undefined) recommendedLevel.value = data.recommendedLevel
  }

  // 更新地图网格
  function updateGrid(newGrid) {
    grid.value = newGrid
  }

  // 更新实体列表
  function updateEntities(newEntities) {
    entities.value = newEntities
  }

  // 更新单个实体
  function updateEntity(entityName, updates) {
    const entity = entities.value.find(e => e.name === entityName)
    if (entity) {
      Object.assign(entity, updates)
    }
  }

  // 设置窗口类型
  function setWindowType(type) {
    windowType.value = type
  }

  // 重置状态
  function reset() {
    id.value = ''
    name.value = ''
    width.value = 10
    height.value = 10
    grid.value = []
    entities.value = []
    windowType.value = 'map'
  }

  return {
    // 状态
    id, name, description, width, height, isSafe, recommendedLevel,
    grid, entities, windowType,
    // 计算属性
    entitiesByType,
    // 方法
    getEntityAt, getEntitiesAt, isPassable, getDistance,
    updateMapInfo, updateGrid, updateEntities, updateEntity,
    setWindowType, reset
  }
})
