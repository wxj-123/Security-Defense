import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import { logout as logoutApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])

  // 登录
  const login = async (loginForm: any) => {
    try {
      console.log('[UserStore] 开始登录流程')
      // ==== 临时Mock登录方案 ====
      // 验证用户名和密码
      const validAccounts: Record<string, string> = {
        'admin': 'admin123',
        'operator': 'admin123',
        'viewer': 'admin123'
      }
      
      if (validAccounts[loginForm.username] !== loginForm.password) {
        throw new Error('用户名或密码错误')
      }
      
      // 生成 Mock Token
      const mockToken = 'mock_token_' + loginForm.username + '_' + Date.now()
      
      // 保存token
      token.value = mockToken
      localStorage.setItem('token', mockToken)
      
      // 从后端获取真实用户信息（包括头像）
      try {
        await fetchUserInfo()
        console.log('[UserStore] 从后端获取用户信息成功')
      } catch (error) {
        console.warn('[UserStore] 获取用户信息失败，使用默认数据:', error)
        // 如果后端获取失败，使用默认数据
        user.value = {
          id: loginForm.username === 'admin' ? 1 : 2,
          username: loginForm.username,
          email: loginForm.username + '@security.com',
          avatar: '/uploads/avatars/default.png',
          permissions: loginForm.username === 'admin' ? ['*'] : ['read'],
          roles: loginForm.username === 'admin' ? ['admin'] : ['user']
        }
        permissions.value = user.value.permissions
        roles.value = user.value.roles
      }
      
      console.log('[UserStore] 登录成功, user:', user.value)
      
      return { data: { token: mockToken, user: user.value } }
    } catch (error) {
      console.error('[UserStore] 登录失败:', error)
      return Promise.reject(error)
    }
  }

  // 登出
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      user.value = null
      permissions.value = []
      roles.value = []
      localStorage.removeItem('token')
      sessionStorage.clear()
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      user.value = res.data
      permissions.value = res.data.permissions
      roles.value = res.data.roles
      return res.data
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 检查权限
  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission) || permissions.value.includes('*')
  }

  // 检查角色
  const hasRole = (role: string) => {
    return roles.value.includes(role) || roles.value.includes('admin')
  }

  // 更新用户头像
  const updateAvatar = (avatarUrl: string) => {
    if (user.value) {
      user.value.avatar = avatarUrl
    }
  }

  // 更新用户信息
  const updateUserInfo = (userInfo: Partial<User>) => {
    if (user.value) {
      Object.assign(user.value, userInfo)
    }
  }

  return {
    token,
    user,
    permissions,
    roles,
    login,
    logout,
    fetchUserInfo,
    hasPermission,
    hasRole,
    updateAvatar,
    updateUserInfo
  }
})
