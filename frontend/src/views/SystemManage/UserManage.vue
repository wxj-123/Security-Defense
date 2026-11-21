<template>
  <div class="user-manage">
    <div class="header">
      <div class="title">用户管理</div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-form">
      <el-form :inline="true" :model="queryForm" class="search-form-content">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="queryForm.department" placeholder="请输入部门" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        :style="{ width: '100%' }"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="primary" link @click="handleAssignRoles(scope.row)">分配角色</el-button>
            <el-button type="primary" link @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button
              type="primary"
              link
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="userForm.department" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select
            v-model="userForm.roleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
      @close="handleRoleDialogClose"
    >
      <el-form label-width="100px">
        <el-form-item label="用户名">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox
              v-for="role in roleOptions"
              :key="role.id"
              :value="role.id"
            >
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRoles">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="500px"
      @close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { userApi, roleApi } from '@/api/system-manage'
import type { ApiResponse } from '@/types'

// 定义用户类型
interface User {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  department: string
  status: number
  createTime: string
  roleIds?: number[]
}

// 定义角色类型
interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  status: number
  createTime: string
}

// 定义分页数据类型
interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

// 响应式数据
const tableLoading = ref(false)
const tableData = ref<User[]>([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const isEdit = ref(false)
const currentUser = ref<User | null>(null)
const selectedRoleIds = ref<number[]>([])
const roleOptions = ref<Role[]>([])

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 查询表单
const queryForm = reactive({
  username: '',
  phone: '',
  department: '',
  status: null
})

// 用户表单
const userForm = reactive({
  id: null as number | null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  department: '',
  status: 1,
  roleIds: [] as number[]
})

// 密码表单
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const userFormRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

const passwordFormRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 表单引用
const userFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑用户' : '新增用户'
})

// 初始化
onMounted(() => {
  loadTableData()
  loadRoleOptions()
})

// 加载表格数据
const loadTableData = () => {
  tableLoading.value = true
  userApi.queryUserPage({
    ...queryForm,
    current: pagination.current,
    size: pagination.size
  }).then(res => {
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  }).finally(() => {
    tableLoading.value = false
  })
}

// 加载角色选项
const loadRoleOptions = () => {
  roleApi.getAllRoles().then(res => {
    if (res.code === 200) {
      roleOptions.value = res.data
    }
  })
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    username: '',
    phone: '',
    department: '',
    status: null
  })
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  loadTableData()
}

// 当前页改变
const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

// 新增用户
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetUserForm()
}

// 编辑用户
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogVisible.value = true
  
  // 加载用户详情
  userApi.getUserById(row.id).then(res => {
    if (res.code === 200) {
      const user = res.data
      Object.assign(userForm, {
        id: user.id,
        username: user.username,
        realName: user.realName,
        phone: user.phone,
        email: user.email,
        department: user.department,
        status: user.status,
        roleIds: []
      })
      
      // 加载用户角色
      userApi.getUserRoleIds(user.id).then(roleRes => {
        if (roleRes.code === 200) {
          userForm.roleIds = roleRes.data
        }
      })
    }
  })
}

// 删除用户
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userApi.deleteUser(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadTableData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
  }).catch(() => {
    // 用户取消
  })
}

// 切换用户状态
const handleToggleStatus = (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userApi.updateUserStatus({
      userId: row.id,
      status: newStatus
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success(`${statusText}成功`)
        loadTableData()
      } else {
        ElMessage.error(res.message || `${statusText}失败`)
      }
    })
  }).catch(() => {
    // 用户取消
  })
}

// 重置密码
const handleResetPassword = (row: any) => {
  currentUser.value = row
  passwordDialogVisible.value = true
  resetPasswordForm()
}

// 分配角色
const handleAssignRoles = (row: any) => {
  currentUser.value = row
  roleDialogVisible.value = true
  
  // 加载用户角色
  userApi.getUserRoleIds(row.id).then(res => {
    if (res.code === 200) {
      selectedRoleIds.value = res.data
    }
  })
}

// 保存用户
const handleSaveUser = () => {
  userFormRef.value?.validate((valid) => {
    if (!valid) return
    
    const api = isEdit.value ? userApi.updateUser : userApi.createUser
    api(userForm).then(res => {
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadTableData()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    })
  })
}

// 保存角色
const handleSaveRoles = () => {
  if (!currentUser.value) return
  
  userApi.assignUserRoles(currentUser.value.id, selectedRoleIds.value).then(res => {
    if (res.code === 200) {
      ElMessage.success('角色分配成功')
      roleDialogVisible.value = false
      loadTableData()
    } else {
      ElMessage.error(res.message || '角色分配失败')
    }
  })
}

// 保存密码
const handleSavePassword = () => {
  passwordFormRef.value?.validate((valid) => {
    if (!valid) return
    
    if (!currentUser.value) return
    
    userApi.resetPassword({
      userId: currentUser.value.id,
      newPassword: passwordForm.newPassword
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('密码重置成功')
        passwordDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '密码重置失败')
      }
    })
  })
}

// 重置表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    department: '',
    status: 1,
    roleIds: []
  })
  userFormRef.value?.resetFields()
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    newPassword: '',
    confirmPassword: ''
  })
  passwordFormRef.value?.resetFields()
}

// 对话框关闭事件
const handleDialogClose = () => {
  userFormRef.value?.resetFields()
}

const handleRoleDialogClose = () => {
  selectedRoleIds.value = []
  currentUser.value = null
}

const handlePasswordDialogClose = () => {
  passwordFormRef.value?.resetFields()
  currentUser.value = null
}
</script>

<style scoped>
.user-manage {
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

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-form-content {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>