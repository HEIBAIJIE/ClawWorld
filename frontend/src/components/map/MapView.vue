<template>
  <div
    class="map-view sci-panel"
    ref="mapViewRef"
    tabindex="0"
    @click="handleClick"
    @contextmenu.prevent="handleRightClick"
  >
    <canvas
      ref="canvasRef"
      class="map-canvas"
      :class="{ dragging: isDragging }"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
      @wheel.prevent="handleWheel"
      @mousedown="handleMouseDown"
    ></canvas>

    <!-- 重置视角按钮 -->
    <button
      v-if="hasDragOffset"
      class="reset-view-btn"
      @click="resetDragOffset"
      title="重置视角"
    >
      ⌂
    </button>

    <!-- 操作提示（含坐标） -->
    <div class="control-hints">
      <span v-if="hoveredCell" class="coord-hint">({{ hoveredCell.x }}, {{ hoveredCell.y }})<template v-if="hoveredEntity"> - {{ hoveredEntity.name }}</template></span>
      <span>WASD 移动</span>
      <span>点击 交互</span>
      <span>滚轮 缩放</span>
      <span>拖动 平移</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMapStore } from '../../stores/mapStore'
import { usePlayerStore } from '../../stores/playerStore'
import { useUIStore } from '../../stores/uiStore'
import { useMapRenderer } from '../../composables/useMapRenderer'
import { useKeyboard } from '../../composables/useKeyboard'
import { useCommand } from '../../composables/useCommand'

const mapStore = useMapStore()
const playerStore = usePlayerStore()
const uiStore = useUIStore()
const { interact } = useCommand()

const mapViewRef = ref(null)
const canvasRef = ref(null)

// 使用地图渲染器
const {
  hoveredCell,
  isDragging,
  dragOffsetX,
  dragOffsetY,
  render,
  screenToMap,
  handleMouseMove,
  handleMouseLeave,
  handleWheel,
  handleMouseDown,
  resetDragOffset,
  resizeCanvas
} = useMapRenderer(canvasRef)

// 使用键盘控制
useKeyboard(mapViewRef)

// 是否有拖动偏移
const hasDragOffset = computed(() => {
  return Math.abs(dragOffsetX.value) > 5 || Math.abs(dragOffsetY.value) > 5
})

// 悬浮的实体
const hoveredEntity = computed(() => {
  if (!hoveredCell.value) return null
  return mapStore.getEntityAt(hoveredCell.value.x, hoveredCell.value.y)
})

// 处理点击
function handleClick(event) {
  // 聚焦以接收键盘事件
  mapViewRef.value?.focus()

  const rect = canvasRef.value.getBoundingClientRect()
  const screenX = event.clientX - rect.left
  const screenY = event.clientY - rect.top
  const mapCoord = screenToMap(screenX, screenY)

  if (!mapCoord) return

  const entity = mapStore.getEntityAt(mapCoord.x, mapCoord.y)
  if (entity && entity.name !== playerStore.name) {
    const distance = mapStore.getDistance(playerStore.x, playerStore.y, mapCoord.x, mapCoord.y)
    if (distance <= 8 && entity.interactionOptions?.length > 0) {
      uiStore.openInteraction(entity)
    } else if (distance > 8) {
      uiStore.showToast('目标太远，无法交互', 'warning')
    }
  }
}

// 处理右键
function handleRightClick(event) {
  const rect = canvasRef.value.getBoundingClientRect()
  const screenX = event.clientX - rect.left
  const screenY = event.clientY - rect.top
  const mapCoord = screenToMap(screenX, screenY)

  if (!mapCoord) return

  const entity = mapStore.getEntityAt(mapCoord.x, mapCoord.y)
  if (entity && entity.name !== playerStore.name && entity.interactionOptions?.length > 0) {
    const items = entity.interactionOptions.map(option => ({
      label: option,
      action: () => interact(entity.name, option)
    }))
    uiStore.showContextMenu(event.clientX, event.clientY, items, entity)
  }
}

onMounted(() => {
  resizeCanvas()
  render()
  // 自动聚焦
  mapViewRef.value?.focus()
})
</script>

<style scoped>
.map-view {
  flex: 1;
  position: relative;
  overflow: hidden;
  outline: none;
  border-radius: 0 0 var(--panel-radius) var(--panel-radius);
  width: 100%;
  height: 100%;
}

.map-view:focus {
  border-color: var(--primary);
}

.map-canvas {
  width: 100%;
  height: 100%;
  display: block;
  cursor: grab;
}

.map-canvas.dragging {
  cursor: grabbing;
}

.reset-view-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: rgba(37, 37, 37, 0.9);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.reset-view-btn:hover {
  background: rgba(76, 175, 80, 0.3);
  border-color: var(--primary);
  color: var(--primary);
}

.control-hints {
  position: absolute;
  bottom: 8px;
  left: 8px;
  display: flex;
  gap: 12px;
  font-size: 10px;
  color: var(--text-muted);
}

.control-hints span {
  background: rgba(37, 37, 37, 0.8);
  padding: 2px 6px;
  border-radius: 2px;
}

.control-hints .coord-hint {
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
