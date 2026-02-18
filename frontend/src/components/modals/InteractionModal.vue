<template>
  <teleport to="body">
    <template v-if="uiStore.showInteractionModal && uiStore.interactionTarget">
      <!-- 遮罩 -->
      <div class="modal-overlay" @click="uiStore.closeInteraction()"></div>

      <!-- 弹窗 -->
      <div class="interaction-modal sci-panel">
        <div class="interaction-modal-header">
          <div class="interaction-entity-icon" :class="entityTypeClass">
            {{ entityIcon }}
          </div>
          <div class="interaction-entity-info">
            <div class="interaction-entity-name">{{ uiStore.interactionTarget.name }}</div>
            <div class="interaction-entity-level" v-if="uiStore.interactionTarget.level">
              Lv.{{ uiStore.interactionTarget.level }}
            </div>
            <div class="interaction-entity-type">{{ entityTypeLabel }}</div>
          </div>
        </div>

        <div class="interaction-options">
          <button
            v-for="option in uiStore.interactionTarget.interactionOptions"
            :key="option"
            class="sci-button interaction-option"
            @click="handleInteraction(option)"
          >
            {{ option }}
          </button>
          <button class="sci-button interaction-option cancel" @click="uiStore.closeInteraction()">
            取消
          </button>
        </div>
      </div>
    </template>
  </teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useUIStore } from '../../stores/uiStore'
import { useCommand } from '../../composables/useCommand'

const uiStore = useUIStore()
const { interact } = useCommand()

// 实体图标
const entityIcon = computed(() => {
  const icons = {
    PLAYER: '👤',
    ENEMY: '👹',
    NPC: '🧙',
    WAYPOINT: '🌀',
    CAMPFIRE: '🔥'
  }
  return icons[uiStore.interactionTarget?.type] || '❓'
})

// 实体类型样式
const entityTypeClass = computed(() => {
  const type = uiStore.interactionTarget?.type?.toLowerCase()
  return type || ''
})

// 实体类型标签
const entityTypeLabel = computed(() => {
  const labels = {
    PLAYER: '玩家',
    ENEMY: '敌人',
    NPC: 'NPC',
    WAYPOINT: '传送点',
    CAMPFIRE: '篝火'
  }
  return labels[uiStore.interactionTarget?.type] || ''
})

// 处理交互
async function handleInteraction(option) {
  const target = uiStore.interactionTarget
  uiStore.closeInteraction()
  await interact(target.name, option)
}
</script>

<style scoped>
.interaction-entity-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  border-radius: 8px;
}

.interaction-entity-icon.enemy {
  background: rgba(244, 67, 54, 0.2);
}

.interaction-entity-icon.player {
  background: rgba(76, 175, 80, 0.2);
}

.interaction-entity-icon.npc {
  background: rgba(33, 150, 243, 0.2);
}

.interaction-entity-icon.waypoint {
  background: rgba(156, 39, 176, 0.2);
}

.interaction-entity-icon.campfire {
  background: rgba(255, 152, 0, 0.2);
}

.interaction-entity-type {
  font-size: 11px;
  color: var(--text-muted);
}

.interaction-option.cancel {
  background: transparent;
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.interaction-option.cancel:hover {
  background: var(--bg-hover);
}
</style>
