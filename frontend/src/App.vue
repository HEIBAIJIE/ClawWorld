<template>
  <div id="app">
    <!-- 登录界面 -->
    <LoginView v-if="!sessionStore.isLoggedIn" />

    <!-- 游戏界面 -->
    <GameView v-else />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useSessionStore } from './stores/sessionStore'
import LoginView from './views/LoginView.vue'
import GameView from './views/GameView.vue'

const sessionStore = useSessionStore()

// 页面加载时检查登录状态
onMounted(() => {
  sessionStore.restoreSession()
})
</script>
