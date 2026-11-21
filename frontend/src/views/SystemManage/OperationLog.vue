<template>
  <div class="operation-log">
    <div class="header">
      <div class="title">操作日志</div>
      <div class="actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="danger" @click="handleCleanExpiredLogs">
          <el-icon><Delete /></el-icon>
          清理过期日志
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-form">
      <el-form :inline="true" :model="queryForm" class="search-form-content">
        <el-form-item label="操作人">
          <el-input v-model="queryForm.username" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="queryForm.operation" placeholder="请输入操作类型" clearable />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="queryForm.status" placeholder="请选择操作结果" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateRangeChange"
          />
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
        border
        class="operation-log-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作类型" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="操作IP" width="140" />
        <el-table-column prop="status" label="操作结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleViewDetail(scope.row)">详情</el-button>
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

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentLog?.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog?.username }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog?.operation }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog?.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag :type="currentLog?.status === 1 ? 'success' : 'danger'">
            {{ currentLog?.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog?.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentLog?.errorMsg" label="错误信息">
          <el-text type="danger">{{ currentLog?.errorMsg }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 清理过期日志对话框 -->
    <el-dialog
      v-model="cleanDialogVisible"
      title="清理过期日志"
      width="400px"
    >
      <el-form label-width="120px">
        <el-form-item label="保留天数">
          <el-input-number
            v-model="cleanRetentionDays"
            :min="7"
            :max="365"
            controls-position="right"
          />
          <div class="form-tip">将清理{{ cleanRetentionDays }}天前的日志记录</div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete } from '@element-plus/icons-vue'
import { logApi } from '@/api/system-manage'

// 响应式数据
const tableLoading = ref(false)
const tableData = ref<any[]>([])
const detailDialogVisible = ref(false)
const cleanDialogVisible = ref(false)
const currentLog = ref<any>(null)
const dateRange = ref<[string, string] | []>([])
const cleanRetentionDays = ref(180)

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 查询表单
const queryForm = reactive({
  username: '',
  operation: '',
  status: '',
  startTime: '',
  endTime: ''
})

// 初始化
onMounted(() => {
  loadTableData()
})

// 加载表格数据
const loadTableData = () => {
  tableLoading.value = true
  
  // 模拟数据
  const mockData = [
    {
      id: 1,
      username: 'admin',
      operation: '查询用户列表',
      ip: '192.168.1.100',
      status: 1,
      createTime: '2025-11-23 14:30:25'
    },
    {
      id: 2,
      username: 'admin',
      operation: '新增角色: 安全审计员',
      ip: '192.168.1.100',
      status: 1,
      createTime: '2025-11-23 13:15:42'
    },
    {
      id: 3,
      username: 'admin',
      operation: '修改系统名称为网络安全防御系统',
      ip: '192.168.1.100',
      status: 1,
      createTime: '2025-11-23 12:20:18'
    },
    {
      id: 4,
      username: 'operator',
      operation: '创建系统备份',
      ip: '192.168.1.101',
      status: 1,
      createTime: '2025-11-23 11:45:33'
    },
    {
      id: 5,
      username: 'auditor',
      operation: '查询操作日志',
      ip: '192.168.1.102',
      status: 1,
      createTime: '2025-11-23 10:30:55'
    },
    {
      id: 6,
      username: 'admin',
      operation: '修改用户状态',
      ip: '192.168.1.100',
      status: 1,
      createTime: '2025-11-23 09:15:27'
    },
    {
      id: 7,
      username: 'admin',
      operation: '为安全审计员分配权限',
      ip: '192.168.1.100',
      status: 1,
      createTime: '2025-11-23 08:40:12'
    },
    {
      id: 8,
      username: 'operator',
      operation: '恢复系统备份',
      ip: '192.168.1.101',
      status: 0,
      errorMsg: '备份文件不存在',
      createTime: '2025-11-23 07:25:44'
    },
    {
      id: 9,
      username: 'admin',
      operation: '查询系统配置',
      ip: '192.168.1.100',
      status: 1,
      createTime: '2025-11-23 06:10:33'
    },
    {
      id: 10,
      username: 'auditor',
      operation: '清理30天前的操作日志',
      ip: '192.168.1.102',
      status: 1,
      createTime: '2025-11-23 05:35:21'
    }
  ];
  
  // 模拟分页
  const start = (pagination.current - 1) * pagination.size;
  const end = start + pagination.size;
  const paginatedData = mockData.slice(start, end);
  
  setTimeout(() => {
    tableData.value = paginatedData;
    pagination.total = mockData.length;
    tableLoading.value = false;
  }, 500);
  
  /*
  logApi.queryOperationLogPage({
    ...queryForm,
    current: pagination.current,
    size: pagination.size
  }).then(res => {
    if (res.code === 200) {
      tableData.value = res.data.records;
      pagination.total = res.data.total;
    }
  }).finally(() => {
    tableLoading.value = false;
  });
  */
}

// 日期范围改变
const handleDateRangeChange = (val: [string, string]) => {
  if (val) {
    queryForm.startTime = val[0]
    queryForm.endTime = val[1]
  } else {
    queryForm.startTime = ''
    queryForm.endTime = ''
  }
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
    operation: '',
    status: '',
    startTime: '',
    endTime: ''
  })
  dateRange.value = [] as [string, string] | []
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

// 查看详情
const handleViewDetail = (row: any) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 导出
const handleExport = () => {
  // 这里可以实现导出Excel的功能
  ElMessage.info('导出功能开发中...')
}

// 清理过期日志
const handleCleanExpiredLogs = () => {
  cleanDialogVisible.value = true
}

const handleConfirmClean = () => {
  ElMessageBox.confirm(
    `确定要清理${cleanRetentionDays.value}天前的日志记录吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    logApi.cleanExpiredLogs({
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
</script>

<style scoped>
.operation-log {
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

.operation-log-table {
  width: 100%;
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