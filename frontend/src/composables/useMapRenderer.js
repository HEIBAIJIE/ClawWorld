import { ref, watch, onMounted, onUnmounted } from 'vue'
import { usePlayerStore } from '../stores/playerStore'
import { useMapStore } from '../stores/mapStore'

/**
 * 地图渲染的composable
 */
export function useMapRenderer(canvasRef) {
  const playerStore = usePlayerStore()
  const mapStore = useMapStore()

  // 渲染配置
  const CELL_SIZE = ref(48)
  const MIN_CELL_SIZE = 24
  const MAX_CELL_SIZE = 72

  // 视口偏移
  const offsetX = ref(0)
  const offsetY = ref(0)

  // 鼠标悬浮的格子
  const hoveredCell = ref(null)

  // 颜色配置
  const TERRAIN_COLORS = {
    GRASS: '#2d5a27',
    WATER: '#1a4a6e',
    ROCK: '#4a4a4a',
    SAND: '#c2b280',
    SNOW: '#e8e8e8',
    TREE: '#1b4d1b',
    WALL: '#333333',
    '草地': '#2d5a27',
    '水': '#1a4a6e',
    '岩石': '#4a4a4a'
  }

  const ENTITY_COLORS = {
    PLAYER: '#4CAF50',
    ENEMY: '#f44336',
    NPC: '#2196F3',
    WAYPOINT: '#9c27b0',
    CAMPFIRE: '#ff9800'
  }

  const ENTITY_ICONS = {
    PLAYER: '👤',
    ENEMY: '👹',
    NPC: '🧙',
    WAYPOINT: '🌀',
    CAMPFIRE: '🔥'
  }

  /**
   * 渲染地图
   */
  function render() {
    const canvas = canvasRef.value
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    const { width, height } = canvas

    // 清空画布
    ctx.fillStyle = '#0a0a0a'
    ctx.fillRect(0, 0, width, height)

    // 计算视口
    const viewportWidth = Math.ceil(width / CELL_SIZE.value)
    const viewportHeight = Math.ceil(height / CELL_SIZE.value)

    // 居中玩家
    centerOnPlayer(viewportWidth, viewportHeight)

    // 渲染地形
    renderTerrain(ctx, viewportWidth, viewportHeight)

    // 渲染网格线
    renderGrid(ctx, viewportWidth, viewportHeight)

    // 渲染实体
    renderEntities(ctx)

    // 渲染玩家
    renderPlayer(ctx)

    // 渲染悬浮高亮
    if (hoveredCell.value) {
      renderHoveredCell(ctx)
    }
  }

  /**
   * 居中玩家
   */
  function centerOnPlayer(viewportWidth, viewportHeight) {
    const targetOffsetX = playerStore.x - Math.floor(viewportWidth / 2)
    const targetOffsetY = playerStore.y - Math.floor(viewportHeight / 2)

    // 边界限制
    offsetX.value = Math.max(0, Math.min(targetOffsetX, mapStore.width - viewportWidth))
    offsetY.value = Math.max(0, Math.min(targetOffsetY, mapStore.height - viewportHeight))
  }

  /**
   * 渲染地形
   */
  function renderTerrain(ctx, viewportWidth, viewportHeight) {
    for (let vy = 0; vy < viewportHeight + 1; vy++) {
      for (let vx = 0; vx < viewportWidth + 1; vx++) {
        const mapX = vx + offsetX.value
        const mapY = offsetY.value + (viewportHeight - vy - 1) // Y轴翻转

        if (mapX < 0 || mapX >= mapStore.width || mapY < 0 || mapY >= mapStore.height) {
          continue
        }

        const cell = mapStore.grid[mapY]?.[mapX]
        const terrain = cell?.terrain || 'GRASS'
        const color = TERRAIN_COLORS[terrain] || TERRAIN_COLORS.GRASS

        const screenX = vx * CELL_SIZE.value
        const screenY = vy * CELL_SIZE.value

        ctx.fillStyle = color
        ctx.fillRect(screenX, screenY, CELL_SIZE.value, CELL_SIZE.value)
      }
    }
  }

  /**
   * 渲染网格线
   */
  function renderGrid(ctx, viewportWidth, viewportHeight) {
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)'
    ctx.lineWidth = 1

    // 垂直线
    for (let x = 0; x <= viewportWidth + 1; x++) {
      ctx.beginPath()
      ctx.moveTo(x * CELL_SIZE.value, 0)
      ctx.lineTo(x * CELL_SIZE.value, (viewportHeight + 1) * CELL_SIZE.value)
      ctx.stroke()
    }

    // 水平线
    for (let y = 0; y <= viewportHeight + 1; y++) {
      ctx.beginPath()
      ctx.moveTo(0, y * CELL_SIZE.value)
      ctx.lineTo((viewportWidth + 1) * CELL_SIZE.value, y * CELL_SIZE.value)
      ctx.stroke()
    }
  }

  /**
   * 渲染实体
   */
  function renderEntities(ctx) {
    const canvas = canvasRef.value
    const viewportHeight = Math.ceil(canvas.height / CELL_SIZE.value)

    for (const entity of mapStore.entities) {
      // 跳过玩家自己
      if (entity.name === playerStore.name) continue

      const screenX = (entity.x - offsetX.value) * CELL_SIZE.value + CELL_SIZE.value / 2
      const screenY = (viewportHeight - (entity.y - offsetY.value) - 1) * CELL_SIZE.value + CELL_SIZE.value / 2

      // 检查是否在视口内
      if (screenX < -CELL_SIZE.value || screenX > canvas.width + CELL_SIZE.value ||
          screenY < -CELL_SIZE.value || screenY > canvas.height + CELL_SIZE.value) {
        continue
      }

      // 绘制实体圆形背景
      const color = ENTITY_COLORS[entity.type] || '#888'
      ctx.fillStyle = color
      ctx.beginPath()
      ctx.arc(screenX, screenY, CELL_SIZE.value * 0.35, 0, Math.PI * 2)
      ctx.fill()

      // 绘制图标
      const icon = ENTITY_ICONS[entity.type] || '?'
      ctx.font = `${CELL_SIZE.value * 0.4}px Arial`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText(icon, screenX, screenY)

      // 绘制名称
      ctx.fillStyle = '#fff'
      ctx.font = `${Math.max(10, CELL_SIZE.value * 0.22)}px Arial`
      ctx.fillText(entity.name, screenX, screenY + CELL_SIZE.value * 0.45)

      // 绘制等级（如果有）
      if (entity.level) {
        ctx.fillStyle = '#ffd700'
        ctx.font = `${Math.max(8, CELL_SIZE.value * 0.18)}px Arial`
        ctx.fillText(`Lv.${entity.level}`, screenX, screenY - CELL_SIZE.value * 0.45)
      }
    }
  }

  /**
   * 渲染玩家
   */
  function renderPlayer(ctx) {
    const canvas = canvasRef.value
    const viewportHeight = Math.ceil(canvas.height / CELL_SIZE.value)

    const screenX = (playerStore.x - offsetX.value) * CELL_SIZE.value + CELL_SIZE.value / 2
    const screenY = (viewportHeight - (playerStore.y - offsetY.value) - 1) * CELL_SIZE.value + CELL_SIZE.value / 2

    // 绘制玩家光环
    ctx.strokeStyle = '#8BC34A'
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.arc(screenX, screenY, CELL_SIZE.value * 0.4, 0, Math.PI * 2)
    ctx.stroke()

    // 绘制玩家圆形背景
    ctx.fillStyle = '#4CAF50'
    ctx.beginPath()
    ctx.arc(screenX, screenY, CELL_SIZE.value * 0.35, 0, Math.PI * 2)
    ctx.fill()

    // 绘制玩家图标
    ctx.font = `${CELL_SIZE.value * 0.4}px Arial`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(playerStore.roleIcon, screenX, screenY)

    // 绘制朝向指示器
    const facingX = screenX + playerStore.facing.dx * CELL_SIZE.value * 0.5
    const facingY = screenY - playerStore.facing.dy * CELL_SIZE.value * 0.5 // Y轴翻转
    ctx.fillStyle = 'rgba(139, 195, 74, 0.5)'
    ctx.beginPath()
    ctx.arc(facingX, facingY, CELL_SIZE.value * 0.1, 0, Math.PI * 2)
    ctx.fill()

    // 绘制名称
    ctx.fillStyle = '#8BC34A'
    ctx.font = `bold ${Math.max(10, CELL_SIZE.value * 0.22)}px Arial`
    ctx.fillText(playerStore.name || '你', screenX, screenY + CELL_SIZE.value * 0.45)
  }

  /**
   * 渲染悬浮高亮
   */
  function renderHoveredCell(ctx) {
    const canvas = canvasRef.value
    const viewportHeight = Math.ceil(canvas.height / CELL_SIZE.value)

    const { x, y } = hoveredCell.value
    const screenX = (x - offsetX.value) * CELL_SIZE.value
    const screenY = (viewportHeight - (y - offsetY.value) - 1) * CELL_SIZE.value

    ctx.strokeStyle = 'rgba(76, 175, 80, 0.8)'
    ctx.lineWidth = 2
    ctx.strokeRect(screenX + 2, screenY + 2, CELL_SIZE.value - 4, CELL_SIZE.value - 4)
  }

  /**
   * 屏幕坐标转地图坐标
   */
  function screenToMap(screenX, screenY) {
    const canvas = canvasRef.value
    if (!canvas) return null

    const viewportHeight = Math.ceil(canvas.height / CELL_SIZE.value)
    const mapX = Math.floor(screenX / CELL_SIZE.value) + offsetX.value
    const mapY = viewportHeight - Math.floor(screenY / CELL_SIZE.value) - 1 + offsetY.value

    return { x: mapX, y: mapY }
  }

  /**
   * 处理鼠标移动
   */
  function handleMouseMove(event) {
    const canvas = canvasRef.value
    if (!canvas) return

    const rect = canvas.getBoundingClientRect()
    const screenX = event.clientX - rect.left
    const screenY = event.clientY - rect.top

    hoveredCell.value = screenToMap(screenX, screenY)
    render()
  }

  /**
   * 处理鼠标离开
   */
  function handleMouseLeave() {
    hoveredCell.value = null
    render()
  }

  /**
   * 处理缩放
   */
  function handleWheel(event) {
    event.preventDefault()
    const delta = event.deltaY > 0 ? -4 : 4
    CELL_SIZE.value = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, CELL_SIZE.value + delta))
    render()
  }

  /**
   * 调整画布大小
   */
  function resizeCanvas() {
    const canvas = canvasRef.value
    if (!canvas) return

    const parent = canvas.parentElement
    if (parent) {
      canvas.width = parent.clientWidth
      canvas.height = parent.clientHeight
      render()
    }
  }

  // 监听数据变化重新渲染
  watch(
    () => [mapStore.grid, mapStore.entities, playerStore.x, playerStore.y],
    () => render(),
    { deep: true }
  )

  // 生命周期
  onMounted(() => {
    resizeCanvas()
    window.addEventListener('resize', resizeCanvas)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', resizeCanvas)
  })

  return {
    CELL_SIZE,
    offsetX,
    offsetY,
    hoveredCell,
    render,
    screenToMap,
    handleMouseMove,
    handleMouseLeave,
    handleWheel,
    resizeCanvas
  }
}
