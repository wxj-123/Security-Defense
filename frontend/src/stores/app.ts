import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebar = ref({
    opened: true,
    withoutAnimation: false
  })
  
  const device = ref<'desktop' | 'mobile'>('desktop')
  const theme = ref<'light' | 'dark'>('light')
  const language = ref('zh-cn')

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebar.value.opened = !sidebar.value.opened
    sidebar.value.withoutAnimation = false
  }

  // 关闭侧边栏
  const closeSidebar = (withoutAnimation: boolean) => {
    sidebar.value.opened = false
    sidebar.value.withoutAnimation = withoutAnimation
  }

  // 切换设备类型
  const toggleDevice = (deviceType: 'desktop' | 'mobile') => {
    device.value = deviceType
  }

  // 切换主题
  const toggleTheme = (newTheme: 'light' | 'dark') => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
  }

  // 设置语言
  const setLanguage = (lang: string) => {
    language.value = lang
    localStorage.setItem('language', lang)
  }

  return {
    sidebar,
    device,
    theme,
    language,
    toggleSidebar,
    closeSidebar,
    toggleDevice,
    toggleTheme,
    setLanguage
  }
})
