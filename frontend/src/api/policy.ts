import { http } from '@/utils/request'
import type { FirewallRule, SecurityPolicy, PageQuery, PageResult, PolicyTemplate } from '@/types'

// 防火墙规则管理
export const getFirewallRules = (params: PageQuery) => {
  return http.get<PageResult<FirewallRule>>('/policy/firewall-rules', { params })
}

export const addFirewallRule = (data: Partial<FirewallRule>) => {
  return http.post('/policy/firewall-rules', data)
}

export const updateFirewallRule = (id: number, data: Partial<FirewallRule>) => {
  return http.put(`/policy/firewall-rules/${id}`, data)
}

export const deleteFirewallRule = (id: number) => {
  return http.delete(`/policy/firewall-rules/${id}`)
}

export const toggleFirewallRule = (id: number, enabled: boolean) => {
  return http.put(`/policy/firewall-rules/${id}/toggle`, { enabled })
}

// 安全策略管理
export const getSecurityPolicies = (params: PageQuery) => {
  return http.get<PageResult<SecurityPolicy>>('/policy/security-policies', { params })
}

export const addSecurityPolicy = (data: Partial<SecurityPolicy>) => {
  return http.post('/policy/security-policies', data)
}

export const updateSecurityPolicy = (id: number, data: Partial<SecurityPolicy>) => {
  return http.put(`/policy/security-policies/${id}`, data)
}

export const deleteSecurityPolicy = (id: number) => {
  return http.delete(`/policy/security-policies/${id}`)
}

// 策略模板
export const getPolicyTemplates = () => {
  return http.get<PolicyTemplate[]>('/policy/templates')
}

export const applyPolicyTemplate = (templateId: number, data: any) => {
  return http.post(`/policy/templates/${templateId}/apply`, data)
}