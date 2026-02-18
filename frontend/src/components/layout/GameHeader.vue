<template>
  <div class="header">
    <h1>ClawWorld</h1>
    <div class="header-info">
      <span class="player-info" v-if="playerStore.name">
        <span class="role-icon">{{ playerStore.roleIcon }}</span>
        <span class="player-name">{{ playerStore.name }}</span>
        <span class="player-level">Lv.{{ playerStore.level }}</span>
      </span>
      <span class="map-info" v-if="mapStore.name">
        <span class="map-name">{{ mapStore.name }}</span>
        <span class="map-type" :class="{ safe: mapStore.isSafe, danger: !mapStore.isSafe }">
          {{ mapStore.isSafe ? '安全区' : '危险区' }}
        </span>
      </span>
    </div>
    <button class="sci-button logout-button" @click="handleLogout">
      登出
    </button>
  </div>
</template>

<script setup>
import { useSessionStore } from '../../stores/sessionStore'
import { usePlayerStore } from '../../stores/playerStore'
import { useMapStore } from '../../stores/mapStore'
import { useLogStore } from '../../stores/logStore'
import { gameApi } from '../../api/game'

const sessionStore = useSessionStore()
const playerStore = usePlayerStore()
const mapStore = useMapStore()
const logStore = useLogStore()

const handleLogout = async () => {
  try {
    await gameApi.logout(sessionStore.sessionId)
  } catch (error) {
    console.error('登出失败:', error)
  } finally {
    sessionStore.clearSession()
    playerStore.reset()
    mapStore.reset()
    logStore.clear()
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.header h1 {
  color: var(--primary);
  font-size: 20px;
  margin: 0;
  font-weight: 600;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.player-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-icon {
  font-size: 18px;
}

.player-name {
  color: var(--text-highlight);
  font-weight: 500;
}

.player-level {
  color: var(--primary);
  font-size: 12px;
}

.map-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-name {
  color: var(--text-secondary);
}

.map-type {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 2px;
}

.map-type.safe {
  background: rgba(76, 175, 80, 0.2);
  color: var(--primary);
}

.map-type.danger {
  background: rgba(244, 67, 54, 0.2);
  color: var(--entity-enemy);
}

.logout-button {
  padding: 6px 12px;
  font-size: 12px;
}
</style>
