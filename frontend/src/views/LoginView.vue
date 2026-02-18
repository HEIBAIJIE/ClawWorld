<template>
  <div class="login-form">
    <h1>ClawWorld</h1>
    <div class="form-group">
      <label>用户名</label>
      <input
        v-model="loginForm.username"
        type="text"
        class="sci-input"
        placeholder="输入用户名"
        @keyup.enter="handleLogin"
      />
    </div>
    <div class="form-group">
      <label>密码</label>
      <input
        v-model="loginForm.password"
        type="password"
        class="sci-input"
        placeholder="输入密码"
        @keyup.enter="handleLogin"
      />
    </div>
    <button
      class="sci-button primary"
      style="width: 100%"
      @click="handleLogin"
      :disabled="sessionStore.isLoading"
    >
      {{ sessionStore.isLoading ? '登录中...' : '登录 / 注册' }}
    </button>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useSessionStore } from '../stores/sessionStore'
import { useLogStore } from '../stores/logStore'
import { useCommand } from '../composables/useCommand'
import { gameApi } from '../api/game'

const sessionStore = useSessionStore()
const logStore = useLogStore()
const { processResponse } = useCommand()

const loginForm = ref({
  username: '',
  password: ''
})
const errorMessage = ref('')

const handleLogin = async () => {
  if (!loginForm.value.username.trim() || !loginForm.value.password.trim()) {
    errorMessage.value = '用户名和密码不能为空'
    return
  }

  sessionStore.isLoading = true
  errorMessage.value = ''

  try {
    const response = await gameApi.login(
      loginForm.value.username.trim(),
      loginForm.value.password
    )

    if (response.data.success) {
      // 保存会话
      sessionStore.setSession(response.data.sessionId, loginForm.value.username.trim())

      // 设置游戏文本
      let initialText = response.data.backgroundPrompt || '欢迎来到 ClawWorld！'
      if (response.data.windowContent) {
        initialText += '\n\n' + response.data.windowContent
      }
      logStore.setRawText(initialText)

      // 解析初始数据
      processResponse(initialText)
    } else {
      errorMessage.value = response.data.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '网络错误，请检查服务器是否启动'
  } finally {
    sessionStore.isLoading = false
  }
}
</script>
