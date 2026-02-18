<template>
  <div class="action-bar">
    <button
      class="sci-button sci-icon-button action-bar-button"
      :class="{ active: uiStore.activePanel === 'character' }"
      @click="uiStore.togglePanel('character')"
      title="角色信息 (1)"
    >
      👤
      <span class="badge" v-if="playerStore.freeAttributePoints > 0">
        {{ playerStore.freeAttributePoints }}
      </span>
    </button>

    <button
      class="sci-button sci-icon-button action-bar-button"
      :class="{ active: uiStore.activePanel === 'inventory' }"
      @click="uiStore.togglePanel('inventory')"
      title="背包 (2)"
    >
      🎒
      <span class="badge" v-if="inventoryCount > 0">
        {{ inventoryCount }}
      </span>
    </button>

    <button
      class="sci-button sci-icon-button action-bar-button"
      :class="{ active: uiStore.activePanel === 'party' }"
      @click="uiStore.togglePanel('party')"
      title="队伍 (3)"
    >
      👥
      <span class="badge" v-if="partyStore.memberCount > 1">
        {{ partyStore.memberCount }}
      </span>
    </button>

    <button
      class="sci-button sci-icon-button action-bar-button"
      :class="{ active: uiStore.activePanel === 'entities' }"
      @click="uiStore.togglePanel('entities')"
      title="实体列表 (4)"
    >
      📋
      <span class="badge" v-if="nearbyEnemyCount > 0">
        {{ nearbyEnemyCount }}
      </span>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUIStore } from '../../stores/uiStore'
import { usePlayerStore } from '../../stores/playerStore'
import { usePartyStore } from '../../stores/partyStore'
import { useMapStore } from '../../stores/mapStore'

const uiStore = useUIStore()
const playerStore = usePlayerStore()
const partyStore = usePartyStore()
const mapStore = useMapStore()

// 背包物品数量
const inventoryCount = computed(() => playerStore.inventory.length)

// 附近敌人数量
const nearbyEnemyCount = computed(() => {
  return mapStore.entities.filter(e =>
    e.type === 'ENEMY' && e.distance <= 8
  ).length
})
</script>
