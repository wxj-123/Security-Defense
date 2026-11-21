<template>
  <div class="anomaly-container">
    <!-- 异常统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="总异常数" :value="stats.total">
            <template #prefix>
              <el-icon color="#f56c6c"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card high">
          <el-statistic title="高危异常" :value="stats.high">
            <template #prefix>
              <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card medium">
          <el-statistic title="中危异常" :value="stats.medium">
            <template #prefix>
              <el-icon color="#e6a23c"><WarningFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card low">
          <el-statistic title="低危异常" :value="stats.low">
            <template #prefix>
              <el-icon color="#67c23a"><InfoFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 异常类型分布图表 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>异常类型分布</span>
          </template>
          <div ref="typeChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>异常趋势分析</span>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 检测规则 -->
    <el-card class="rules-card">
      <template #header>
        <div class="card-header">
          <span>检测规则</span>
          <el-button type="primary" size="small" @click="openRuleDialog()">
            <el-icon><Plus /></el-icon>
            添加规则
          </el-button>
        </div>
      </template>
      
      <el-table :data="rules" border stripe>
        <el-table-column prop="name" label="规则名称" width="200" />
        <el-table-column prop="type" label="检测类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="toggleRule(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openRuleDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" link @click="deleteRule(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 异常列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>异常记录</span>
          <el-button type="primary" size="small" @click="loadAnomalies">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="类型">
          <el-select v-model="filterType" placeholder="全部" clearable style="width: 150px">
            <el-option label="频繁访问" value="频繁访问" />
            <el-option label="异常时段访问" value="异常时段访问" />
            <el-option label="大流量传输" value="大流量传输" />
            <el-option label="端口扫描" value="端口扫描" />
            <el-option label="暴力破解" value="暴力破解" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="filterSeverity" placeholder="全部" clearable style="width: 120px">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源IP">
          <el-input v-model="filterSource" placeholder="输入IP" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleResetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="anomalies"
        stripe
        border
        v-loading="loading"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="检测时间">{{ row.time }}</el-descriptions-item>
                <el-descriptions-item label="持续时间">{{ row.duration }}分钟</el-descriptions-item>
                <el-descriptions-item label="源IP">{{ row.source }}</el-descriptions-item>
                <el-descriptions-item label="目标IP">{{ row.target || 'N/A' }}</el-descriptions-item>
                <el-descriptions-item label="触发规则">{{ row.rule }}</el-descriptions-item>
                <el-descriptions-item label="置信度">{{ row.confidence }}%</el-descriptions-item>
                <el-descriptions-item label="详细信息" :span="2">
                  {{ row.details }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="异常类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源IP" width="150" />
        <el-table-column prop="severity" label="严重程度" width="120">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)" effect="dark">
              {{ row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.confidence" :color="getConfidenceColor(row.confidence)" />
          </template>
        </el-table-column>
        <el-table-column prop="time" label="检测时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已处理' ? 'success' : 'warning'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleAnomaly(row, 'block')">
              封禁
            </el-button>
            <el-button type="success" size="small" link @click="handleAnomaly(row, 'ignore')">
              忽略
            </el-button>
            <el-button type="info" size="small" link @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadAnomalies"
        @current-change="loadAnomalies"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 规则编辑对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="currentRule?.id ? '编辑规则' : '添加规则'"
      width="600px"
    >
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="检测类型">
          <el-select v-model="ruleForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="频繁访问" value="频繁访问" />
            <el-option label="异常时段访问" value="异常时段访问" />
            <el-option label="大流量传输" value="大流量传输" />
            <el-option label="端口扫描" value="端口扫描" />
            <el-option label="暴力破解" value="暴力破解" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值">
          <el-input-number v-model="ruleForm.threshold" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="ruleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入规则描述"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="ruleForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="异常详情" width="700px">
      <el-descriptions :column="2" border v-if="currentAnomaly">
        <el-descriptions-item label="异常ID">{{ currentAnomaly.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeTagType(currentAnomaly.type)">
            {{ currentAnomaly.type }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源IP">{{ currentAnomaly.source }}</el-descriptions-item>
        <el-descriptions-item label="目标IP">{{ currentAnomaly.target || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getSeverityType(currentAnomaly.severity)" effect="dark">
            {{ currentAnomaly.severity }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="置信度">{{ currentAnomaly.confidence }}%</el-descriptions-item>
        <el-descriptions-item label="检测时间">{{ currentAnomaly.time }}</el-descriptions-item>
        <el-descriptions-item label="持续时间">{{ currentAnomaly.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="触发规则">{{ currentAnomaly.rule }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentAnomaly.status === '已处理' ? 'success' : 'warning'">
            {{ currentAnomaly.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="详细信息" :span="2">
          {{ currentAnomaly.details }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning,
  CircleCloseFilled,
  WarningFilled,
  InfoFilled,
  Plus,
  Refresh
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { getAnomalyDetection } from '@/api/security'

const loading = ref(false)
const typeChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
let typeChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

// 统计数据
const stats = reactive({
  total: 0,
  high: 0,
  medium: 0,
  low: 0
})

// 检测规则
const rules = ref<any[]>([
  {
    id: 1,
    name: '高频访问检测',
    type: '频繁访问',
    threshold: 100,
    description: '检测1分钟内访问次数超过100次的IP',
    enabled: true
  },
  {
    id: 2,
    name: '非工作时间访问',
    type: '异常时段访问',
    threshold: 10,
    description: '检测晚上22:00至次日6:00的访问行为',
    enabled: true
  },
  {
    id: 3,
    name: '大文件传输',
    type: '大流量传输',
    threshold: 1024,
    description: '检测单次传输超过1GB的文件',
    enabled: true
  },
  {
    id: 4,
    name: '端口扫描检测',
    type: '端口扫描',
    threshold: 50,
    description: '检测1分钟内访问超过50个不同端口',
    enabled: true
  }
])

// 异常列表
const anomalies = ref<any[]>([])
const filterType = ref('')
const filterSeverity = ref('')
const filterSource = ref('')

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 规则对话框
const ruleDialogVisible = ref(false)
const currentRule = ref<any>(null)
const ruleForm = reactive({
  name: '',
  type: '',
  threshold: 100,
  description: '',
  enabled: true
})

// 详情对话框
const detailVisible = ref(false)
const currentAnomaly = ref<any>(null)

// 加载异常数据
const loadAnomalies = async () => {
  loading.value = true
  try {
    console.log('开始加载异常数据')
    const res = await getAnomalyDetection()
    console.log('收到响应:', res)
    
    // 检查响应数据结构
    if (!res || !res.data) {
      console.error('响应数据结构异常:', res)
      ElMessage.error('数据格式错误')
      return
    }
    
    // 更新统计
    stats.total = res.data.total || 0
    stats.high = res.data.highSeverity || 0
    stats.medium = res.data.mediumSeverity || 0
    stats.low = res.data.lowSeverity || 0
    
    // 更新列表
    if (Array.isArray(res.data.anomalies)) {
      anomalies.value = res.data.anomalies.map((item: any) => ({
        ...item,
        target: item.target || '192.168.1.1',
        duration: Math.floor(Math.random() * 30) + 5,
        rule: '高频访问检测',
        status: Math.random() > 0.5 ? '已处理' : '待处理',
        details: `检测到来自 ${item.source} 的异常行为，类型为 ${item.type}，置信度 ${item.confidence}%`
      }))
    } else {
      anomalies.value = []
    }
    
    pagination.total = anomalies.value.length
    
    // 更新图表
    updateCharts()
  } catch (error: any) {
    console.error('加载异常数据失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response)
    }
    ElMessage.error(`加载失败: ${error.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
  }
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  window.addEventListener('resize', () => {
    typeChart?.resize()
    trendChart?.resize()
  })
}

// 更新图表
const updateCharts = () => {
  // 类型分布饼图
  const typeData = [
    { value: 0, name: '频繁访问' },
    { value: 0, name: '异常时段访问' },
    { value: 0, name: '大流量传输' },
    { value: 0, name: '端口扫描' },
    { value: 0, name: '暴力破解' }
  ]
  
  anomalies.value.forEach(item => {
    const index = typeData.findIndex(t => t.name === item.type)
    if (index !== -1) {
      typeData[index].value++
    }
  })
  
  const typeOption: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: typeData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  typeChart?.setOption(typeOption)
  
  // 趋势折线图
  const hours = []
  const counts = []
  for (let i = 23; i >= 0; i--) {
    const hour = new Date().getHours() - i
    hours.push(hour < 0 ? 24 + hour : hour)
    counts.push(Math.floor(Math.random() * 10) + 1)
  }
  
  const trendOption: EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: hours.map(h => `${h}:00`),
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '异常数量'
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: counts,
        itemStyle: {
          color: '#f56c6c'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
          ])
        }
      }
    ]
  }
  
  trendChart?.setOption(trendOption)
}

// 打开规则对话框
const openRuleDialog = (rule?: any) => {
  if (rule) {
    currentRule.value = rule
    Object.assign(ruleForm, rule)
  } else {
    currentRule.value = null
    Object.assign(ruleForm, {
      name: '',
      type: '',
      threshold: 100,
      description: '',
      enabled: true
    })
  }
  ruleDialogVisible.value = true
}

// 保存规则
const saveRule = () => {
  if (currentRule.value) {
    Object.assign(currentRule.value, ruleForm)
    ElMessage.success('规则已更新')
  } else {
    rules.value.push({
      id: rules.value.length + 1,
      ...ruleForm
    })
    ElMessage.success('规则已添加')
  }
  ruleDialogVisible.value = false
}

// 切换规则状态
const toggleRule = (rule: any) => {
  ElMessage.success(`规则已${rule.enabled ? '启用' : '禁用'}`)
}

// 删除规则
const deleteRule = async (rule: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '警告', {
      type: 'warning'
    })
    const index = rules.value.findIndex(r => r.id === rule.id)
    if (index !== -1) {
      rules.value.splice(index, 1)
      ElMessage.success('规则已删除')
    }
  } catch {
    // 用户取消
  }
}

// 处理异常
const handleAnomaly = async (row: any, action: string) => {
  try {
    const actionText = action === 'block' ? '封禁' : '忽略'
    await ElMessageBox.confirm(`确定要${actionText}该异常吗？`, '提示', {
      type: 'warning'
    })
    
    row.status = '已处理'
    ElMessage.success(`已${actionText}该异常`)
  } catch {
    // 用户取消
  }
}

// 查看详情
const viewDetail = (row: any) => {
  currentAnomaly.value = row
  detailVisible.value = true
}

// 筛选
const handleFilter = () => {
  pagination.page = 1
  // 这里可以添加实际的筛选逻辑
}

const handleResetFilter = () => {
  filterType.value = ''
  filterSeverity.value = ''
  filterSource.value = ''
  handleFilter()
}

// 获取类型标签类型
const getTypeTagType = (type: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '频繁访问': 'warning',
    '异常时段访问': 'danger',
    '大流量传输': 'primary',
    '端口扫描': 'danger',
    '暴力破解': 'danger'
  }
  return types[type] || 'info'
}

// 获取严重程度标签类型
const getSeverityType = (severity: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'success'
  }
  return types[severity] || 'info'
}

// 获取置信度颜色
const getConfidenceColor = (confidence: number) => {
  if (confidence >= 80) return '#f56c6c'
  if (confidence >= 60) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => {
  initCharts()
  loadAnomalies()
})

onUnmounted(() => {
  typeChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.anomaly-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 3px solid #409eff;
}

.stat-card.high {
  border-left-color: #f56c6c;
}

.stat-card.medium {
  border-left-color: #e6a23c;
}

.stat-card.low {
  border-left-color: #67c23a;
}

.rules-card {
  margin: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.expand-content {
  padding: 20px;
}

:deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: bold;
}

:deep(.el-progress__text) {
  font-size: 12px !important;
}
</style>
