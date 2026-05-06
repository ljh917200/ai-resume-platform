import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'
import './styles/ink-transitions.css'
import App from './App.vue'
import router from './router'

// 引入自定义指令
import ripple from './directives/v-ripple'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 注册自定义指令
app.directive('ripple', ripple)

app.mount('#app')