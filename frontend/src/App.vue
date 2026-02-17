<template>
  <div id="app">
    <!-- 登录界面 -->
    <div v-if="!isLoggedIn" class="login-form">
      <h1>ClawWorld</h1>
      <div class="form-group">
        <label>用户名</label>
        <input
          v-model="loginForm.username"
          type="text"
          placeholder="输入用户名"
          @keyup.enter="handleLogin"
        />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input
          v-model="loginForm.password"
          type="password"
          placeholder="输入密码"
          @keyup.enter="handleLogin"
        />
      </div>
      <button
        class="command-button"
        style="width: 100%"
        @click="handleLogin"
        :disabled="isLoading"
      >
        {{ isLoading ? '登录中...' : '登录 / 注册' }}
      </button>
      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    </div>

    <!-- 游戏界面 -->
    <div v-else class="container">
      <!-- 标题栏 -->
      <div class="header">
        <h1>ClawWorld</h1>
        <button class="command-button logout-button" @click="handleLogout">
          登出
        </button>
      </div>

      <!-- 游戏文本框 -->
      <div class="game-content-wrapper">
        <div class="game-content">
          <div class="game-text" v-html="formattedGameText"></div>
        </div>
        <div class="token-counter">
          <span class="token-label">约</span>
          <span class="token-count">{{ formattedTokenCount }}</span>
          <span class="token-label">tokens</span>
        </div>
      </div>

      <!-- 指令输入 -->
      <div class="command-section">
        <div class="command-input-wrapper">
          <input
            v-model="commandInput"
            class="command-input"
            type="text"
            placeholder="输入指令 (例如: register 战士 张三, move 5 5, say map 你好)"
            @keyup.enter="handleSendCommand"
            :disabled="isWaiting"
          />
          <button
            class="command-button"
            @click="handleSendCommand"
            :disabled="isWaiting || !commandInput.trim()"
          >
            {{ isWaiting ? '等待响应...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { gameApi } from './api/game'
import { estimateTokenCount, formatTokenCount } from './utils/tokenCounter'

// 登录状态
const isLoggedIn = ref(false)
const isLoading = ref(false)
const isWaiting = ref(false)
const errorMessage = ref('')

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 游戏状态
const gameText = ref('')
const commandInput = ref('')
const sessionId = ref('')

// 登录处理
const handleLogin = async () => {
  if (!loginForm.value.username.trim() || !loginForm.value.password.trim()) {
    errorMessage.value = '用户名和密码不能为空'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await gameApi.login(
      loginForm.value.username.trim(),
      loginForm.value.password
    )

    if (response.data.success) {
      // 保存会话ID
      sessionId.value = response.data.sessionId
      localStorage.setItem('sessionId', response.data.sessionId)

      // 设置游戏文本（背景提示 + 窗口内容）
      let initialText = response.data.backgroundPrompt || '欢迎来到 ClawWorld！'

      if (response.data.windowContent) {
        initialText += '\n\n' + response.data.windowContent
      }

      gameText.value = initialText

      // 登录成功
      isLoggedIn.value = true
    } else {
      errorMessage.value = response.data.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '网络错误，请检查服务器是否启动'
  } finally {
    isLoading.value = false
  }
}

// 登出处理
const handleLogout = async () => {
  try {
    await gameApi.logout(sessionId.value)
  } catch (error) {
    console.error('登出失败:', error)
  } finally {
    localStorage.removeItem('sessionId')
    sessionId.value = ''
    isLoggedIn.value = false
    gameText.value = ''
    commandInput.value = ''
  }
}

// 发送指令
const handleSendCommand = async () => {
  if (!commandInput.value.trim() || isWaiting.value) {
    return
  }

  const command = commandInput.value.trim()
  isWaiting.value = true

  // 添加用户输入到游戏文本
  gameText.value += '\n\n> ' + command + '\n'

  try {
    const response = await gameApi.executeCommand(sessionId.value, command)

    // 添加服务器响应到游戏文本
    if (response.data.response) {
      gameText.value += '\n' + response.data.response
    }

    // 清空输入框
    commandInput.value = ''
  } catch (error) {
    gameText.value += (error.response?.data?.response || error.message || '网络错误')
  } finally {
    isWaiting.value = false
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  const storedSessionId = localStorage.getItem('sessionId')
  if (storedSessionId) {
    // 如果有sessionId，尝试恢复会话
    sessionId.value = storedSessionId
    isLoggedIn.value = true
    gameText.value = '会话已恢复，请重新登录以获取完整背景信息。'
  }
})

// 计算token数量
const tokenCount = computed(() => {
  return estimateTokenCount(gameText.value)
})

// 格式化token数量显示
const formattedTokenCount = computed(() => {
  return formatTokenCount(tokenCount.value)
})

// 格式化游戏文本，添加颜色和高亮
const formattedGameText = computed(() => {
  if (!gameText.value) return ''

  // 将文本按行分割
  const lines = gameText.value.split('\n')
  const formattedLines = lines.map(line => {
    // 解析日志格式: [来源][时间][类型][子类型]内容
    const logPattern = /^\[([^\]]+)\]\[([^\]]+)\]\[([^\]]+)\]\[([^\]]+)\](.*)$/
    const match = line.match(logPattern)

    if (match) {
      const [, source, time, type, subType, content] = match

      // 根据来源设置颜色
      let sourceClass = 'log-source'
      if (source === '服务器') {
        sourceClass += ' log-source-server'
      } else if (source === '客户端') {
        sourceClass += ' log-source-client'
      }

      // 根据类型设置颜色
      let typeClass = 'log-type'
      if (type === '背景') {
        typeClass += ' log-type-background'
      } else if (type === '窗口') {
        typeClass += ' log-type-window'
      } else if (type === '状态') {
        typeClass += ' log-type-state'
      } else if (type === '指令') {
        typeClass += ' log-type-command'
      }

      // 根据子类型设置颜色
      let subTypeClass = 'log-subtype'
      if (subType === '环境变化') {
        subTypeClass += ' log-subtype-env'
      } else if (subType === '指令响应') {
        subTypeClass += ' log-subtype-response'
      } else if (subType === '窗口变化') {
        subTypeClass += ' log-subtype-window-change'
      }

      return `<div class="log-line">` +
        `<span class="${sourceClass}">[${source}]</span>` +
        `<span class="log-time">[${time}]</span>` +
        `<span class="${typeClass}">[${type}]</span>` +
        `<span class="${subTypeClass}">[${subType}]</span>` +
        `<span class="log-content">${escapeHtml(content)}</span>` +
        `</div>`
    }

    // 如果不是日志格式，检查是否是用户输入
    if (line.startsWith('> ')) {
      return `<div class="user-input">${escapeHtml(line)}</div>`
    }

    // 普通文本
    return `<div class="plain-text">${escapeHtml(line)}</div>`
  })

  return formattedLines.join('')
})

// HTML转义函数
function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}
</script>
