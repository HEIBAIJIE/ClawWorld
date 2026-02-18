import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSessionStore = defineStore('session', () => {
  const sessionId = ref('')
  const isLoggedIn = ref(false)
  const isLoading = ref(false)
  const isWaiting = ref(false)
  const username = ref('')

  function setSession(id, user) {
    console.log('[SessionStore] 设置会话:', { sessionId: id, username: user })
    sessionId.value = id
    username.value = user
    isLoggedIn.value = true
    localStorage.setItem('sessionId', id)
    localStorage.setItem('username', user)
  }

  function clearSession() {
    console.log('[SessionStore] 清除会话')
    sessionId.value = ''
    username.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('sessionId')
    localStorage.removeItem('username')
  }

  function restoreSession() {
    const storedSessionId = localStorage.getItem('sessionId')
    const storedUsername = localStorage.getItem('username')
    console.log('[SessionStore] 尝试恢复会话:', { storedSessionId, storedUsername })
    if (storedSessionId) {
      sessionId.value = storedSessionId
      username.value = storedUsername || ''
      isLoggedIn.value = true
      console.log('[SessionStore] 会话恢复成功')
      return true
    }
    console.log('[SessionStore] 无可恢复的会话')
    return false
  }

  return {
    sessionId,
    isLoggedIn,
    isLoading,
    isWaiting,
    username,
    setSession,
    clearSession,
    restoreSession
  }
})
