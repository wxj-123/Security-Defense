import { http } from '@/utils/request'
import type { ApiResponse } from '@/types'

// 定义配置类型
interface SysConfig {
  id: number
  configKey: string
  configValue: string
  configName: string
  configType: string
  description: string
}

// 定义连接测试返回类型
interface ConnectionTestResult {
  status: boolean
  database?: string
  redis?: string
}

// 定义系统信息类型
interface SystemInfo {
  jvm: {
    maxMemory: number
    totalMemory: number
    usedMemory: number
    freeMemory: number
  }
  os: {
    name: string
    version: string
    arch: string
  }
  java: {
    version: string
    vendor: string
    home: string
  }
}

// 用户管理API
export const userApi = {
  // 创建用户
  createUser: (data: any) => {
    return http.post<boolean>('/system-manage/user/create', data)
  },

  // 更新用户
  updateUser: (data: any) => {
    return http.post<boolean>('/system-manage/user/update', data)
  },

  // 删除用户
  deleteUser: (userId: number) => {
    return http.delete<boolean>(`/system-manage/user/${userId}`)
  },

  // 重置密码
  resetPassword: (data: { userId: number; newPassword: string }) => {
    return http.post<boolean>('/system-manage/user/reset-password', data)
  },

  // 更新用户状态
  updateUserStatus: (data: { userId: number; status: number }) => {
    return http.post<boolean>('/system-manage/user/update-status', data)
  },

  // 分页查询用户
  queryUserPage: (data: any) => {
    return http.post<any>('/system-manage/user/page', data)
  },

  // 根据ID获取用户详情
  getUserById: (userId: number) => {
    return http.get<any>(`/system-manage/user/${userId}`)
  },

  // 获取用户角色ID列表
  getUserRoleIds: (userId: number) => {
    return http.get<number[]>(`/system-manage/user/${userId}/roles`)
  },

  // 分配用户角色
  assignUserRoles: (userId: number, roleIds: number[]) => {
    return http.post<boolean>(`/system-manage/user/${userId}/assign-roles`, roleIds)
  }
}

// 角色管理API
export const roleApi = {
  // 创建角色
  createRole: (data: any) => {
    return http.post<boolean>('/system-manage/role/create', data)
  },

  // 更新角色
  updateRole: (data: any) => {
    return http.post<boolean>('/system-manage/role/update', data)
  },

  // 删除角色
  deleteRole: (roleId: number) => {
    return http.delete<boolean>(`/system-manage/role/${roleId}`)
  },

  // 分页查询角色
  queryRolePage: (params: any) => {
    return http.get<any>('/system-manage/role/page', { params })
  },

  // 获取所有角色
  getAllRoles: () => {
    return http.get<any[]>('/system-manage/role/list')
  },

  // 根据ID获取角色详情
  getRoleById: (roleId: number) => {
    return http.get<any>(`/system-manage/role/${roleId}`)
  },

  // 获取角色权限ID列表
  getRolePermissionIds: (roleId: number) => {
    return http.get<number[]>(`/system-manage/role/${roleId}/permissions`)
  },

  // 分配角色权限
  assignRolePermissions: (roleId: number, permissionIds: number[]) => {
    return http.post<boolean>(`/system-manage/role/${roleId}/assign-permissions`, permissionIds)
  }
}

// 权限管理API
export const permissionApi = {
  // 获取权限树形结构
  getPermissionTree: () => {
    return http.get<any[]>('/system-manage/permission/tree')
  },

  // 获取用户权限编码集合
  getUserPermissions: (userId: number) => {
    return http.get<string[]>(`/system-manage/permission/user/${userId}/codes`)
  },

  // 获取用户菜单权限
  getUserMenus: (userId: number) => {
    return http.get<any[]>(`/system-manage/permission/user/${userId}/menus`)
  },

  // 检查用户是否有指定权限
  checkUserPermission: (userId: number, permissionCode: string) => {
    return http.get<boolean>(`/system-manage/permission/user/${userId}/check`, { params: { permissionCode } })
  }
}

// 系统配置API
export const configApi = {
  // 获取所有配置
  listConfig: () => {
    return http.get<SysConfig[]>('/system-manage/config/list')
  },

  // 根据类型获取配置
  getConfigByType: (configType: string) => {
    return http.get<SysConfig[]>(`/system-manage/config/type/${configType}`)
  },

  // 根据键获取配置值
  getConfigByKey: (configKey: string) => {
    return http.get<string>(`/system-manage/config/key/${configKey}`)
  },

  // 更新配置
  updateConfig: (data: any) => {
    return http.post<boolean>('/system-manage/config/update', data)
  }
}

// 系统信息API
export const systemApi = {
  // 测试数据库连接
  testDatabaseConnection: () => {
    return http.get<ConnectionTestResult>('/system-manage/connection/test/database')
  },

  // 测试Redis连接
  testRedisConnection: () => {
    return http.get<ConnectionTestResult>('/system-manage/connection/test/redis')
  },

  // 获取系统信息
  getSystemInfo: () => {
    return http.get<SystemInfo>('/system-manage/info')
  }
}

// 操作日志API
export const logApi = {
  // 分页查询操作日志
  queryOperationLogPage: (data: any) => {
    return http.post<any>('/system-manage/operation-log/page', data)
  },

  // 根据ID获取日志详情
  getLogById: (logId: number) => {
    return http.get<any>(`/system-manage/operation-log/${logId}`)
  },

  // 清理过期日志
  cleanExpiredLogs: (data: { retentionDays?: number }) => {
    return http.post<boolean>('/system-manage/operation-log/clean-expired', data)
  }
}

// 数据备份API
export const backupApi = {
  // 创建备份
  createBackup: (data: {
    backupName?: string;
    backupType?: string;
    description?: string;
  }) => {
    return http.post<boolean>('/system-manage/backup/create', data)
  },

  // 分页查询备份列表
  queryBackupPage: (params: {
    current?: number;
    size?: number;
  }) => {
    return http.get<any>('/system-manage/backup/page', { params })
  },

  // 恢复备份
  restoreBackup: (backupId: number) => {
    return http.post<boolean>(`/system-manage/backup/${backupId}/restore`)
  },

  // 删除备份
  deleteBackup: (backupId: number) => {
    return http.delete<boolean>(`/system-manage/backup/${backupId}`)
  },

  // 清理过期备份
  cleanExpiredBackups: (data: { retentionDays?: number }) => {
    return http.post<boolean>('/system-manage/backup/clean-expired', data)
  }
}