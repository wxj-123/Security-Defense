<template>
  <div class="permission-manage">
    <div class="header">
      <div class="title">权限管理</div>
      <el-button type="primary" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        v-loading="tableLoading"
        :data="permissionTree"
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        :style="{ width: '100%' }"
      >
        <el-table-column prop="permissionName" label="权限名称" min-width="200" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="200" />
        <el-table-column prop="resourceType" label="资源类型" width="120">
          <template #default="scope">
            <el-tag :type="getResourceTypeColor(scope.row.resourceType)">
              {{ getResourceTypeText(scope.row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourceUrl" label="资源路径" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { permissionApi } from '@/api/system-manage'

// 响应式数据
const tableLoading = ref(false)
const permissionTree = ref<any[]>([])

// 模拟权限数据
const mockPermissions = [
  {
    id: 100,
    permissionName: '系统管理',
    permissionCode: '',
    resourceType: 'menu',
    resourceUrl: '/system-manage',
    parentId: 0,
    sort: 1,
    icon: 'tools',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 101,
    permissionName: '用户管理',
    permissionCode: 'system:user',
    resourceType: 'menu',
    resourceUrl: '/system-manage/user',
    parentId: 100,
    sort: 1,
    icon: 'user',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 102,
    permissionName: '用户查看',
    permissionCode: 'system:user:view',
    resourceType: 'button',
    resourceUrl: '',
    parentId: 101,
    sort: 1,
    icon: '',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 103,
    permissionName: '用户新增',
    permissionCode: 'system:user:create',
    resourceType: 'button',
    resourceUrl: '',
    parentId: 101,
    sort: 2,
    icon: '',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 104,
    permissionName: '用户编辑',
    permissionCode: 'system:user:update',
    resourceType: 'button',
    resourceUrl: '',
    parentId: 101,
    sort: 3,
    icon: '',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 110,
    permissionName: '角色管理',
    permissionCode: 'system:role',
    resourceType: 'menu',
    resourceUrl: '/system-manage/role',
    parentId: 100,
    sort: 2,
    icon: 'tools',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 111,
    permissionName: '角色查看',
    permissionCode: 'system:role:view',
    resourceType: 'button',
    resourceUrl: '',
    parentId: 110,
    sort: 1,
    icon: '',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 120,
    permissionName: '权限管理',
    permissionCode: 'system:permission',
    resourceType: 'menu',
    resourceUrl: '/system-manage/permission',
    parentId: 100,
    sort: 3,
    icon: 'key',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  },
  {
    id: 121,
    permissionName: '权限查看',
    permissionCode: 'system:permission:view',
    resourceType: 'button',
    resourceUrl: '',
    parentId: 120,
    sort: 1,
    icon: '',
    status: 1,
    createTime: '2025-11-23 12:00:00'
  }
]

// 初始化
onMounted(() => {
  loadPermissionTree()
})

// 加载权限树
const loadPermissionTree = () => {
  tableLoading.value = true
  // 使用模拟数据
  setTimeout(() => {
    permissionTree.value = buildPermissionTree(mockPermissions, 0)
    tableLoading.value = false
  }, 500)
}

// 构建权限树
const buildPermissionTree = (permissions: any[], parentId: number): any[] => {
  const children = permissions.filter((permission: any) => permission.parentId === parentId)
  children.forEach((child: any) => {
    const grandChildren = buildPermissionTree(permissions, child.id)
    if (grandChildren.length > 0) {
      child.children = grandChildren
    }
  })
  return children
}

// 刷新
const handleRefresh = () => {
  loadPermissionTree()
}

// 获取资源类型颜色
const getResourceTypeColor = (type: string) => {
  switch (type) {
    case 'menu':
      return 'success'
    case 'button':
      return 'primary'
    case 'api':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取资源类型文本
const getResourceTypeText = (type: string) => {
  switch (type) {
    case 'menu':
      return '菜单'
    case 'button':
      return '按钮'
    case 'api':
      return '接口'
    default:
      return type
  }
}
</script>

<style scoped>
.permission-manage {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.table-container {
  margin-bottom: 20px;
}
</style>