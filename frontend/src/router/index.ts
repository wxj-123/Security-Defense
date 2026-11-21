import { createRouter, createWebHistory, RouteRecordRaw, NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Odometer,
  Lock,
  PieChart,
  Compass,
  Aim,
  Histogram,
  Warning,
  Connection,
  Document,
  Key,
  Tickets,
  Finished,
  User,
  DataLine,
  Monitor,
  Setting,
  DocumentChecked,
  Edit,
  Tools,
  Files,
  DataBoard,
  Notebook,
  SetUp,
  Star,
  InfoFilled
} from '@element-plus/icons-vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '首页', icon: Odometer }
      },
      {
        path: '/security-situation',
        redirect: '/security-situation/dashboard',
        meta: {
          hidden: true
        }
      },
      {
        path: '/security-situation/dashboard',
        name: 'SecuritySituationDashboard',
        component: () => import('@/views/SecuritySituation/Dashboard.vue'),
        meta: {
          title: '安全态势总览',
          icon: InfoFilled,
          parentTitle: '安全态势感知'
        }
      },
      {
        path: '/security-situation/asset-risk-distribution',
        name: 'AssetRiskDistribution',
        component: () => import('@/views/SecuritySituation/AssetRiskDistribution.vue'),
        meta: {
          title: '资产风险分布',
          icon: PieChart,
          parentTitle: '安全态势感知'
        }
      },
      {
        path: '/security-situation/threat-intelligence',
        name: 'ThreatIntelligenceOverview',
        component: () => import('@/views/SecuritySituation/ThreatIntelligence.vue'),
        meta: {
          title: '威胁情报概览',
          icon: Compass,
          parentTitle: '安全态势感知'
        }
      },
      {
        path: '/security-situation/dimension-analysis',
        name: 'DimensionAnalysis',
        component: () => import('@/views/SecuritySituation/DimensionAnalysis.vue'),
        meta: {
          title: '安全维度分析',
          icon: Aim,
          parentTitle: '安全态势感知'
        }
      },
      {
        path: '/security-situation/risk-heatmap',
        name: 'RiskHeatmap',
        component: () => import('@/views/SecuritySituation/RiskHeatmap.vue'),
        meta: {
          title: '资产风险热力图',
          icon: Histogram,
          parentTitle: '安全态势感知'
        }
      },
      {
        path: '/security-situation/asset/:assetId',
        name: 'AssetDetail',
        component: () => import('@/views/SecuritySituation/AssetDetail.vue'),
        meta: {
          title: '资产详情',
          hidden: true
        }
      },
      {
        path: '/security-situation/threat/:threatId',
        name: 'ThreatIntelligenceDetail',
        component: () => import('@/views/SecuritySituation/ThreatIntelligenceDetail.vue'),
        meta: {
          title: '威胁情报详情',
          hidden: true
        }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: User, hidden: true }
      },
      {
        path: '/security/events',
        name: 'SecurityEvents',
        component: () => import('@/views/Security/Events.vue'),
        meta: { title: '安全事件', icon: Warning, parentTitle: '安全监控' }
      },
      {
        path: '/security/network-flow',
        name: 'NetworkFlow',
        component: () => import('@/views/Security/NetworkFlow.vue'),
        meta: { title: '网络流量', icon: Connection, parentTitle: '安全监控' }
      },
      {
        path: '/security/logs',
        name: 'SecurityLogs',
        component: () => import('@/views/Security/Logs.vue'),
        meta: { title: '实时日志', icon: Document, parentTitle: '安全监控' }
      },
      {
        path: '/security/anomaly',
        name: 'AnomalyDetection',
        component: () => import('@/views/Security/Anomaly.vue'),
        meta: { title: '异常检测', icon: Aim, parentTitle: '安全监控' }
      },
      {
        path: '/security/attack-events',
        name: 'AttackEvents',
        component: () => import('@/views/Security/AttackEvents.vue'),
        meta: { title: '攻击事件监控', icon: SetUp, parentTitle: '安全监控' }
      },
      {
        path: '/policy/firewall',
        name: 'FirewallRules',
        component: () => import('@/views/Policy/Firewall.vue'),
        meta: { title: '防火墙规则', icon: Notebook, parentTitle: '策略管理' }
      },
      {
        path: '/policy/acl',
        name: 'AccessControl',
        component: () => import('@/views/Policy/ACL.vue'),
        meta: { title: '访问控制', icon: Key, parentTitle: '策略管理' }
      },
      {
        path: '/policy/templates',
        name: 'PolicyTemplates',
        component: () => import('@/views/Policy/Templates.vue'),
        meta: { title: '策略模板', icon: Tickets, parentTitle: '策略管理' }
      },
      {
        path: '/policy/workflow',
        name: 'PolicyWorkflow',
        component: () => import('@/views/Policy/Workflow.vue'),
        meta: { title: '审批工作流', icon: Finished, parentTitle: '策略管理' }
      },
      {
        path: '/report/statistics',
        name: 'EventStatistics',
        component: () => import('@/views/Report/Statistics.vue'),
        meta: { title: '事件统计', icon: PieChart, parentTitle: '报表分析' }
      },
      {
        path: '/report/threat',
        name: 'ThreatIntelligence',
        component: () => import('@/views/Report/Threat.vue'),
        meta: { title: '威胁情报', icon: Compass, parentTitle: '报表分析' }
      },
      {
        path: '/report/compliance',
        name: 'ComplianceReport',
        component: () => import('@/views/Report/Compliance.vue'),
        meta: { title: '合规性报告', icon: DocumentChecked, parentTitle: '报表分析' }
      },
      {
        path: '/report/custom',
        name: 'CustomReport',
        component: () => import('@/views/Report/Custom.vue'),
        meta: { title: '自定义报表', icon: Edit, parentTitle: '报表分析' }
      },
      {
        path: '/system-manage',
        redirect: '/system-manage/user',
        meta: { hidden: true, requiresAdmin: true }
      },
      {
        path: '/system-manage/user',
        name: 'UserManage',
        component: () => import('@/views/SystemManage/UserManage.vue'),
        meta: { 
          title: '用户管理', 
          icon: User, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      },
      {
        path: '/system-manage/role',
        name: 'RoleManage',
        component: () => import('@/views/SystemManage/RoleManage.vue'),
        meta: { 
          title: '角色管理', 
          icon: Star, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      },
      {
        path: '/system-manage/permission',
        name: 'PermissionManage',
        component: () => import('@/views/SystemManage/PermissionManage.vue'),
        meta: { 
          title: '权限管理', 
          icon: Key, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      },
      {
        path: '/system-manage/config',
        name: 'SystemConfig',
        component: () => import('@/views/SystemManage/SystemConfig.vue'),
        meta: { 
          title: '系统配置', 
          icon: Setting, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      },
      {
        path: '/system-manage/log',
        name: 'OperationLog',
        component: () => import('@/views/SystemManage/OperationLog.vue'),
        meta: { 
          title: '操作日志', 
          icon: Files, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      },
      {
        path: '/system-manage/backup',
        name: 'DataBackup',
        component: () => import('@/views/SystemManage/DataBackup.vue'),
        meta: { 
          title: '数据备份', 
          icon: DataBoard, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      },
      {
        path: '/system-manage/info',
        name: 'SystemInfo',
        component: () => import('@/views/SystemManage/SystemInfo.vue'),
        meta: { 
          title: '系统信息', 
          icon: Monitor, 
          parentTitle: '系统管理',
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to: RouteLocationNormalized, _from: RouteLocationNormalized, next: NavigationGuardNext) => {
  const userStore = useUserStore()

  // 开发环境日志
  if (import.meta.env.DEV) {
    console.log('路由守卫 - 目标路由:', to.path, 'Token:', userStore.token ? '存在' : '不存在')
  }

  // 设置页面标题
  document.title = `${to.meta.title || '网络安全防御系统'} - 网络安全防御系统`

  // 白名单路由
  const whiteList = ['/login']

  if (userStore.token) {
    if (import.meta.env.DEV) console.log('用户已登录')

    if (to.path === '/login') {
      if (import.meta.env.DEV) console.log('已登录用户访问登录页，重定向到首页')
      next({ path: '/' })
    } else {
      // 检查是否需要 admin 角色
      if (to.meta.requiresAdmin && !userStore.hasRole('admin')) {
        if (import.meta.env.DEV) console.log('非管理员用户访问系统管理，拒绝访问')
        ElMessage.error('您没有权限访问系统管理功能')
        next({ path: '/' })
        return
      }

      // 检查是否已获取用户信息
      if (!userStore.user) {
        if (import.meta.env.DEV) console.log('需要获取用户信息')
        try {
          await userStore.fetchUserInfo()
          // 获取用户信息后再次检查 admin 角色
          if (to.meta.requiresAdmin && !userStore.hasRole('admin')) {
            if (import.meta.env.DEV) console.log('非管理员用户访问系统管理，拒绝访问')
            ElMessage.error('您没有权限访问系统管理功能')
            next({ path: '/' })
            return
          }
          if (import.meta.env.DEV) console.log('用户信息获取成功')
          next()
        } catch (error) {
          // Token失效，清除并跳转登录
          console.error('获取用户信息失败:', error)
          userStore.logout()
          ElMessage.error('登录已过期，请重新登录')
          next(`/login?redirect=${to.path}`)
        }
      } else {
        if (import.meta.env.DEV) console.log('用户信息已存在，直接放行')
        next()
      }
    }
  } else {
    if (import.meta.env.DEV) console.log('用户未登录')
    // 未登录
    if (whiteList.includes(to.path)) {
      if (import.meta.env.DEV) console.log('访问白名单路由，直接放行')
      next()
    } else {
      if (import.meta.env.DEV) console.log('需要登录，重定向到登录页')
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
