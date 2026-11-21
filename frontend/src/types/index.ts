// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export interface LoginForm {
  username: string
  password: string
  code?: string
  remember?: boolean
}

export interface TwoFactorAuth {
  code: string
  token: string
}

// 安全事件类型
export interface SecurityEvent {
  id: number
  type?: string
  eventType?: string
  level?: 'critical' | 'high' | 'medium' | 'low'
  eventLevel?: string
  source: string
  sourceIp?: string
  target: string
  targetIp?: string
  description: string
  timestamp: string
  eventTime?: string
  status: 'pending' | 'processing' | 'resolved' | 'ignored'
  targetPort?: number
  sourcePort?: number
  protocol?: string
  payload?: string
  duration?: number
}

// 网络流量类型
export interface NetworkFlow {
  id: number
  sourceIp: string
  targetIp: string
  sourcePort: number
  targetPort: number
  protocol: string
  bytes: number
  packets: number
  timestamp: string
  threat: boolean
}

// 防火墙规则类型
export interface FirewallRule {
  id: number
  name: string
  action: 'allow' | 'deny' | 'drop'
  sourceIp: string
  targetIp: string
  port: string
  protocol: string
  priority: number
  enabled: boolean
  createTime: string
}

// 安全策略类型
export interface SecurityPolicy {
  id: number
  name: string
  type: string
  description: string
  rules: any[]
  enabled: boolean
  createTime: string
  updateTime: string
}

// 策略模板类型
export interface PolicyTemplate {
  id: number
  name: string
  category: string
  description: string
  ruleCount: number
  rules: Array<{
    name: string
    type: string
    action: string
    description: string
  }>
  official?: boolean
  useCount?: number
}

// 告警规则类型
export interface AlertRule {
  id: number
  name: string
  condition: string
  threshold: number
  level: 'critical' | 'high' | 'medium' | 'low'
  enabled: boolean
  notifyChannels: string[]
}

// 首页统计数据
export interface DashboardStats {
  totalEvents: number
  criticalEvents: number
  blockedAttacks: number
  systemHealth: number
  networkTraffic: {
    inbound: number
    outbound: number
  }
}

// 图表数据类型
export interface ChartData {
  labels: string[]
  datasets: {
    label: string
    data: number[]
    backgroundColor?: string
    borderColor?: string
  }[]
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页类型
export interface PageQuery {
  page: number
  pageSize: number
  keyword?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}