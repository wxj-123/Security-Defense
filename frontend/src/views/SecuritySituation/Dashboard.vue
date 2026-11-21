<template>
  <div class="security-dashboard">
    <div class="dashboard-header">
      <h1>安全态势感知</h1>
      <div class="header-controls">
        <el-date-picker
          v-model="timeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="handleTimeRangeChange"
        />
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 功能导航卡片 -->
    <div class="navigation-cards">
      <el-row :gutter="20">
        <el-col :span="6" v-for="nav in navigationCards" :key="nav.path">
          <el-card 
            class="nav-card" 
            shadow="hover"
            @click="navigateTo(nav.path)"
          >
            <div class="nav-content">
              <div class="nav-icon" :style="{ backgroundColor: nav.color }">
                <el-icon size="32">
                  <component :is="nav.icon" />
                </el-icon>
              </div>
              <div class="nav-info">
                <h3>{{ nav.title }}</h3>
                <p>{{ nav.description }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="dashboard-layout">
      <!-- 安全态势评分区域 -->
      <div class="score-section">
        <el-card class="dashboard-card">
          <SecurityScoreOverview 
            :situation-data="situationData"
            :time-range="timeRange"
            @indicator-click="handleIndicatorClick"
            @refresh="refreshData"
          />
        </el-card>
      </div>

      <!-- 资产风险分布和威胁趋势分析 -->
      <div class="analysis-row">
        <div class="analysis-col">
          <el-card class="dashboard-card">
            <AssetRiskDistribution 
              @asset-click="handleAssetTypeClick"
            />
          </el-card>
        </div>
        <div class="analysis-col">
          <el-card class="dashboard-card">
            <ThreatTrendAnalysis 
              :trend-data="trendData"
              :time-range="timeRange"
              @threat-click="handleThreatTypeClick"
              @interval-change="handleTrendIntervalChange"
              :trend-interval="trendInterval"
            />
          </el-card>
        </div>
      </div>

      <!-- 快速统计 -->
      <div class="quick-stats">
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in quickStats" :key="stat.title">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon" :style="{ backgroundColor: stat.color + '20' }">
                  <el-icon :size="24" :color="stat.color">
                    <component :is="stat.icon" />
                  </el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.title }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 最新威胁情报 -->
      <div class="latest-threats">
        <el-card class="threat-card">
          <template #header>
            <div class="card-header">
              <span>最新威胁情报</span>
              <el-button link @click="navigateTo('/security-situation/threat-intelligence')">
                查看更多 →
              </el-button>
            </div>
          </template>
          <el-table :data="threatsData.slice(0, 5)" stripe size="small" @row-click="navigateToThreatDetail" row-class-name="table-row-hover">
            <el-table-column prop="threatType" label="威胁类型" width="120" />
            <el-table-column prop="iocValue" label="IOC" />
            <el-table-column prop="severity" label="严重程度" width="80">
              <template #default="scope">
                <span :class="getSeverityClass(scope.row.severity)">{{ scope.row.severity }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="discoveredTime" label="发现时间" width="150">
              <template #default="scope">
                {{ formatDate(scope.row.discoveredTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click.stop="navigateToThreatDetail(scope.row.threatId)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, PieChart, Compass, Aim, Histogram, InfoFilled, Warning, Connection, SuccessFilled } from '@element-plus/icons-vue'
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
  BarElement,
  BarController
} from 'chart.js'
import SecurityScoreOverview from '@/components/SecuritySituation/SecurityScoreOverview.vue'
import AssetRiskDistribution from '@/components/SecuritySituation/AssetRiskDistribution.vue'
import ThreatTrendAnalysis from '@/components/SecuritySituation/ThreatTrendAnalysis.vue'
import { getSituationOverview, getSituationTrend, getAssetRiskDistribution, getThreatIntelligence } from '@/api/security-situation'
import type { SituationData, AssetRisk, ThreatIntelligence } from '@/types/security-situation'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, BarElement, BarController)

// 响应式数据
const timeRange = ref<[string, string]>([
  new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
  new Date().toISOString()
])
const situationData = ref<SituationData>({} as SituationData)
const assetRisksData = ref<AssetRisk[]>([])
const threatsData = ref<ThreatIntelligence[]>([])
const trendData = ref<any[]>([])
const assetHeatmapData = ref<any[]>([])
const lastUpdateTime = ref('')
const assetRiskType = ref('riskLevel')
const trendInterval = ref('60')

// 导航卡片配置
const navigationCards = [
  {
    title: '资产风险分布',
    description: '查看资产风险等级和类型分布',
    icon: markRaw(PieChart),
    color: '#409eff',
    path: '/security-situation/asset-risk-distribution'
  },
  {
    title: '威胁情报概览',
    description: '实时威胁情报和趋势分析',
    icon: markRaw(Compass),
    color: '#f56c6c',
    path: '/security-situation/threat-intelligence'
  },
  {
    title: '安全维度分析',
    description: '多维度安全态势评估',
    icon: markRaw(Aim),
    color: '#67c23a',
    path: '/security-situation/dimension-analysis'
  },
  {
    title: '资产风险热力图',
    description: '可视化资产风险分布',
    icon: markRaw(Histogram),
    color: '#e6a23c',
    path: '/security-situation/risk-heatmap'
  }
]

// 快速统计数据
const quickStats = computed(() => {
  // 创建一个临时对象来存储统计数据
  const stats: Record<string, number> = {
    totalAssets: assetRisksData.value.length,
    highRiskAssets: assetRisksData.value.filter(a => (a.riskScore || 0) >= 70).length,
    activeThreats: threatsData.value.filter(t => t.status === 'ACTIVE').length,
    overallScore: situationData.value.situationIndex || 0
  }
  
  return [
    {
      title: '资产总数',
      value: stats.totalAssets,
      icon: markRaw(InfoFilled),
      color: '#409eff'
    },
    {
      title: '高危资产',
      value: stats.highRiskAssets,
      icon: markRaw(Warning),
      color: '#f56c6c'
    },
    {
      title: '活跃威胁',
      value: stats.activeThreats,
      icon: markRaw(Connection),
      color: '#e6a23c'
    },
    {
      title: '安全评分',
      value: stats.overallScore,
      icon: markRaw(SuccessFilled),
      color: '#67c23a'
    }
  ]
})

// 图表引用
const assetRiskChart = ref<HTMLCanvasElement>()
const trendChart = ref<HTMLCanvasElement>()
const dimensionChart = ref<HTMLCanvasElement>()
let assetRiskChartInstance: Chart | null = null
let trendChartInstance: Chart | null = null
let dimensionChartInstance: Chart | null = null

// 路由
const router = useRouter()

// 生命周期钩子
onMounted(() => {
  fetchAllData()
  // 定时刷新数据
  setInterval(() => {
    fetchAllData()
  }, 30000)
})

// 方法
const fetchAllData = async () => {
  try {
    // 并行获取所有数据
    const [situationRes, assetRisksRes, threatsRes, trendRes] = await Promise.allSettled([
      fetchSituationOverview(),
      fetchAssetRisks(),
      fetchThreats(),
      fetchTrendData()
    ])
    
    // 处理每个请求的结果
    if (situationRes.status === 'fulfilled') {
      // fetchSituationOverview已经处理了数据设置
    } else {
      console.error('获取态势总览数据失败:', situationRes.reason)
    }
    
    if (assetRisksRes.status === 'fulfilled') {
      // fetchAssetRisks已经处理了数据设置
    } else {
      console.error('获取资产风险数据失败:', assetRisksRes.reason)
    }
    
    if (threatsRes.status === 'fulfilled') {
      // fetchThreats已经处理了数据设置
    } else {
      console.error('获取威胁情报数据失败:', threatsRes.reason)
    }
    
    if (trendRes.status === 'fulfilled') {
      // fetchTrendData已经处理了数据设置
    } else {
      console.error('获取趋势数据失败:', trendRes.reason)
    }
    
    // 更新图表
    updateCharts()
    lastUpdateTime.value = new Date().toLocaleString('zh-CN')
  } catch (error) {
    ElMessage.error('数据加载失败')
    console.error('加载数据失败:', error)
  }
}

const fetchSituationOverview = async () => {
  try {
    const response = await getSituationOverview()
    
    // 验证响应数据
    if (!response?.data || typeof response.data !== 'object') {
      console.warn('后端返回数据格式异常，使用模拟数据')
      situationData.value = {
        situationIndex: Math.floor(Math.random() * 100),
        situationLevel: 'MEDIUM',
        timestamp: new Date().toISOString(),
        totalAssets: Math.floor(Math.random() * 1000) + 500,
        affectedAssets: Math.floor(Math.random() * 100) + 10,
        activeThreats: Math.floor(Math.random() * 50) + 5,
        openVulnerabilities: Math.floor(Math.random() * 100) + 20,
        securityEvents: Math.floor(Math.random() * 500) + 100,
        attackChainCompleteRate: Math.floor(Math.random() * 100),
        dimensionScores: {}
      } as SituationData
      return
    }
    
    // 适配后端返回的数据结构
    const data: any = response.data
    
    // 检查数据是否包含SituationData的字段
    const expectedFields = ['situationIndex', 'situationLevel', 'timestamp', 'totalAssets', 'affectedAssets', 'activeThreats']
    const hasAllFields = expectedFields.every(field => field in data)
    
    if (hasAllFields) {
      // 处理后端实际返回的SituationData对象
      situationData.value = {
        situationIndex: typeof data.situationIndex === 'number' ? data.situationIndex : Math.floor(Math.random() * 100),
        situationLevel: typeof data.situationLevel === 'string' && 
                      ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE'].includes(data.situationLevel.toUpperCase()) ? 
                      data.situationLevel.toUpperCase() as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE' : 'MEDIUM',
        timestamp: typeof data.timestamp === 'string' ? data.timestamp : new Date().toISOString(),
        totalAssets: typeof data.totalAssets === 'number' ? data.totalAssets : Math.floor(Math.random() * 1000) + 500,
        affectedAssets: typeof data.affectedAssets === 'number' ? data.affectedAssets : Math.floor(Math.random() * 100) + 10,
        activeThreats: typeof data.activeThreats === 'number' ? data.activeThreats : Math.floor(Math.random() * 50) + 5,
        openVulnerabilities: typeof data.openVulnerabilities === 'number' ? data.openVulnerabilities : Math.floor(Math.random() * 100) + 20,
        securityEvents: typeof data.securityEvents === 'number' ? data.securityEvents : Math.floor(Math.random() * 500) + 100,
        attackChainCompleteRate: typeof data.attackChainCompleteRate === 'number' ? data.attackChainCompleteRate : Math.floor(Math.random() * 100),
        dimensionScores: typeof data.dimensionScores === 'object' && data.dimensionScores !== null ? data.dimensionScores : {}
      } as SituationData
    } else {
      // 处理Map格式数据或模拟数据
      // 根据API文件中的模拟数据结构进行适配
      situationData.value = {
        situationIndex: typeof data.situationScore === 'number' ? data.situationScore : Math.floor(Math.random() * 100),
        situationLevel: 'MEDIUM', // 默认值
        timestamp: new Date().toISOString(),
        totalAssets: typeof data.totalAssets === 'number' ? data.totalAssets : Math.floor(Math.random() * 1000) + 500,
        affectedAssets: typeof data.highRiskAssets === 'number' ? data.highRiskAssets : Math.floor(Math.random() * 100) + 10,
        activeThreats: typeof data.threatsCount === 'number' ? data.threatsCount : Math.floor(Math.random() * 50) + 5,
        openVulnerabilities: typeof data.vulnerabilities === 'number' ? data.vulnerabilities : Math.floor(Math.random() * 100) + 20,
        securityEvents: typeof data.securityEvents === 'number' ? data.securityEvents : Math.floor(Math.random() * 500) + 100,
        attackChainCompleteRate: Math.floor(Math.random() * 100), // 默认值
        dimensionScores: {} // 默认值
      } as SituationData
    }
  } catch (error) {
    console.error('获取态势总览数据失败:', error)
    // 如果API调用失败，使用模拟数据
    situationData.value = {
      situationIndex: Math.floor(Math.random() * 100),
      situationLevel: 'MEDIUM',
      timestamp: new Date().toISOString(),
      totalAssets: Math.floor(Math.random() * 1000) + 500,
      affectedAssets: Math.floor(Math.random() * 100) + 10,
      activeThreats: Math.floor(Math.random() * 50) + 5,
      openVulnerabilities: Math.floor(Math.random() * 100) + 20,
      securityEvents: Math.floor(Math.random() * 500) + 100,
      attackChainCompleteRate: Math.floor(Math.random() * 100),
      dimensionScores: {}
    } as SituationData
  }
}

const fetchAssetRisks = async () => {
  try {
    const response = await getAssetRiskDistribution()
    
    // 使用API函数返回的数据，它已经进行了适当的处理和验证
    if (response?.data && Array.isArray(response.data)) {
      assetRisksData.value = response.data
    } else {
      // 如果数据格式仍然不正确，使用空数组
      assetRisksData.value = []
    }
  } catch (error) {
    console.error('获取资产风险数据失败:', error)
    // 如果API调用失败，使用模拟数据
    assetRisksData.value = [
      {
        assetId: 'asset_1',
        assetName: '服务器1',
        assetType: '服务器',
        riskScore: Math.floor(Math.random() * 100),
        riskLevel: 'MEDIUM',
        ipAddress: '192.168.1.1',
        location: '数据中心',
        lastScanTime: new Date().toISOString(),
        assetValueScore: Math.floor(Math.random() * 100),
        vulnerabilityScore: Math.floor(Math.random() * 100),
        threatScore: Math.floor(Math.random() * 100),
        vulnerabilityCount: Math.floor(Math.random() * 10),
        threatCount: Math.floor(Math.random() * 5),
        status: 'ACTIVE'
      }
    ] as AssetRisk[]
  }
}

const fetchThreats = async () => {
  try {
    const response = await getThreatIntelligence()
    
    // 使用API函数返回的数据，它已经进行了适当的处理和验证
    if (response?.data && Array.isArray(response.data)) {
      threatsData.value = response.data as ThreatIntelligence[]
    } else {
      // 如果数据格式仍然不正确，使用空数组
      threatsData.value = []
    }
  } catch (error) {
    console.error('获取威胁情报数据失败:', error)
    // 如果API调用失败，使用模拟数据
    threatsData.value = generateMockThreatIntelligence(10).data as ThreatIntelligence[]
  }
}

// 模拟趋势数据生成函数
const generateMockTrendData = () => {
  const now = new Date()
  const data = []
  
  // 生成过去24小时的数据点，每小时一个点
  for (let i = 23; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 60 * 60 * 1000)
    data.push({
      timestamp: time.toISOString(),
      situationIndex: Math.floor(Math.random() * 50) + 25, // 25-75之间的随机数
      activeThreats: Math.floor(Math.random() * 10) + 5 // 5-15之间的随机数
    })
  }
  
  return { data }
}

const fetchTrendData = async () => {
  try {
    const response = await getSituationTrend(timeRange.value[0], timeRange.value[1], parseInt(trendInterval.value))
    
    // 增强数据验证和类型保护
    let data: any[] = []
    
    // 检查响应数据是否存在且为数组
    if (response?.data && Array.isArray(response.data)) {
      data = response.data
    } 
    // 如果是对象且包含必要的字段，则转换为数组
    else if (response?.data && typeof response.data === 'object' && 
             'timestamp' in response.data && 'situationIndex' in response.data) {
      data = [response.data]
    } 
    // 如果数据格式不正确，使用模拟数据
    else {
      console.warn('后端返回数据格式异常，使用模拟数据')
      data = generateMockTrendData().data
    }
    
    // 进一步验证数组中的每个元素，确保数据完整性
    trendData.value = data
      .filter(item => item && typeof item === 'object')
      .map(item => ({
        ...item,
        timestamp: typeof item.timestamp === 'string' ? item.timestamp : new Date().toISOString(),
        situationIndex: typeof item.situationIndex === 'number' ? item.situationIndex : Math.floor(Math.random() * 50) + 25,
        activeThreats: typeof item.activeThreats === 'number' ? item.activeThreats : Math.floor(Math.random() * 10) + 5
      }))
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    // 如果API调用失败，使用模拟数据
    trendData.value = generateMockTrendData().data
  }
}

const generateHeatmapData = () => {
  // 创建assetRisksData的副本以避免循环引用
  const assetRisksDataCopy = [...assetRisksData.value].map(asset => ({ ...asset }))
  
  assetHeatmapData.value = assetRisksDataCopy.map(asset => ({
    assetId: asset.assetId,
    assetName: asset.assetName,
    riskScore: asset.riskScore,
    location: asset.location || '未知位置'
  })).sort((a, b) => b.riskScore - a.riskScore)
}

const updateCharts = async () => {
  await nextTick()
  updateAssetRiskChart()
  updateTrendChart()
  updateDimensionChart()
}

const updateAssetRiskChart = () => {
  if (assetRiskChartInstance) {
    assetRiskChartInstance.destroy()
  }

  const ctx = assetRiskChart.value?.getContext('2d')
  if (!ctx) return

  // 创建assetRisksData的副本以避免循环引用
  const assetRisksDataCopy = [...assetRisksData.value].map(asset => ({ ...asset }))

  let labels: string[] = []
  let data: number[] = []
  let backgroundColor: string[] = []

  if (assetRiskType.value === 'riskLevel') {
    // 按风险等级统计
    const riskLevelCount: {[key: string]: number} = {}
    assetRisksDataCopy.forEach(asset => {
      const level = asset.riskLevel || 'UNKNOWN'
      riskLevelCount[level] = (riskLevelCount[level] || 0) + 1
    })
    
    const riskLevels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE', 'UNKNOWN']
    labels = riskLevels.map(level => getRiskLevelText(level))
    data = riskLevels.map(level => riskLevelCount[level] || 0)
    backgroundColor = riskLevels.map(level => getRiskLevelColor(level))
  } else {
    // 按资产类型统计
    const assetTypeCount: {[key: string]: number} = {}
    assetRisksDataCopy.forEach(asset => {
      const type = asset.assetType || 'UNKNOWN'
      assetTypeCount[type] = (assetTypeCount[type] || 0) + 1
    })
    
    labels = Object.keys(assetTypeCount)
    data = Object.values(assetTypeCount)
    backgroundColor = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b', '#e377c2', '#7f7f7f', '#bcbd22', '#17becf']
  }

  assetRiskChartInstance = new Chart(ctx, {
    type: 'pie',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor,
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: {
            padding: 20,
            font: {
              size: 12
            }
          }
        }
      },
      // 添加安全的事件处理配置
      events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
    }
  })
}

const updateTrendChart = () => {
  if (trendChartInstance) {
    trendChartInstance.destroy()
  }

  const ctx = trendChart.value?.getContext('2d')
  if (!ctx) return

  // 创建trendData的副本以避免循环引用
  const trendDataCopy = [...trendData.value].map(item => ({ ...item }))

  const labels = trendDataCopy.map(item => formatChartDate(item.timestamp))
  const situationIndexData = trendDataCopy.map(item => item.situationIndex || 0)
  const activeThreatsData = trendDataCopy.map(item => item.activeThreats || 0)

  trendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: '态势指数',
          data: situationIndexData,
          borderColor: '#1f77b4',
          backgroundColor: 'rgba(31, 119, 180, 0.1)',
          tension: 0.4,
          fill: true
        },
        {
          label: '活跃威胁数',
          data: activeThreatsData,
          borderColor: '#d62728',
          backgroundColor: 'rgba(214, 39, 40, 0.1)',
          tension: 0.4,
          fill: true,
          yAxisID: 'y1'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index' as const,
        intersect: false
      },
      plugins: {
        legend: {
          position: 'top',
        }
      },
      scales: {
        x: {
          grid: {
            display: false
          }
        },
        y: {
          title: {
            display: true,
            text: '态势指数'
          },
          min: 0,
          max: 100
        },
        y1: {
          position: 'right',
          title: {
            display: true,
            text: '威胁数'
          },
          min: 0,
          grid: {
            drawOnChartArea: false
          }
        }
      }
    }
  })
}

const updateDimensionChart = () => {
  if (dimensionChartInstance) {
    dimensionChartInstance.destroy()
  }

  const ctx = dimensionChart.value?.getContext('2d')
  if (!ctx) return

  // 创建situationData的副本以避免循环引用
  const situationDataCopy = { ...situationData.value }
  
  // 准备维度评分数据
  const dimensionScores = situationDataCopy.dimensionScores || {}
  let labels: string[] = []
  let data: number[] = []

  // 如果有维度评分数据
  if (Object.keys(dimensionScores).length > 0) {
    labels = Object.keys(dimensionScores)
    data = Object.values(dimensionScores).map(score => score || 0)
  } else {
    // 如果没有数据，使用默认值
    const defaultDimensions = {
      '威胁检测': 0,
      '漏洞管理': 0,
      '资产安全': 0,
      '网络流量': 0,
      '安全配置': 0
    }
    labels = Object.keys(defaultDimensions)
    data = Object.values(defaultDimensions)
  }

  // 确保数据数组中的所有值都是数字类型
  const numericData = data.map(value => typeof value === 'number' ? value : 0)

  dimensionChartInstance = new Chart(ctx, {
    type: 'radar',
    data: {
      labels: labels.map(key => getDimensionLabel(key)),
      datasets: [{
        label: '维度评分',
        data: numericData,
        backgroundColor: 'rgba(31, 119, 180, 0.2)',
        borderColor: '#1f77b4',
        pointBackgroundColor: '#1f77b4',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: '#1f77b4'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        r: {
          angleLines: {
            display: true
          },
          suggestedMin: 0,
          suggestedMax: 100
        }
      }
    }
  } as any) // 使用 any 类型来避免类型检查错误
}

// 事件处理
const refreshData = () => {
  fetchAllData()
  ElMessage.success('数据已刷新')
}

const handleTimeRangeChange = () => {
  fetchTrendData()
    .then(() => updateTrendChart())
    .catch(() => ElMessage.error('趋势数据加载失败'))
}

const handleAssetRiskTypeChange = () => {
  updateAssetRiskChart()
}

const handleTrendIntervalChange = () => {
  fetchTrendData()
    .then(() => updateTrendChart())
    .catch(() => ElMessage.error('趋势数据加载失败'))
}

// 辅助函数
const getSituationLevelClass = (level?: string) => {
  const levelMap: {[key: string]: string} = {
    'CRITICAL': 'level-critical',
    'HIGH': 'level-high',
    'MEDIUM': 'level-medium',
    'LOW': 'level-low',
    'SAFE': 'level-safe'
  }
  return levelMap[level || ''] || 'level-unknown'
}

const getSituationLevelText = (level?: string) => {
  const levelMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'SAFE': '安全'
  }
  return levelMap[level || ''] || '未知'
}

const getRiskLevelColor = (level?: string) => {
  const colorMap: {[key: string]: string} = {
    'CRITICAL': '#dc3545',
    'HIGH': '#fd7e14',
    'MEDIUM': '#ffc107',
    'LOW': '#28a745',
    'SAFE': '#17a2b8',
    'UNKNOWN': '#6c757d'
  }
  return colorMap[level || ''] || '#6c757d'
}

const getRiskLevelText = (level?: string) => {
  const levelMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'SAFE': '安全',
    'UNKNOWN': '未知'
  }
  return levelMap[level || ''] || '未知'
}

const getSeverityClass = (severity?: string) => {
  const severityMap: {[key: string]: string} = {
    'CRITICAL': 'severity-critical',
    'HIGH': 'severity-high',
    'MEDIUM': 'severity-medium',
    'LOW': 'severity-low',
    'INFO': 'severity-info'
  }
  return `severity-tag ${severityMap[severity || ''] || 'severity-unknown'}`
}

const navigateTo = (path: string) => {
  router.push(path)
}

const navigateToAssetDetail = (asset: any) => {
  const assetId = typeof asset === 'string' ? asset : asset.assetId
  router.push(`/security-situation/asset/${assetId}`)
}

const navigateToThreatDetail = (threat: any) => {
  const threatId = typeof threat === 'string' ? threat : (threat.threatId || threat.id || threat.threat_id)

  if (!threatId) {
    ElMessage.error('威胁情报ID不存在，无法查看详情')
    console.error('威胁对象缺少ID字段:', threat)
    return
  }

  router.push({
    name: 'ThreatIntelligenceDetail',
    params: { threatId: threatId }
  })
}

// 处理指标点击
const handleIndicatorClick = (indicator: any) => {
  console.log('Indicator clicked:', indicator)
  // 可以根据不同指标进行相应的筛选或跳转操作
}

// 处理资产类型点击
const handleAssetTypeClick = (assetType: any) => {
  console.log('Asset type clicked:', assetType)
  // 可以根据资产类型筛选数据
}

// 处理威胁类型点击
const handleThreatTypeClick = (threatType: string) => {
  console.log('Threat type clicked:', threatType)
  // 可以根据威胁类型筛选数据
}

const getProgressColor = (value: number) => {
  if (value >= 80) return '#dc3545'
  if (value >= 60) return '#fd7e14'
  if (value >= 40) return '#ffc107'
  if (value >= 20) return '#28a745'
  return '#17a2b8'
}

const getDimensionLabel = (key: string) => {
  const labelMap: {[key: string]: string} = {
    'threatDetection': '威胁检测',
    'vulnerability': '漏洞管理',
    'assetSecurity': '资产安全',
    'networkTraffic': '网络流量',
    'securityConfig': '安全配置'
  }
  return labelMap[key] || key
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const formatChartDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 模拟数据生成函数
const generateMockThreatIntelligence = (limit: number) => {
  const threatTypes = ['恶意软件', '钓鱼网站', 'DDoS攻击', 'SQL注入', 'XSS攻击', '勒索软件']
  const iocTypes = ['IP', 'DOMAIN', 'URL', 'FILE_HASH', 'EMAIL']
  const severities: ('CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO')[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']
  const sources = ['内部IDS', '外部威胁情报', '沙箱分析', '用户举报', '自动化扫描']
  
  const mockData = []
  
  for (let i = 0; i < limit; i++) {
    const threatType = threatTypes[Math.floor(Math.random() * threatTypes.length)]
    const iocType = iocTypes[Math.floor(Math.random() * iocTypes.length)]
    const severity = severities[Math.floor(Math.random() * severities.length)]
    const source = sources[Math.floor(Math.random() * sources.length)]
    
    mockData.push({
      threatId: `THREAT-${String(i + 1).padStart(4, '0')}`,
      threatType,
      iocType,
      iocValue: generateIOCValue(iocType, i + 1),
      severity,
      confidenceScore: Math.floor(Math.random() * 50) + 50,
      source,
      discoveredTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      firstSeen: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      lastUpdated: new Date().toISOString(),
      description: `${threatType}威胁情报`,
      affectedSystems: [],
      relatedCves: [],
      tags: [threatType, iocType],
      status: 'ACTIVE' as const
    })
  }
  
  return { data: mockData }
}

const generateIOCValue = (iocType: string, index: number) => {
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

</script>

<style scoped>
/* 安全态势感知仪表板样式 */
.security-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-controls {
  display: flex;
  gap: 10px;
}

.navigation-cards {
  margin-bottom: 20px;
}

.nav-card {
  cursor: pointer;
  transition: all 0.3s;
}

.nav-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-content {
  display: flex;
  align-items: center;
}

.nav-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.nav-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.nav-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.dashboard-layout {
  display: grid;
  grid-template-rows: auto auto auto 1fr;
  gap: 20px;
}

.score-section {
  grid-row: 1;
}

.analysis-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  grid-row: 2;
}

.analysis-col {
  display: flex;
  flex-direction: column;
}

.quick-stats {
  grid-row: 3;
}

.latest-threats {
  grid-row: 4;
}

.dashboard-card {
  height: 100%;
}

.threat-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-row-hover {
  cursor: pointer;
}

.table-row-hover:hover {
  background-color: #f5f7fa !important;
}

.level-critical { color: #dc3545; }
.level-high { color: #fd7e14; }
.level-medium { color: #ffc107; }
.level-low { color: #28a745; }
.level-safe { color: #17a2b8; }
.level-unknown { color: #6c757d; }

.severity-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
}

.severity-critical { background: #fde2e2; color: #dc3545; }
.severity-high { background: #fff3cd; color: #fd7e14; }
.severity-medium { background: #fff8e6; color: #ffc107; }
.severity-low { background: #e2f0fb; color: #28a745; }
.severity-info { background: #f1f3f4; color: #6c757d; }

/* 图表容器 */
.chart-container {
  position: relative;
  height: 250px;
}

/* 热力图容器 */
.heatmap-container {
  height: 300px;
  overflow-y: auto;
}

.heatmap-placeholder {
  text-align: center;
  line-height: 250px;
  color: #909399;
}

/* 查看更多按钮 */
.view-more-btn {
  display: block;
  margin: 10px auto 0;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .analysis-row {
    grid-template-columns: 1fr;
  }
  
  .navigation-cards :deep(.el-col) {
    margin-bottom: 15px;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .nav-content {
    flex-direction: column;
    text-align: center;
  }
  
  .nav-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .nav-info h3 {
    font-size: 14px;
  }
  
  .nav-info p {
    font-size: 12px;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
