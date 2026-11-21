<template>
  <div class="role-manage">
    <div class="header">
      <div class="title">角色管理</div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-form">
      <el-form :inline="true" :model="queryForm" class="search-form-content">
        <el-form-item label="角色名称">
          <el-input v-model="queryForm.roleName" placeholder="请输入角色名称" clearable />
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
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="description" label="描述" />
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
            <el-button type="primary" link @click="handleAssignPermissions(scope.row)">分配权限</el-button>
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

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleFormRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" :disabled="isEdit && roleForm.roleCode === 'admin'" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRole">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="600px"
      @close="handlePermissionDialogClose"
    >
      <el-form label-width="100px">
        <el-form-item label="角色名称">
          <span>{{ currentRole?.roleName }}</span>
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="permissionTreeRef"
            :data="permissionTree"
            show-checkbox
            node-key="id"
            :props="defaultProps"
            :default-expanded-keys="expandedKeys"
            :default-checked-keys="checkedPermissionIds"
            class="permission-tree"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePermissions">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { roleApi, permissionApi } from '@/api/system-manage'

// 响应式数据
const tableLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const currentRole = ref<any>(null)
const permissionTree = ref<any[]>([])
const checkedPermissionIds = ref<number[]>([])
const expandedKeys = ref<number[]>([])

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 查询表单
const queryForm = reactive({
  roleName: ''
})

// 角色表单
const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const roleFormRules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '角色编码只能包含字母、数字和下划线，且以字母开头', trigger: 'blur' }
  ]
}

// 表单引用
const roleFormRef = ref<FormInstance>()
const permissionTreeRef = ref()

// 树形组件属性
const defaultProps = {
  children: 'children',
  label: 'permissionName'
}

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑角色' : '新增角色'
})

// 初始化
onMounted(() => {
  loadTableData()
})

// 加载表格数据
const loadTableData = () => {
  tableLoading.value = true
  roleApi.queryRolePage({
    current: pagination.current,
    size: pagination.size,
    roleName: queryForm.roleName
  }).then(res => {
    if (res.code === 200) {
      // 确保响应数据格式正确
      if (res.data && res.data.records) {
        tableData.value = res.data.records
        pagination.total = res.data.total || 0
      } else {
        console.error('响应数据格式不正确:', res)
        ElMessage.error('加载数据失败：响应格式错误')
      }
    } else {
      ElMessage.error(res.message || '加载数据失败')
    }
  }).catch(error => {
    console.error('加载角色数据失败:', error)
    ElMessage.error('加载数据失败，请检查网络连接')
  }).finally(() => {
    tableLoading.value = false
  })
}

// 加载权限树
const loadPermissionTree = () => {
  permissionApi.getPermissionTree().then(res => {
    if (res.code === 200) {
      permissionTree.value = res.data
      
      // 默认展开第一层
      if (res.data.length > 0) {
        expandedKeys.value = res.data.map(item => item.id)
      }
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
  queryForm.roleName = ''
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

// 新增角色
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetRoleForm()
}

// 编辑角色
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(roleForm, {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    description: row.description,
    status: row.status
  })
}

// 删除角色
const handleDelete = (row: any) => {
  if (row.roleCode === 'admin') {
    ElMessage.warning('系统管理员角色不允许删除')
    return
  }

  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tableLoading.value = true
    roleApi.deleteRole(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')

        // 先保存当前数据数量，再清空表格数据，确保UI立即更新
        const currentDataCount = tableData.value.length
        tableData.value = []

        // 检查当前页是否只有一条数据，如果是则跳转到上一页
        if (currentDataCount === 1 && pagination.current > 1) {
          pagination.current--
        }

        // 重新加载数据
        loadTableData()
      } else {
        ElMessage.error(res.message || '删除失败')
        tableLoading.value = false
      }
    }).catch(error => {
      console.error('删除角色失败:', error)
      ElMessage.error('删除失败，请检查网络连接')
      tableLoading.value = false
    })
  }).catch(() => {
    // 用户取消
  })
}

// 分配权限
const handleAssignPermissions = (row: any) => {
  currentRole.value = row
  permissionDialogVisible.value = true
  
  // 加载权限树
  loadPermissionTree()
  
  // 加载角色权限
  roleApi.getRolePermissionIds(row.id).then(res => {
    if (res.code === 200) {
      checkedPermissionIds.value = res.data
      
      // 等待DOM更新后设置选中状态
      nextTick(() => {
        permissionTreeRef.value?.setCheckedKeys(checkedPermissionIds.value)
      })
    }
  })
}

// 保存角色
const handleSaveRole = () => {
  roleFormRef.value?.validate((valid) => {
    if (!valid) return
    
    const api = isEdit.value ? roleApi.updateRole : roleApi.createRole
    api(roleForm).then(res => {
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

// 保存权限
const handleSavePermissions = () => {
  if (!currentRole.value) return
  
  const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []
  const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]
  
  roleApi.assignRolePermissions(currentRole.value.id, allCheckedKeys).then(res => {
    if (res.code === 200) {
      ElMessage.success('权限分配成功')
      permissionDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '权限分配失败')
    }
  })
}

// 重置表单
const resetRoleForm = () => {
  Object.assign(roleForm, {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
  roleFormRef.value?.resetFields()
}

// 对话框关闭事件
const handleDialogClose = () => {
  roleFormRef.value?.resetFields()
}

const handlePermissionDialogClose = () => {
  permissionTree.value = []
  checkedPermissionIds.value = []
  expandedKeys.value = []
  currentRole.value = null
}
</script>

<style scoped>
.role-manage {
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

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}
</style>