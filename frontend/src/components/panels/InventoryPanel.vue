<template>
  <div class="popup-panel sci-panel inventory-panel">
    <div class="popup-panel-header">
      <span class="popup-panel-title">背包 ({{ playerStore.inventory.length }}/50)</span>
      <button class="popup-panel-close" @click="uiStore.closePanel()">×</button>
    </div>

    <div class="popup-panel-content">
      <div class="inventory-grid">
        <div
          v-for="(slot, index) in inventorySlots"
          :key="index"
          class="inventory-slot"
          :class="{ empty: !slot }"
          @click="handleSlotClick(slot, $event)"
          @contextmenu.prevent="handleSlotRightClick(slot, $event)"
        >
          <template v-if="slot">
            <span class="item-icon">{{ getItemIcon(slot) }}</span>
            <span v-if="slot.quantity > 1" class="item-count">{{ slot.quantity }}</span>
          </template>
        </div>
      </div>

      <div v-if="playerStore.inventory.length === 0" class="empty-inventory">
        背包为空
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUIStore } from '../../stores/uiStore'
import { usePlayerStore } from '../../stores/playerStore'
import { useCommand } from '../../composables/useCommand'

const uiStore = useUIStore()
const playerStore = usePlayerStore()
const { useItem, equip, sendCommand } = useCommand()

// 48格背包槽位
const inventorySlots = computed(() => {
  const slots = new Array(48).fill(null)
  playerStore.inventory.forEach((item, index) => {
    if (index < 48) {
      slots[index] = item
    }
  })
  return slots
})

// 获取物品图标
function getItemIcon(item) {
  if (!item) return ''
  if (item.isEquipment) return '⚔️'
  if (item.name.includes('药水') || item.name.includes('药剂')) return '🧪'
  if (item.name.includes('技能书')) return '📖'
  return '📦'
}

// 点击槽位
function handleSlotClick(slot, event) {
  if (!slot) return
  // 单击显示物品信息
  sendCommand(`inspect ${slot.name}`)
}

// 右键槽位
function handleSlotRightClick(slot, event) {
  if (!slot) return

  const items = [
    { label: '查看', action: () => sendCommand(`inspect ${slot.name}`) }
  ]

  if (slot.isEquipment) {
    items.push({ label: '装备', action: () => equip(slot.name) })
  } else {
    items.push({ label: '使用', action: () => useItem(slot.name) })
  }

  uiStore.showContextMenu(event.clientX, event.clientY, items, slot)
}
</script>

<style scoped>
.inventory-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 4px;
}

.inventory-slot {
  aspect-ratio: 1;
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: all var(--transition-fast);
  min-height: 40px;
}

.inventory-slot:hover:not(.empty) {
  border-color: var(--primary);
  background: var(--bg-hover);
}

.inventory-slot.empty {
  cursor: default;
  opacity: 0.5;
}

.item-icon {
  font-size: 20px;
}

.item-count {
  position: absolute;
  bottom: 2px;
  right: 4px;
  font-size: 10px;
  color: var(--text-primary);
  text-shadow: 0 0 2px var(--bg-dark);
}

.empty-inventory {
  text-align: center;
  color: var(--text-muted);
  padding: 24px;
  font-size: 13px;
}
</style>
