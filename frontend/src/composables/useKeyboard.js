import { onMounted, onUnmounted, ref } from 'vue'
import { usePlayerStore } from '../stores/playerStore'
import { useMapStore } from '../stores/mapStore'
import { useUIStore } from '../stores/uiStore'
import { useCommand } from './useCommand'

/**
 * 键盘事件处理的composable
 */
export function useKeyboard(mapViewRef) {
  const playerStore = usePlayerStore()
  const mapStore = useMapStore()
  const uiStore = useUIStore()
  const { move, interact } = useCommand()

  // 按键状态
  const pressedKeys = ref(new Set())

  // 方向映射
  const DIRECTION_MAP = {
    'w': { dx: 0, dy: 1 },   // 上（y增加）
    'a': { dx: -1, dy: 0 },  // 左
    's': { dx: 0, dy: -1 },  // 下（y减少）
    'd': { dx: 1, dy: 0 }    // 右
  }

  // 面板快捷键
  const PANEL_KEYS = {
    '1': 'character',
    '2': 'inventory',
    '3': 'party',
    '4': 'entities'
  }

  /**
   * 处理按键按下
   */
  function handleKeyDown(event) {
    // 如果焦点在输入框，不处理
    if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
      return
    }

    const key = event.key.toLowerCase()
    pressedKeys.value.add(key)

    // WASD移动
    if (DIRECTION_MAP[key] && mapStore.windowType === 'map') {
      event.preventDefault()
      handleMove(key)
      return
    }

    // 回车交互
    if (event.key === 'Enter' && mapStore.windowType === 'map') {
      event.preventDefault()
      handleEnterInteract()
      return
    }

    // 数字键切换面板
    if (PANEL_KEYS[key]) {
      event.preventDefault()
      uiStore.togglePanel(PANEL_KEYS[key])
      return
    }

    // ESC关闭面板/弹窗
    if (event.key === 'Escape') {
      event.preventDefault()
      if (uiStore.showInteractionModal) {
        uiStore.closeInteraction()
      } else if (uiStore.contextMenu.visible) {
        uiStore.hideContextMenu()
      } else if (uiStore.activePanel) {
        uiStore.closePanel()
      }
      return
    }
  }

  /**
   * 处理按键释放
   */
  function handleKeyUp(event) {
    const key = event.key.toLowerCase()
    pressedKeys.value.delete(key)
  }

  /**
   * 处理移动
   */
  async function handleMove(key) {
    const direction = DIRECTION_MAP[key]
    if (!direction) return

    const newX = playerStore.x + direction.dx
    const newY = playerStore.y + direction.dy

    // 更新朝向
    playerStore.setFacing(direction.dx, direction.dy)

    // 检查是否可通行
    if (mapStore.isPassable(newX, newY)) {
      await move(newX, newY)
    }
  }

  /**
   * 处理回车交互
   */
  function handleEnterInteract() {
    const facingX = playerStore.x + playerStore.facing.dx
    const facingY = playerStore.y + playerStore.facing.dy

    // 查找面向位置的实体
    const entity = mapStore.getEntityAt(facingX, facingY)
    if (entity && entity.interactionOptions && entity.interactionOptions.length > 0) {
      uiStore.openInteraction(entity)
    }
  }

  /**
   * 检查按键是否按下
   */
  function isKeyPressed(key) {
    return pressedKeys.value.has(key.toLowerCase())
  }

  // 生命周期
  onMounted(() => {
    window.addEventListener('keydown', handleKeyDown)
    window.addEventListener('keyup', handleKeyUp)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeyDown)
    window.removeEventListener('keyup', handleKeyUp)
  })

  return {
    pressedKeys,
    isKeyPressed,
    handleKeyDown,
    handleKeyUp
  }
}
