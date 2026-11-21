<template>
  <div class="threat-intelligence">
    <div class="page-header">
      <h2>威胁情报概览</h2>
      <div class="header-controls">
        <el-select v-model="threatFilter" @change="handleFilterChange" style="width: 150px">
          <el-option label="全部威胁" value="all" />
          <el-option label="高危威胁" value="high" />
          <el-option label="今日新增" value="today" />
          <el-option label="活跃威胁" value="active" />
        </el-select>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 威胁统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" v-for="(stat, index) in threatStats" :key="index">
        <div class="stat-content">
          <div class="stat-icon" :style="{ backgroundColor: stat.color + '20' }">
            <el-icon :size="24" :color="stat.color">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="content-layout">
      <!-- 威胁类型分布图表 -->
      <div class="chart-section">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>威胁类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="threatTypeChart" height="300"></canvas>
          </div>
        </el-card>
      </div>

      <!-- 威胁趋势图表 -->
      <div class="trend-section">
        <el-card class="trend-card">
          <template #header>
            <div class="card-header">
              <span>威胁趋势分析</span>
              <el-select v-model="trendInterval" @change="handleTrendChange" style="width: 100px">
                <el-option label="1小时" value="1" />
                <el-option label="6小时" value="6" />
                <el-option label="24小时" value="24" />
                <el-option label="7天" value="168" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="threatTrendChart" height="300"></canvas>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 威胁情报列表 -->
    <el-card class="threat-list-card">
      <template #header>
        <div class="card-header">
          <span>威胁情报列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索威胁类型或IOC"
              style="width: 200px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredThreatData"
        stripe
        v-loading="loading"
        @row-click="handleRowClick"
        row-class-name="table-row-hover"
      >
        <el-table-column prop="threatType" label="威胁类型" width="120">
          <template #default="scope">
            <el-tag :type="getThreatTypeColor(scope.row.threatType)" size="small">
              {{ scope.row.threatType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="iocType" label="IOC类型" width="100" />
        <el-table-column prop="iocValue" label="IOC值" min-width="200">
          <template #default="scope">
            <el-tooltip :content="scope.row.iocValue" placement="top">
              <span class="ioc-value">{{ scope.row.iocValue }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="scope">
            <el-tag :type="getSeverityType(scope.row.severity)" size="small">
              {{ getSeverityText(scope.row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="confidenceScore" label="置信度" width="100">
          <template #default="scope">
            <el-progress
              :percentage="Number(scope.row.confidenceScore || 0)"
              :color="getConfidenceColor(scope.row.confidenceScore)"
              :stroke-width="6"
              :show-text="false"
            />
            <span class="confidence-text">{{ scope.row.confidenceScore }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discoveredTime" label="发现时间" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.discoveredTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click.stop="viewThreatDetail(scope.row)">
              详情
            </el-button>
            <el-button 
              :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'" 
              size="small" 
              @click.stop="toggleThreatStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? '忽略' : '激活' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Search, InfoFilled, Warning, Connection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Chart, 
  CategoryScale, 
  LinearScale, 
  LineElement, 
  PointElement, 
  LineController,
  Title, 
  Tooltip, 
  Legend,
  DoughnutController,
  ArcElement,
  Filler
} from 'chart.js'
import { getThreatIntelligence } from '@/api/security-situation'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, LineController, Title, Tooltip, Legend, DoughnutController, ArcElement, Filler)

// 响应式数据
const threatFilter = ref('all')
const threatsData = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const trendInterval = ref('24')

// 图表引用
const threatTypeChart = ref<HTMLCanvasElement>()
const threatTrendChart = ref<HTMLCanvasElement>()
let threatTypeChartInstance: Chart | null = null
let threatTrendChartInstance: Chart | null = null

// 路由
const router = useRouter()

// 计算属性
const filteredThreatData = computed(() => {
  let data = threatsData.value
  
  // 应用筛选条件
  if (threatFilter.value === 'high') {
    data = data.filter(t => ['CRITICAL', 'HIGH'].includes(t.severity))
  } else if (threatFilter.value === 'today') {
    const today = new Date().toDateString()
    data = data.filter(t => new Date(t.discoveredTime).toDateString() === today)
  } else if (threatFilter.value === 'active') {
    data = data.filter(t => t.status === 'ACTIVE')
  }
  
  // 应用搜索关键字
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(t => 
      t.threatType?.toLowerCase().includes(keyword) ||
      t.iocValue?.toLowerCase().includes(keyword)
    )
  }
  
  return data
})

const paginatedThreatData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredThreatData.value.slice(start, end)
})

const totalCount = computed(() => filteredThreatData.value.length)

const threatStats = computed(() => {
  const stats = {
    total: threatsData.value.length,
    critical: threatsData.value.filter(t => t.severity === 'CRITICAL').length,
    high: threatsData.value.filter(t => t.severity === 'HIGH').length,
    active: threatsData.value.filter(t => t.status === 'ACTIVE').length
  }

  return [
    { label: '威胁总数', value: stats.total, color: '#409eff', icon: markRaw(InfoFilled) },
    { label: '危急威胁', value: stats.critical, color: '#dc3545', icon: markRaw(Warning) },
    { label: '高危威胁', value: stats.high, color: '#fd7e14', icon: markRaw(Warning) },
    { label: '活跃威胁', value: stats.active, color: '#f56c6c', icon: markRaw(Connection) }
  ]
})

// 生命周期钩子
onMounted(() => {
  fetchData()
})

// 方法
const fetchData = async () => {
  loading.value = true
  try {
    const response = await getThreatIntelligence(100)
    // 确保返回的是数组，如果不是则转换为数组或使用默认值
    const data = Array.isArray(response.data) ? response.data : 
                response.data ? [response.data] : []
    
    threatsData.value = data
    updateThreatTypeChart()
    updateThreatTrendChart()
  } catch (error) {
    ElMessage.error('获取威胁情报数据失败')
    console.error('获取数据失败:', error)
    // 如果API调用失败，使用模拟数据
    threatsData.value = generateMockThreatData()
    updateThreatTypeChart()
    updateThreatTrendChart()
  } finally {
    loading.value = false
  }
}

// 生成模拟威胁数据
const generateMockThreatData = () => {
  const threatTypes = ['恶意软件', '钓鱼网站', 'DDoS攻击', 'SQL注入', 'XSS攻击', '暴力破解']
  const iocTypes = ['IP', 'DOMAIN', 'URL', 'FILE_HASH', 'EMAIL']
  const severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']
  const statuses = ['ACTIVE', 'RESOLVED', 'IGNORED', 'INVESTIGATING']
  const sources = ['内部IDS', '外部威胁情报', '沙箱分析', '用户举报', '自动化扫描']
  
  const mockData = []
  
  for (let i = 0; i < 50; i++) {
    const randomType = threatTypes[Math.floor(Math.random() * threatTypes.length)]
    const randomIocType = iocTypes[Math.floor(Math.random() * iocTypes.length)]
    const randomSeverity = severities[Math.floor(Math.random() * severities.length)]
    const randomStatus = statuses[Math.floor(Math.random() * statuses.length)]
    const randomSource = sources[Math.floor(Math.random() * sources.length)]
    
    mockData.push({
      threatId: `THREAT-${String(i + 1).padStart(4, '0')}`,
      threatType: randomType,
      iocType: randomIocType,
      iocValue: generateIocValue(randomIocType, i + 1),
      severity: randomSeverity,
      confidenceScore: Math.floor(Math.random() * 50) + 50,
      source: randomSource,
      status: randomStatus,
      discoveredTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
    })
  }
  
  return mockData
}

// 生成模拟IOC值
const generateIocValue = (iocType: string, index: number) => {
  switch (iocType) {
    case 'IP':
      return `${Math.floor(Math.random() * 64) + 192}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}`
    case 'DOMAIN':
      return `malicious-domain-${index}.com`
    case 'URL':
      return `http://malicious-site-${index}.com/payload`
    case 'FILE_HASH':
      return `a1b2c3d4e5f6${String(index).padStart(4, '0')}`
    case 'EMAIL':
      return `threat${index}@malicious.com`
    default:
      return `unknown-ioc-${index}`
  }
}

const updateThreatTypeChart = async () => {
  await nextTick()
  
  if (threatTypeChartInstance) {
    threatTypeChartInstance.destroy()
  }

  const ctx = threatTypeChart.value?.getContext('2d')
  if (!ctx) return

  // 统计威胁类型
  const typeCount: {[key: string]: number} = {}
  threatsData.value.forEach(threat => {
    const type = threat.threatType || '未知'
    typeCount[type] = (typeCount[type] || 0) + 1
  })

  threatTypeChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: Object.keys(typeCount),
      datasets: [{
        data: Object.values(typeCount),
        backgroundColor: [
          '#dc3545', '#fd7e14', '#ffc107', '#28a745', '#17a2b8',
          '#6610f2', '#e83e8c', '#6f42c1', '#20c997', '#6c757d'
        ],
        borderWidth: 2,
        borderColor: '#fff'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: {
            padding: 15,
            font: { size: 12 }
          }
        }
      },
      // 添加安全的事件处理配置
      events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
    }
  })
}

const updateThreatTrendChart = async () => {
  await nextTick()
  
  if (threatTrendChartInstance) {
    threatTrendChartInstance.destroy()
  }

  const ctx = threatTrendChart.value?.getContext('2d')
  if (!ctx) return

  // 生成模拟趋势数据
  const hours = parseInt(trendInterval.value)
  const labels = []
  const data = []
  
  for (let i = hours - 1; i >= 0; i--) {
    const time = new Date(Date.now() - i * 60 * 60 * 1000)
    labels.push(time.getHours() + ':00')
    data.push(Math.floor(Math.random() * 50) + 10)
  }

  threatTrendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: '威胁数量',
        data,
        borderColor: '#409eff',
        backgroundColor: 'rgba(64, 158, 255, 0.1)',
        tension: 0.4,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: '威胁数量'
          }
        }
      }
    }
  })
}

// 事件处理
const refreshData = () => {
  fetchData()
  ElMessage.success('数据已刷新')
}

const handleFilterChange = () => {
  currentPage.value = 1
}

const handleTrendChange = () => {
  updateThreatTrendChart()
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

const handleRowClick = (row: any) => {
  viewThreatDetail(row)
}

const viewThreatDetail = (threat: any) => {
  // 验证威胁对象是否有效
  if (!threat) {
    ElMessage.error('威胁数据不存在')
    console.error('威胁对象为空')
    return
  }

  // 检查威胁ID - 支持多种字段名
  let threatId = threat.threatId || threat.id || threat.threat_id

  if (!threatId) {
    ElMessage.error('威胁情报ID不存在，无法查看详情')
    console.error('威胁对象缺少ID字段:', threat)
    console.error('可用字段:', Object.keys(threat))
    return
  }

  if (import.meta.env.DEV) {
    console.log('跳转到威胁情报详情，threatId:', threatId)
    console.log('威胁对象:', threat)
  }

  // 跳转到详情页
  router.push({
    name: 'ThreatIntelligenceDetail',
    params: { threatId: threatId }
  })
}

const toggleThreatStatus = async (threat: any) => {
  const action = threat.status === 'ACTIVE' ? '忽略' : '激活'
  try {
    await ElMessageBox.confirm(`确定要${action}这个威胁吗？`, '确认操作', {
      type: 'warning'
    })
    
    // 更新状态（这里应该调用API）
    threat.status = threat.status === 'ACTIVE' ? 'IGNORED' : 'ACTIVE'
    ElMessage.success(`威胁已${action}`)
  } catch {
    // 用户取消
  }
}

// 辅助函数
const getThreatTypeColor = (type: string) => {
  const colorMap: {[key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger'} = {
    '恶意软件': 'danger',
    '钓鱼网站': 'warning',
    'DDoS攻击': 'danger',
    'SQL注入': 'warning',
    'XSS攻击': 'warning'
  }
  return colorMap[type] || 'info'
}

const getSeverityType = (severity: string) => {
  const typeMap: {[key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger'} = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'warning',
    'LOW': 'success',
    'INFO': 'info'
  }
  return typeMap[severity] || 'info'
}

const getSeverityText = (severity: string) => {
  const textMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高危',
    'MEDIUM': '中危',
    'LOW': '低危',
    'INFO': '信息'
  }
  return textMap[severity] || severity
}

const getStatusType = (status: string) => {
  const typeMap: {[key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger'} = {
    'ACTIVE': 'danger',
    'RESOLVED': 'success',
    'IGNORED': 'info',
    'INVESTIGATING': 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: {[key: string]: string} = {
    'ACTIVE': '活跃',
    'RESOLVED': '已解决',
    'IGNORED': '已忽略',
    'INVESTIGATING': '调查中'
  }
  return textMap[status] || status
}

const getConfidenceColor = (confidenceScore?: number) => {
  if (!confidenceScore) return '#909399'
  if (confidenceScore >= 80) return '#67c23a'
  if (confidenceScore >= 60) return '#e6a23c'
  return '#f56c6c'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.threat-intelligence {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.header-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-section, .trend-section {
  min-height: 400px;
}

.chart-container {
  position: relative;
  height: 300px;
}

.threat-list-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.table-row-hover:hover {
  background-color: #f5f7fa !important;
  cursor: pointer;
}

.ioc-value {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.confidence-text {
  margin-left: 8px;
  font-size: 12px;
  color: #606266;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-controls {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-row {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}
</style>