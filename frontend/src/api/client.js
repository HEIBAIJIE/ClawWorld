import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  timeout: 120000, // 2分钟超时，考虑到战斗可能需要等待
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：记录请求日志
client.interceptors.request.use(
  config => {
    console.log('[API] 发送请求:', config.method?.toUpperCase(), config.url, config.data)
    return config
  },
  error => {
    console.error('[API] 请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：处理错误和记录日志
client.interceptors.response.use(
  response => {
    console.log('[API] 收到响应:', response.config.url, response.status, response.data)
    return response
  },
  error => {
    console.error('[API] 请求失败:', error.config?.url, error.response?.status, error.message)
    if (error.response?.status === 401) {
      console.warn('[API] 401未授权，清除会话并刷新页面')
      localStorage.removeItem('sessionId')
      window.location.reload()
    }
    return Promise.reject(error)
  }
)

export default client
