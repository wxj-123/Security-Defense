import { http } from '@/utils/request'
import type { DashboardStats, SecurityEvent } from '@/types'

// 获取首页统计数据
export const getDashboardStats = () => {
  return http.get<DashboardStats>('/dashboard/stats')
}

// 获取最近安全事件
export const getRecentEvents = (limit = 10) => {
  return http.get<SecurityEvent[]>('/dashboard/recent-events', {
    params: { limit }
  })
}

// 获取攻击流量趋势
export const getAttackTrend = (period: '1h' | '6h' | '24h' | '7d' = '24h') => {
  return http.get('/dashboard/attack-trend', {
    params: { period }
  })
}

// 获取威胁分布
export const getThreatDistribution = () => {
  return http.get('/dashboard/threat-distribution')
}

// 获取系统健康状态
export const getSystemHealth = () => {
  return http.get('/dashboard/system-health')
}

// 获取网络流量统计
export const getNetworkTraffic = (timeRange: string) => {
  return http.get('/dashboard/network-traffic', {
    params: { timeRange }
  })
}
