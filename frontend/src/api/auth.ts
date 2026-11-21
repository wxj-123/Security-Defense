import { http } from '@/utils/request'
import type { LoginForm, User, TwoFactorAuth } from '@/types'

// 用户登录
export const login = (data: LoginForm) => {
  return http.post<{ token: string; user: User; requireTwoFactor?: boolean; tempToken?: string }>('/auth/login', data)
}

// 双因素认证验证
export const verifyTwoFactor = (data: TwoFactorAuth) => {
  return http.post<{ token: string }>('/auth/2fa/verify', data)
}

// 用户登出
export const logout = () => {
  return http.post('/auth/logout')
}

// 获取用户信息
export const getUserInfo = () => {
  return http.get<User>('/auth/user/info')
}

// 刷新Token
export const refreshToken = () => {
  return http.post<{ token: string }>('/auth/refresh')
}

// 获取CSRF Token
export const getCsrfToken = () => {
  return http.get<{ token: string }>('/auth/csrf-token')
}
