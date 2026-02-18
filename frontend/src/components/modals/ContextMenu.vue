<template>
  <teleport to="body">
    <div
      v-if="uiStore.contextMenu.visible"
      class="context-menu sci-panel"
      :style="{ left: uiStore.contextMenu.x + 'px', top: uiStore.contextMenu.y + 'px' }"
    >
      <div
        v-for="(item, index) in uiStore.contextMenu.items"
        :key="index"
        class="context-menu-item"
        @click="handleItemClick(item)"
      >
        {{ item.label }}
      </div>
    </div>

    <!-- 点击其他地方关闭菜单 -->
    <div
      v-if="uiStore.contextMenu.visible"
      class="context-menu-backdrop"
      @click="uiStore.hideContextMenu()"
    ></div>
  </teleport>
</template>

<script setup>
import { useUIStore } from '../../stores/uiStore'

const uiStore = useUIStore()

function handleItemClick(item) {
  uiStore.hideContextMenu()
  if (item.action) {
    item.action()
  }
}
</script>

<style scoped>
.context-menu-backdrop {
  position: fixed;
  inset: 0;
  z-index: 199;
}

.context-menu {
  position: fixed;
  z-index: 200;
}
</style>
