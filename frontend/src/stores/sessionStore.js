import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSessionStore = defineStore('session', () => {
  const sessionId = ref('')
  const isLoggedIn = ref(false)
  const isLoading = ref(false)
  const isWaiting = ref(false)
  const username = ref('')

  function setSession(id, user) {
    sessionId.value = id
    username.value = user
    isLoggedIn.value = true
    localStorage.setItem('sessionId', id)
    localStorage.setItem('username', user)
  }

  function clearSession() {
    sessionId.value = ''
    username.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('sessionId')
    localStorage.removeItem('username')
  }

  function restoreSession() {
    const storedSessionId = localStorage.getItem('sessionId')
    const storedUsername = localStorage.getItem('username')
    if (storedSessionId) {
      sessionId.value = storedSessionId
      username.value = storedUsername || ''
      isLoggedIn.value = true
      return true
    }
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
