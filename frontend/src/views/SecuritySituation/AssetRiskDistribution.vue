<template>
  <div class="asset-risk-distribution">
    <div class="page-header">
      <h2>资产风险分布</h2>
      <div class="header-controls">
        <el-select v-model="riskViewType" @change="handleViewTypeChange" style="width: 150px">
          <el-option label="按风险等级" value="riskLevel" />
          <el-option label="按资产类型" value="assetType" />
          <el-option label="按地理位置" value="location" />
        </el-select>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <div class="content-layout">
      <!-- 图表区域 -->
      <div class="chart-section">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ getChartTitle() }}</span>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="riskChart" height="400"></canvas>
          </div>
        </el-card>
      </div>

      <!-- 数据表格 -->
      <div class="table-section">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>资产风险详情</span>
              <div class="header-actions">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索资产名称或IP"
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
            :data="filteredAssetData"
            stripe
            v-loading="loading"
            @row-click="handleRowClick"
            row-class-name="table-row-hover"
          >
            <el-table-column prop="assetName" label="资产名称" min-width="150" />
            <el-table-column prop="assetType" label="资产类型" width="120" />
            <el-table-column prop="ipAddress" label="IP地址" width="130" />
            <el-table-column prop="riskScore" label="风险分数" width="120">
              <template #default="scope">
                <el-progress 
                  :percentage="Number(scope.row.riskScore)" 
                  :color="getProgressColor(scope.row.riskScore)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template #default="scope">
                <el-tag :type="getRiskLevelType(scope.row.riskLevel)" size="small">
                  {{ getRiskLevelText(scope.row.riskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" width="120" />
            <el-table-column prop="lastScanTime" label="最后扫描" width="150">
              <template #default="scope">
                {{ formatDate(scope.row.lastScanTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click.stop="viewAssetDetail(scope.row)">
                  详情
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
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" v-for="(stat, index) in riskStats" :key="index">
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted, watch, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import {
  Refresh,
  Search,
  Warning,
  InfoFilled,
  SuccessFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { 
  Chart, 
  ChartConfiguration, 
  CategoryScale, 
  LinearScale, 
  LineElement, 
  PointElement, 
  Title, 
  Tooltip, 
  Legend,
  DoughnutController,
  ArcElement
} from 'chart.js'
import { getAssetRiskDistribution } from '@/api/security-situation'
import { debounce } from 'lodash-es'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, DoughnutController, ArcElement)

// 响应式数据
const riskViewType = ref('riskLevel')
const assetData = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)

// 图表引用
const riskChart = ref<HTMLCanvasElement>()
let riskChartInstance: Chart | null = null

// 路由
const router = useRouter()

// 计算属性
const filteredAssetData = computed(() => {
  // 创建assetData的副本以避免循环引用
  const assetDataCopy = [...assetData.value].map(item => ({ ...item }))
  
  let data = assetDataCopy
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(asset => 
      asset.assetName?.toLowerCase().includes(keyword) ||
      asset.ipAddress?.toLowerCase().includes(keyword)
    )
  }
  
  totalCount.value = data.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return data.slice(start, end)
})

const riskStats = computed(() => {
  // 创建assetData的副本以避免循环引用
  const assetDataCopy = [...assetData.value].map(item => ({ ...item }))
  
  const stats = {
    CRITICAL: { count: 0, color: '#dc3545' },
    HIGH: { count: 0, color: '#fd7e14' },
    MEDIUM: { count: 0, color: '#ffc107' },
    LOW: { count: 0, color: '#28a745' },
    SAFE: { count: 0, color: '#17a2b8' }
  }
  
  assetDataCopy.forEach(asset => {
    const level = asset.riskLevel || 'SAFE'
    if (stats[level as keyof typeof stats]) {
      stats[level as keyof typeof stats].count++
    }
  })
  
  return [
    { label: '危急资产', value: stats.CRITICAL.count, color: stats.CRITICAL.color, icon: markRaw(Warning) },
    { label: '高风险资产', value: stats.HIGH.count, color: stats.HIGH.color, icon: markRaw(Warning) },
    { label: '中风险资产', value: stats.MEDIUM.count, color: stats.MEDIUM.color, icon: markRaw(InfoFilled) },
    { label: '安全资产', value: stats.SAFE.count, color: stats.SAFE.color, icon: markRaw(SuccessFilled) }
  ]
})

// 生命周期钩子
onMounted(async () => {
  await fetchData() // 立即执行首次数据加载，不使用防抖
})

onUnmounted(() => {
  // 组件卸载时清理图表实例
  if (riskChartInstance) {
    try {
      riskChartInstance.destroy()
    } catch (e) {
      console.warn('组件卸载时销毁图表实例出错:', e)
    }
    riskChartInstance = null
  }
})

// 方法
const fetchData = async () => {
  loading.value = true
  try {
    const response = await getAssetRiskDistribution()
    // 确保返回的是数组，如果不是则转换为数组
    const data = Array.isArray(response.data) ? response.data : 
                response.data ? [response.data] : []
    
    assetData.value = data
    // 使用防抖函数来限制更新频率
    updateChart()
  } catch (error) {
    ElMessage.error('获取资产风险数据失败')
    console.error('获取数据失败:', error)
    // 如果API调用失败，使用模拟数据
    assetData.value = generateMockAssetData()
    updateChart()
  } finally {
    loading.value = false
  }
}

const updateChart = debounce(async () => {
  await nextTick()
  
  if (riskChartInstance) {
    try {
      riskChartInstance.destroy()
    } catch (e) {
      console.warn('销毁图表实例时出错:', e)
    }
    riskChartInstance = null // 确保实例被正确清理
  }

  const ctx = riskChart.value?.getContext('2d')
  if (!ctx) return

  // 创建独立的配置对象，避免循环引用
  let chartConfig: any = {}

  if (riskViewType.value === 'riskLevel') {
    chartConfig = getRiskLevelChartConfig()
  } else if (riskViewType.value === 'assetType') {
    chartConfig = getAssetTypeChartConfig()
  } else if (riskViewType.value === 'location') {
    chartConfig = getLocationChartConfig()
  }

  try {
    // 使用普通对象而不是响应式对象创建图表配置
    const plainConfig = JSON.parse(JSON.stringify(chartConfig))
    riskChartInstance = new Chart(ctx, plainConfig)
  } catch (error) {
    console.error('创建图表时出错:', error)
    // 如果创建图表失败，使用基础配置
    try {
      riskChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: [],
          datasets: [{
            label: '资产数量',
            data: [],
            backgroundColor: '#409eff'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false
        }
      } as ChartConfiguration<'bar'>)
    } catch (fallbackError) {
      console.error('创建基础图表也失败:', fallbackError)
    }
  }
}, 300)

const getRiskLevelChartConfig = () => {
  // 创建assetData的副本以避免循环引用
  const assetDataCopy = [...assetData.value].map(item => ({ ...item }))
  
  const riskLevels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE']
  const counts = riskLevels.map(level => 
    assetDataCopy.filter(asset => asset.riskLevel === level).length
  )

  // 使用普通对象而不是响应式对象创建配置
  return {
    type: 'doughnut',
    data: {
      labels: [...riskLevels.map(level => getRiskLevelText(level))], // 创建新数组
      datasets: [{
        data: [...counts], // 创建新数组
        backgroundColor: ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#17a2b8'],
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
        },
        tooltip: {
          callbacks: {
            label: (context: any) => {
              // 安全检查context对象
              if (!context || !context.dataset || !context.parsed) return ''
              const total = context.dataset.data.reduce((a: number, b: number) => a + b, 0)
              const percentage = ((context.parsed / total) * 100).toFixed(1)
              return `${context.label}: ${context.parsed} (${percentage}%)`
            }
          }
        }
      },
      // 添加安全的事件处理配置
      events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
    }
  }
}

const getAssetTypeChartConfig = () => {
  // 创建assetData的副本以避免循环引用
  const assetDataCopy = [...assetData.value].map(item => ({ ...item }))
  
  const typeCount: {[key: string]: number} = {}
  assetDataCopy.forEach(asset => {
    const type = asset.assetType || '未知'
    typeCount[type] = (typeCount[type] || 0) + 1
  })

  // 使用普通对象而不是响应式对象创建配置
  return {
    type: 'bar',
    data: {
      labels: [...Object.keys(typeCount)], // 创建新数组
      datasets: [{
        label: '资产数量',
        data: [...Object.values(typeCount)], // 创建新数组
        backgroundColor: '#409eff',
        borderColor: '#409eff',
        borderWidth: 1
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
            text: '资产数量'
          }
        }
      }
    }
  }
}

const getLocationChartConfig = () => {
  // 创建assetData的副本以避免循环引用
  const assetDataCopy = [...assetData.value].map(item => ({ ...item }))
  
  const locationCount: {[key: string]: number} = {}
  assetDataCopy.forEach(asset => {
    const location = asset.location || '未知位置'
    locationCount[location] = (locationCount[location] || 0) + 1
  })

  // 使用普通对象而不是响应式对象创建配置
  return {
    type: 'pie',
    data: {
      labels: [...Object.keys(locationCount)], // 创建新数组
      datasets: [{
        data: [...Object.values(locationCount)], // 创建新数组
        backgroundColor: [
          '#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd',
          '#8c564b', '#e377c2', '#7f7f7f', '#bcbd22', '#17becf'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right'
        }
      }
    }
  }
}

// 事件处理
const refreshData = () => {
  fetchData() // 直接调用，不使用防抖以立即刷新
  ElMessage.success('数据已刷新')
}

const handleViewTypeChange = debounce(() => {
  // 使用防抖函数来限制更新频率
  updateChart()
}, 300)

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
  viewAssetDetail(row)
}

const viewAssetDetail = (asset: any) => {
  router.push(`/security-situation/asset/${asset.assetId}`)
}

// 辅助函数
const getChartTitle = () => {
  const titles: Record<string, string> = {
    riskLevel: '资产风险等级分布',
    assetType: '资产类型分布',
    location: '资产地理分布'
  }
  return titles[riskViewType.value] || '资产分布'
}

const getRiskLevelText = (level: string) => {
  const levelMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'SAFE': '安全'
  }
  return levelMap[level] || level
}

const getRiskLevelType = (level: string) => {
  const typeMap: Record<string, any> = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'warning',
    'LOW': 'success',
    'SAFE': 'success'
  }
  return typeMap[level] || 'info'
}

const getProgressColor = (value: number) => {
  if (value >= 80) return '#dc3545'
  if (value >= 60) return '#fd7e14'
  if (value >= 40) return '#ffc107'
  if (value >= 20) return '#28a745'
  return '#17a2b8'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return '从未'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生成模拟资产数据
const generateMockAssetData = () => {
  const assetTypes = ['服务器', '网络设备', '工作站', '数据库', '应用系统', '移动设备']
  const locations = ['北京数据中心', '上海机房', '广州办公室', '深圳研发中心', '成都分部', '武汉分公司']
  const riskLevels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE']
  
  const mockData = []
  
  for (let i = 0; i < 50; i++) {
    const randomType = assetTypes[Math.floor(Math.random() * assetTypes.length)]
    const randomLocation = locations[Math.floor(Math.random() * locations.length)]
    const randomRiskLevel = riskLevels[Math.floor(Math.random() * riskLevels.length)]
    
    mockData.push({
      assetId: `asset_${i + 1}`,
      assetName: `${randomType}${i + 1}`,
      assetType: randomType,
      ipAddress: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
      location: randomLocation,
      riskScore: Math.floor(Math.random() * 100),
      riskLevel: randomRiskLevel,
      lastScanTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
    })
  }
  
  return mockData
}
</script>

<style scoped>
.asset-risk-distribution {
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

.content-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-section {
  min-height: 500px;
}

.table-section {
  min-height: 500px;
}

.chart-container {
  position: relative;
  height: 400px;
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

.table-row-hover:hover {
  background-color: #f5f7fa !important;
  cursor: pointer;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #fff;
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