<template>
  <div class="log-panel">
    <!-- 日志显示区 -->
    <div class="log-display sci-panel sci-scrollbar" ref="logDisplayRef">
      <div class="game-text" v-html="formattedGameText"></div>
    </div>

    <!-- Token计数器 -->
    <div class="token-counter">
      <span class="token-label">约</span>
      <span class="token-count">{{ logStore.formattedTokenCount }}</span>
      <span class="token-label">tokens</span>
    </div>

    <!-- 指令输入区 -->
    <div class="command-input-section">
      <input
        v-model="commandInput"
        class="sci-input"
        type="text"
        placeholder="输入指令..."
        @keyup.enter="handleSendCommand"
        :disabled="sessionStore.isWaiting"
      />
      <button
        class="sci-button primary"
        @click="handleSendCommand"
        :disabled="sessionStore.isWaiting || !commandInput.trim()"
      >
        {{ sessionStore.isWaiting ? '等待...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useSessionStore } from '../../stores/sessionStore'
import { useLogStore } from '../../stores/logStore'
import { useCommand } from '../../composables/useCommand'

const sessionStore = useSessionStore()
const logStore = useLogStore()
const { sendCommand } = useCommand()

const commandInput = ref('')
const logDisplayRef = ref(null)

// 发送指令
const handleSendCommand = async () => {
  if (!commandInput.value.trim() || sessionStore.isWaiting) {
    return
  }

  const command = commandInput.value.trim()
  commandInput.value = ''
  await sendCommand(command)
}

// 格式化游戏文本
const formattedGameText = computed(() => {
  if (!logStore.rawText) return ''

  const lines = logStore.rawText.split('\n')
  const formattedLines = lines.map(line => {
    // 解析日志格式: [来源][时间][类型][子类型]内容
    const logPattern = /^\[([^\]]+)\]\[([^\]]+)\]\[([^\]]+)\]\[([^\]]+)\](.*)$/
    const match = line.match(logPattern)

    if (match) {
      const [, source, time, type, subType, content] = match

      let sourceClass = 'log-source'
      if (source === '服务端') sourceClass += ' log-source-server'
      else if (source === '你') sourceClass += ' log-source-client'

      let typeClass = 'log-type'
      if (type === '背景') typeClass += ' log-type-background'
      else if (type === '窗口') typeClass += ' log-type-window'
      else if (type === '状态') typeClass += ' log-type-state'
      else if (type === '发送指令') typeClass += ' log-type-command'

      return `<div class="log-line">` +
        `<span class="${sourceClass}">[${source}]</span>` +
        `<span class="log-time">[${time}]</span>` +
        `<span class="${typeClass}">[${type}]</span>` +
        `<span class="log-subtype">[${subType}]</span>` +
        `<span class="log-content">${escapeHtml(content)}</span>` +
        `</div>`
    }

    if (line.startsWith('> ')) {
      return `<div class="user-input">${escapeHtml(line)}</div>`
    }

    return `<div class="plain-text">${escapeHtml(line)}</div>`
  })

  return formattedLines.join('')
})

// HTML转义
function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 自动滚动到底部
watch(() => logStore.rawText, async () => {
  await nextTick()
  if (logDisplayRef.value) {
    logDisplayRef.value.scrollTop = logDisplayRef.value.scrollHeight
  }
})
</script>

<style scoped>
.log-panel {
  display: flex;
  flex-direction: column;
  position: relative;
}

.log-display {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.6;
}

.token-counter {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(37, 37, 37, 0.9);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.token-label {
  color: var(--text-muted);
}

.token-count {
  color: var(--primary);
  font-weight: 600;
}

.command-input-section {
  margin-top: 12px;
}

.command-input-section input {
  width: 100%;
  margin-bottom: 8px;
}

.command-input-section button {
  width: 100%;
}
</style>
