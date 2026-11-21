<template>
  <div class="data-backup">
    <div class="header">
      <div class="title">数据备份</div>
      <div class="actions">
        <el-button type="primary" @click="handleCreateBackup">
          <el-icon><Plus /></el-icon>
          创建备份
        </el-button>
        <el-button type="danger" @click="handleCleanExpiredBackups">
          <el-icon><Delete /></el-icon>
          清理过期备份
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="backupName" label="备份名称" min-width="200" />
        <el-table-column prop="backupType" label="备份类型" width="120">
          <template #default="scope">
            <el-tag :type="getBackupTypeColor(scope.row.backupType)">
              {{ getBackupTypeText(scope.row.backupType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="backupSize" label="备份大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.backupSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="backupStatus" label="备份状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.backupStatus === '成功' ? 'success' : 'danger'">
              {{ scope.row.backupStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="backupUser" label="操作人" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.backupStatus === '成功'"
              type="primary"
              link
              @click="handleRestoreBackup(scope.row)"
            >
              恢复
            </el-button>
            <el-button type="danger" link @click="handleDeleteBackup(scope.row)">删除</el-button>
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

    <!-- 创建备份对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建备份"
      width="600px"
      @close="handleCreateDialogClose"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="100px"
      >
        <el-form-item label="备份名称" prop="backupName">
          <el-input v-model="createForm.backupName" placeholder="请输入备份名称" />
        </el-form-item>
        <el-form-item label="备份类型" prop="backupType">
          <el-select v-model="createForm.backupType" style="width: 100%">
            <el-option label="完整备份" value="full" />
            <el-option label="增量备份" value="incremental" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备份描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveBackup">创建备份</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 清理过期备份对话框 -->
    <el-dialog
      v-model="cleanDialogVisible"
      title="清理过期备份"
      width="400px"
    >
      <el-form label-width="120px">
        <el-form-item label="保留天数">
          <el-input-number
            v-model="cleanRetentionDays"
            :min="7"
            :max="90"
            controls-position="right"
          />
          <div class="form-tip">将清理{{ cleanRetentionDays }}天前的备份文件</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cleanDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleConfirmClean">确定清理</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { backupApi } from '@/api/system-manage'

// 响应式数据
const tableLoading = ref(false)
const tableData = ref([])
const createDialogVisible = ref(false)
const cleanDialogVisible = ref(false)
const cleanRetentionDays = ref(30)

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 创建备份表单
const createForm = reactive({
  backupName: '',
  backupType: 'full',
  description: ''
})

// 表单验证规则
const createFormRules: FormRules = {
  backupName: [
    { required: true, message: '请输入备份名称', trigger: 'blur' }
  ],
  backupType: [
    { required: true, message: '请选择备份类型', trigger: 'change' }
  ]
}

// 表单引用
const createFormRef = ref<FormInstance>()

// 初始化
onMounted(() => {
  loadTableData()
})

// 加载表格数据
const loadTableData = () => {
  tableLoading.value = true
  backupApi.queryBackupPage({
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

// 创建备份
const handleCreateBackup = () => {
  createDialogVisible.value = true
  resetCreateForm()
}

// 恢复备份
const handleRestoreBackup = (row: any) => {
  ElMessageBox.confirm(
    `确定要恢复备份 "${row.backupName}" 吗？恢复操作将覆盖当前数据！`,
    '警告',
    {
      confirmButtonText: '确定恢复',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    backupApi.restoreBackup(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('备份恢复成功')
        loadTableData()
      } else {
        ElMessage.error(res.message || '备份恢复失败')
      }
    })
  }).catch(() => {
    // 用户取消
  })
}

// 删除备份
const handleDeleteBackup = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除备份 "${row.backupName}" 吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    backupApi.deleteBackup(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('备份删除成功')
        loadTableData()
      } else {
        ElMessage.error(res.message || '备份删除失败')
      }
    })
  }).catch(() => {
    // 用户取消
  })
}

// 保存备份
const handleSaveBackup = () => {
  createFormRef.value?.validate((valid) => {
    if (!valid) return
    
    backupApi.createBackup(createForm).then(res => {
      if (res.code === 200) {
        ElMessage.success('备份创建成功')
        createDialogVisible.value = false
        loadTableData()
      } else {
        ElMessage.error(res.message || '备份创建失败')
      }
    })
  })
}

// 清理过期备份
const handleCleanExpiredBackups = () => {
  cleanDialogVisible.value = true
}

const handleConfirmClean = () => {
  ElMessageBox.confirm(
    `确定要清理${cleanRetentionDays.value}天前的备份文件吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    backupApi.cleanExpiredBackups({
      retentionDays: cleanRetentionDays.value
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('清理操作已提交，系统将在后台处理')
        cleanDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '清理失败')
      }
    })
  }).catch(() => {
    // 用户取消
  })
}

// 重置表单
const resetCreateForm = () => {
  Object.assign(createForm, {
    backupName: '',
    backupType: 'full',
    description: ''
  })
  createFormRef.value?.resetFields()
}

// 对话框关闭事件
const handleCreateDialogClose = () => {
  createFormRef.value?.resetFields()
}

// 获取备份类型颜色
const getBackupTypeColor = (type: string) => {
  switch (type) {
    case 'full':
      return 'primary'
    case 'incremental':
      return 'success'
    case 'manual':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取备份类型文本
const getBackupTypeText = (type: string) => {
  switch (type) {
    case 'full':
      return '完整备份'
    case 'incremental':
      return '增量备份'
    case 'manual':
      return '手动备份'
    default:
      return type
  }
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let fileSize = size
  
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  
  return `${fileSize.toFixed(2)} ${units[index]}`
}
</script>

<style scoped>
.data-backup {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>