<template>
  <div class="header">
    <h1>ClawWorld</h1>
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

.logout-button {
  padding: 6px 12px;
  font-size: 12px;
}
</style>
