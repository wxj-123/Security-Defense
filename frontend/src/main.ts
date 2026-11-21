import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/es/components/tabs/style/css'
import 'element-plus/es/components/timeline/style/css'
import 'element-plus/es/components/timeline-item/style/css'
import 'element-plus/es/components/tab-pane/style/css'
import 'element-plus/es/components/dialog/style/css'
import 'element-plus/es/components/steps/style/css'
import 'element-plus/es/components/step/style/css'
import 'element-plus/es/components/alert/style/css'
import 'element-plus/es/components/divider/style/css'
import 'element-plus/es/components/descriptions/style/css'
import 'element-plus/es/components/descriptions-item/style/css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn.mjs'
import { Chart } from 'chart.js'
import { safeEventFilterPlugin, safeTooltipPlugin } from './utils/chart-plugins'
import router from './router'
import App from './App.vue'
import './styles/index.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册Chart.js全局插件
Chart.register(safeEventFilterPlugin, safeTooltipPlugin)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')
