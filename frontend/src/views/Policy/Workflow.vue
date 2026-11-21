<template>
  <div class="workflow-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card pending">
          <el-statistic title="待审批" :value="stats.pending">
            <template #prefix>
              <el-icon color="#e6a23c"><Clock /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card approved">
          <el-statistic title="已通过" :value="stats.approved">
            <template #prefix>
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card rejected">
          <el-statistic title="已拒绝" :value="stats.rejected">
            <template #prefix>
              <el-icon color="#f56c6c"><CircleClose /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="总计" :value="stats.total">
            <template #prefix>
              <el-icon color="#409eff"><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 切换 -->
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 待审批 -->
        <el-tab-pane name="pending">
          <template #label>
            <span>
              待审批
              <el-badge :value="pendingList.length" class="tab-badge" v-if="pendingList.length > 0" />
            </span>
          </template>
          
          <el-table :data="pendingList" border stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="expand-content">
                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="申请人">{{ row.applicant }}</el-descriptions-item>
                    <el-descriptions-item label="申请时间">{{ row.applyTime }}</el-descriptions-item>
                    <el-descriptions-item label="策略类型">{{ row.policyType }}</el-descriptions-item>
                    <el-descriptions-item label="优先级">
                      <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="策略详情" :span="2">
                      {{ row.details }}
                    </el-descriptions-item>
                    <el-descriptions-item label="申请理由" :span="2">
                      {{ row.reason }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="工单ID" width="100" />
            <el-table-column prop="title" label="策略名称" width="200" />
            <el-table-column prop="policyType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.policyType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicant" label="申请人" width="120" />
            <el-table-column prop="applyTime" label="申请时间" width="180" />
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">
                  {{ row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="openApproveDialog(row, 'approve')">
                  <el-icon><Select /></el-icon>
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="openApproveDialog(row, 'reject')">
                  <el-icon><CloseBold /></el-icon>
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 已审批 -->
        <el-tab-pane label="已审批" name="approved">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="状态">
              <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 120px">
                <el-option label="已通过" value="已通过" />
                <el-option label="已拒绝" value="已拒绝" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="filterKeyword" placeholder="搜索" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="applyFilter">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="filteredApprovedList" border stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="expand-content">
                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="申请人">{{ row.applicant }}</el-descriptions-item>
                    <el-descriptions-item label="申请时间">{{ row.applyTime }}</el-descriptions-item>
                    <el-descriptions-item label="审批人">{{ row.approver }}</el-descriptions-item>
                    <el-descriptions-item label="审批时间">{{ row.approveTime }}</el-descriptions-item>
                    <el-descriptions-item label="审批意见" :span="2">
                      {{ row.comment || '无' }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="工单ID" width="100" />
            <el-table-column prop="title" label="策略名称" width="200" />
            <el-table-column prop="applicant" label="申请人" width="120" />
            <el-table-column prop="approver" label="审批人" width="120" />
            <el-table-column prop="approveTime" label="审批时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '已通过' ? 'success' : 'danger'">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewWorkflow(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的申请 -->
        <el-tab-pane label="我的申请" name="myApply">
          <div class="tab-header">
            <el-button type="primary" @click="openApplyDialog">
              <el-icon><Plus /></el-icon>
              新建申请
            </el-button>
          </div>

          <el-table :data="myApplyList" border stripe>
            <el-table-column prop="id" label="工单ID" width="100" />
            <el-table-column prop="title" label="策略名称" width="200" />
            <el-table-column prop="policyType" label="类型" width="120" />
            <el-table-column prop="applyTime" label="申请时间" width="180" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approver" label="审批人" width="120" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === '待审批'"
                  type="danger"
                  size="small"
                  link
                  @click="cancelApply(row)"
                >
                  撤销
                </el-button>
                <el-button type="primary" size="small" link @click="viewWorkflow(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      :title="approveAction === 'approve' ? '审批通过' : '审批拒绝'"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="工单ID">
          <span>{{ currentWorkflow?.id }}</span>
        </el-form-item>
        <el-form-item label="策略名称">
          <span>{{ currentWorkflow?.title }}</span>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approveComment"
            type="textarea"
            :rows="4"
            :placeholder="approveAction === 'approve' ? '请输入审批意见（可选）' : '请输入拒绝理由'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button
          :type="approveAction === 'approve' ? 'success' : 'danger'"
          @click="confirmApprove"
        >
          确认{{ approveAction === 'approve' ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="新建策略申请" width="600px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="策略名称">
          <el-input v-model="applyForm.title" placeholder="请输入策略名称" />
        </el-form-item>
        <el-form-item label="策略类型">
          <el-select v-model="applyForm.policyType" placeholder="请选择" style="width: 100%">
            <el-option label="防火墙规则" value="防火墙规则" />
            <el-option label="访问控制" value="访问控制" />
            <el-option label="安全策略" value="安全策略" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="applyForm.priority" placeholder="请选择" style="width: 100%">
            <el-option label="紧急" value="紧急" />
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="策略详情">
          <el-input
            v-model="applyForm.details"
            type="textarea"
            :rows="3"
            placeholder="请输入策略详情"
          />
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入申请理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 工作流详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工作流详情" width="700px">
      <el-timeline v-if="viewingWorkflow">
        <el-timeline-item
          :timestamp="viewingWorkflow.applyTime"
          placement="top"
          :color="'#409eff'"
        >
          <el-card>
            <h4>申请提交</h4>
            <p>申请人：{{ viewingWorkflow.applicant }}</p>
            <p>申请理由：{{ viewingWorkflow.reason }}</p>
          </el-card>
        </el-timeline-item>
        
        <el-timeline-item
          v-if="viewingWorkflow.status !== '待审批'"
          :timestamp="viewingWorkflow.approveTime"
          placement="top"
          :color="viewingWorkflow.status === '已通过' ? '#67c23a' : '#f56c6c'"
        >
          <el-card>
            <h4>{{ viewingWorkflow.status }}</h4>
            <p>审批人：{{ viewingWorkflow.approver }}</p>
            <p>审批意见：{{ viewingWorkflow.comment || '无' }}</p>
          </el-card>
        </el-timeline-item>
        
        <el-timeline-item
          v-if="viewingWorkflow.status === '待审批'"
          timestamp="待审批"
          placement="top"
          color="#e6a23c"
        >
          <el-card>
            <h4>等待审批</h4>
            <p>审批人：{{ viewingWorkflow.approver }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock,
  CircleCheck,
  CircleClose,
  Document,
  Select,
  CloseBold,
  Plus
} from '@element-plus/icons-vue'

const activeTab = ref('pending')

// 统计数据
const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0
})

// 待审批列表
const pendingList = ref([
  {
    id: 'WF202511090001',
    title: '允许外网访问80端口',
    policyType: '防火墙规则',
    applicant: 'zhangsan',
    applyTime: '2025-11-09 10:00:00',
    priority: '高',
    status: '待审批',
    approver: 'admin',
    details: '源IP: 0.0.0.0/0, 目标端口: 80, 动作: 允许',
    reason: '业务需求，需要对外提供Web服务'
  },
  {
    id: 'WF202511090002',
    title: '限制SSH访问',
    policyType: '访问控制',
    applicant: 'lisi',
    applyTime: '2025-11-09 11:30:00',
    priority: '中',
    status: '待审批',
    approver: 'admin',
    details: '源IP: 0.0.0.0/0, 目标端口: 22, 动作: 拒绝',
    reason: '安全加固，防止暴力破解'
  },
  {
    id: 'WF202511090003',
    title: '数据库白名单更新',
    policyType: '安全策略',
    applicant: 'wangwu',
    applyTime: '2025-11-09 14:00:00',
    priority: '紧急',
    status: '待审批',
    approver: 'admin',
    details: '添加IP: 192.168.1.100 到数据库白名单',
    reason: '新增应用服务器，需要访问数据库'
  }
])

// 已审批列表
const approvedList = ref([
  {
    id: 'WF202511080001',
    title: '允许HTTPS访问',
    policyType: '防火墙规则',
    applicant: 'zhangsan',
    applyTime: '2025-11-08 09:00:00',
    status: '已通过',
    approver: 'admin',
    approveTime: '2025-11-08 09:30:00',
    comment: '同意开放443端口'
  },
  {
    id: 'WF202511080002',
    title: '开放测试环境端口',
    policyType: '防火墙规则',
    applicant: 'lisi',
    applyTime: '2025-11-08 10:00:00',
    status: '已拒绝',
    approver: 'admin',
    approveTime: '2025-11-08 10:15:00',
    comment: '测试环境不应对外开放'
  }
])

// 我的申请
const myApplyList = ref([
  {
    id: 'WF202511090004',
    title: '添加新的防火墙规则',
    policyType: '防火墙规则',
    applyTime: '2025-11-09 15:00:00',
    status: '待审批',
    approver: 'admin'
  }
])

// 筛选
const filterStatus = ref('')
const filterKeyword = ref('')

const filteredApprovedList = computed(() => {
  let list = approvedList.value
  if (filterStatus.value) {
    list = list.filter(item => item.status === filterStatus.value)
  }
  if (filterKeyword.value) {
    list = list.filter(item => 
      item.title.includes(filterKeyword.value) || 
      item.id.includes(filterKeyword.value)
    )
  }
  return list
})

// 对话框
const approveDialogVisible = ref(false)
const applyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentWorkflow = ref<any>(null)
const viewingWorkflow = ref<any>(null)
const approveAction = ref<'approve' | 'reject'>('approve')
const approveComment = ref('')

const applyForm = reactive({
  title: '',
  policyType: '',
  priority: '中',
  details: '',
  reason: ''
})

// 更新统计
const updateStats = () => {
  stats.pending = pendingList.value.length
  stats.approved = approvedList.value.filter(item => item.status === '已通过').length
  stats.rejected = approvedList.value.filter(item => item.status === '已拒绝').length
  stats.total = pendingList.value.length + approvedList.value.length
}

// 打开审批对话框
const openApproveDialog = (workflow: any, action: 'approve' | 'reject') => {
  currentWorkflow.value = workflow
  approveAction.value = action
  approveComment.value = ''
  approveDialogVisible.value = true
}

// 确认审批
const confirmApprove = () => {
  if (approveAction.value === 'reject' && !approveComment.value) {
    ElMessage.warning('请输入拒绝理由')
    return
  }

  const workflow = currentWorkflow.value
  const newStatus = approveAction.value === 'approve' ? '已通过' : '已拒绝'
  
  // 从待审批列表移除
  const index = pendingList.value.findIndex(w => w.id === workflow.id)
  if (index !== -1) {
    pendingList.value.splice(index, 1)
  }
  
  // 添加到已审批列表
  approvedList.value.unshift({
    ...workflow,
    status: newStatus,
    approveTime: new Date().toLocaleString(),
    comment: approveComment.value
  })
  
  ElMessage.success(`已${approveAction.value === 'approve' ? '通过' : '拒绝'}该申请`)
  approveDialogVisible.value = false
  updateStats()
}

// 打开申请对话框
const openApplyDialog = () => {
  Object.assign(applyForm, {
    title: '',
    policyType: '',
    priority: '中',
    details: '',
    reason: ''
  })
  applyDialogVisible.value = true
}

// 提交申请
const submitApply = () => {
  if (!applyForm.title || !applyForm.policyType) {
    ElMessage.warning('请填写必填项')
    return
  }

  const newApply = {
    id: 'WF' + new Date().getTime(),
    ...applyForm,
    applicant: 'admin',
    applyTime: new Date().toLocaleString(),
    status: '待审批',
    approver: 'admin'
  }
  
  myApplyList.value.unshift(newApply)
  pendingList.value.unshift(newApply)
  
  ElMessage.success('申请已提交')
  applyDialogVisible.value = false
  updateStats()
}

// 撤销申请
const cancelApply = async (workflow: any) => {
  try {
    await ElMessageBox.confirm('确定要撤销该申请吗？', '提示', {
      type: 'warning'
    })
    
    const index = myApplyList.value.findIndex(w => w.id === workflow.id)
    if (index !== -1) {
      myApplyList.value.splice(index, 1)
    }
    
    const pendingIndex = pendingList.value.findIndex(w => w.id === workflow.id)
    if (pendingIndex !== -1) {
      pendingList.value.splice(pendingIndex, 1)
    }
    
    ElMessage.success('申请已撤销')
    updateStats()
  } catch {
    // 用户取消
  }
}

// 查看工作流详情
const viewWorkflow = (workflow: any) => {
  viewingWorkflow.value = workflow
  detailDialogVisible.value = true
}

// 筛选
const applyFilter = () => {
  // 筛选逻辑已在 computed 中
}

const resetFilter = () => {
  filterStatus.value = ''
  filterKeyword.value = ''
}

// 获取优先级标签类型
const getPriorityType = (priority: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '紧急': 'danger',
    '高': 'warning',
    '中': 'primary',
    '低': 'info'
  }
  return types[priority] || 'info'
}

// 获取状态标签类型
const getStatusType = (status: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '待审批': 'warning',
    '已通过': 'success',
    '已拒绝': 'danger'
  }
  return types[status] || 'info'
}

// 初始化
const init = () => {
  updateStats()
}

init()
</script>

<style scoped>
.workflow-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 3px solid #409eff;
}

.stat-card.pending {
  border-left-color: #e6a23c;
}

.stat-card.approved {
  border-left-color: #67c23a;
}

.stat-card.rejected {
  border-left-color: #f56c6c;
}

.tab-badge {
  margin-left: 5px;
}

.filter-form {
  margin-bottom: 20px;
}

.tab-header {
  margin-bottom: 20px;
}

.expand-content {
  padding: 20px;
}

:deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: bold;
}

:deep(.el-timeline-item__timestamp) {
  font-weight: bold;
}
</style>
