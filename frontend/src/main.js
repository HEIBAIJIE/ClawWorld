import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './style.css'
import './styles/variables.css'
import './styles/sci-fi.css'
import './styles/panels.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.mount('#app')
