<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="logo">
        <el-icon :size="32" color="#409eff">
          <component :is="LockIcon" />
        </el-icon>
        <span v-if="!isCollapse">网络安全防御系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
      >
        <menu-item
          v-for="route in menuRoutes"
          :key="route.path"
          :route="route"
        />
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon @click="toggleSidebar" class="fold-icon">
            <component :is="FoldIcon" v-if="!isCollapse" />
            <component :is="ExpandIcon" v-else />
          </el-icon>
        </div>
        
        <div class="header-right">
          <el-dropdown>
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.user?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view v-slot="{ Component, route }">
          <transition name="slide-fade" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, markRaw, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Fold, Expand, Lock, Monitor, Setting, DataLine, Tools, Guide } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import MenuItem from '@/components/MenuItem.vue'
import type { RouteRecordRaw } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 使用markRaw包装图标组件，防止响应式循环引用
const FoldIcon = markRaw(Fold)
const ExpandIcon = markRaw(Expand)
const LockIcon = markRaw(Lock)

const isCollapse = computed(() => !appStore.sidebar.opened)
const sidebarWidth = computed(() => isCollapse.value ? '64px' : '200px')
const activeMenu = computed(() => route.path)

// 缓存的页面组件名称
const cachedViews = ref<string[]>([])

// 监听路由变化，更新缓存的视图
watch(
  () => route.name,
  (newName) => {
    if (!newName || typeof newName !== 'string') return

    // 如果页面设置了noCache，从缓存中移除
    if (route.meta?.noCache === true) {
      cachedViews.value = cachedViews.value.filter(name => name !== newName)
      return
    }

    // 添加到缓存
    if (!cachedViews.value.includes(newName)) {
      cachedViews.value.push(newName)
    }

    // 限制最大缓存数量
    if (cachedViews.value.length > 10) {
      cachedViews.value.shift()
    }
  },
  { immediate: true }
)

const menuRoutes = computed(() => {
  const routes = (router.options.routes.find((r: RouteRecordRaw) => r.path === '/')?.children || []) as RouteRecordRaw[]

  // 按 parentTitle 分组
  const grouped = new Map<string, RouteRecordRaw[]>()
  const topLevel: RouteRecordRaw[] = []

  routes.forEach(route => {
    // 过滤需要 admin 权限的路由
    if (route.meta?.requiresAdmin && !userStore.hasRole('admin')) {
      return
    }
    
    const parentTitle = route.meta?.parentTitle as string
    if (parentTitle) {
      if (!grouped.has(parentTitle)) {
        grouped.set(parentTitle, [])
      }
      grouped.get(parentTitle)!.push(route)
    } else if (!route.meta?.hidden) {
      topLevel.push(route)
    }
  })

  // 构造菜单结构
  const menuData: RouteRecordRaw[] = [...topLevel]

  // 添加分组菜单
  const groupConfig = [
    { title: '安全监控', icon: markRaw(Monitor), path: 'security' },
    { title: '策略管理', icon: markRaw(Guide), path: 'policy' },
    { title: '报表分析', icon: markRaw(DataLine), path: 'report' },
    { title: '安全态势感知', icon: markRaw(Lock), path: 'security-situation' },
    { title: '系统管理', icon: markRaw(Tools), path: 'system-manage', requiresAdmin: true }
  ]

  groupConfig.forEach(config => {
    // 过滤需要 admin 权限的分组
    if (config.requiresAdmin && !userStore.hasRole('admin')) {
      return
    }
    
    const children = grouped.get(config.title)
    if (children && children.length > 0) {
      menuData.push({
        path: config.path,
        name: config.path,
        meta: { title: config.title, icon: config.icon },
        children
      } as RouteRecordRaw)
    }
  })

  return menuData
})

// 清理缓存的视图
const clearCache = () => {
  cachedViews.value = []
}

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const goToProfile = () => {
  router.push('/profile')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.sidebar {
  background: #ffffff;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: #333;
  font-size: 18px;
  font-weight: bold;
  gap: 10px;
}

.logo img {
  width: 32px;
  height: 32px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.fold-icon {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.fold-icon:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 滑动淡入淡出过渡效果 */
.slide-fade-enter-active {
  transition: all 0.2s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

/* 快速淡入淡出过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease-in-out;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
