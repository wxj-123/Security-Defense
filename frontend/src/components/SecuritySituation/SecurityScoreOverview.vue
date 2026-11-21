<template>
  <div class="security-score-overview">
    <el-card class="card-wrapper">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <div class="header-actions">
            <el-select v-model="timeRange" size="small" @change="handleTimeRangeChange">
              <el-option label="实时" value="realtime"></el-option>
              <el-option label="今日" value="today"></el-option>
              <el-option label="本周" value="week"></el-option>
              <el-option label="本月" value="month"></el-option>
            </el-select>
          </div>
        </div>
      </template>
      
      <div class="main-content">
        <div class="score-section">
          <div class="score-display"
            :class="getScoreLevelClass(currentScore.score)"
          >
            <div class="score-value">{{ currentScore.score }}</div>
            <div class="score-label">安全态势评分</div>
          </div>
          <div class="score-details">
            <div class="score-metrics">
              <div class="metric-item">
                <div class="metric-label">趋势变化</div>
                <div class="metric-value" :class="getTrendClass(currentScore.trend)">
                  <el-icon v-if="currentScore.trend > 0" :size="16" color="#F56C6C">
                    <ArrowUpBold />
                  </el-icon>
                  <el-icon v-else-if="currentScore.trend < 0" :size="16" color="#67C23A">
                    <ArrowDownBold />
                  </el-icon>
                  <span v-else>
                    <Remove />
                  </span>
                  <span>{{ Math.abs(currentScore.trend) }}%</span>
                </div>
              </div>
              <div class="metric-item">
                <div class="metric-label">安全等级</div>
                <div class="metric-value" :class="getScoreLevelClass(currentScore.score)">
                  {{ getScoreLevelText(currentScore.score) }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="key-indicators">
          <div class="indicator-grid">
            <div 
              v-for="indicator in keyIndicators" 
              :key="indicator.id"
              class="indicator-item"
              @click="handleIndicatorClick(indicator)"
            >
              <div class="indicator-icon" :style="{ backgroundColor: indicator.color + '20' }">
                <el-icon :size="24" :color="indicator.color"><component :is="indicator.icon" /></el-icon>
              </div>
              <div class="indicator-info">
                <div class="indicator-name">{{ indicator.name }}</div>
                <div class="indicator-value">{{ formatNumber(indicator.value) }}</div>
                <div class="indicator-trend" :class="getTrendClass(indicator.trend)">
                  <el-icon v-if="indicator.trend > 0" :size="14" color="#F56C6C">
                    <ArrowUpBold />
                  </el-icon>
                  <el-icon v-else-if="indicator.trend < 0" :size="14" color="#67C23A">
                    <ArrowDownBold />
                  </el-icon>
                  <el-icon v-else :size="14" color="#909399">
                    <Remove />
                  </el-icon>
                  <span>{{ Math.abs(indicator.trend) }}%</span>
                </div>
              </div>
              <div class="indicator-status" :class="indicator.status">
                <el-tag :type="getStatusTagType(indicator.status)">
                  {{ getStatusText(indicator.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        
        <div class="score-trend">
          <canvas ref="trendChartCanvas" :width="400" :height="200"></canvas>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import { Chart, ChartConfiguration, CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, Filler, LineController } from 'chart.js'
import { ArrowUpBold, ArrowDownBold, Remove, Warning, Key, Bell, Monitor, Lock } from '@element-plus/icons-vue'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, Filler, LineController)

// Props
interface Props {
  title?: string
}

const { title = '安全态势评分' } = defineProps<Props>()

// Emits
interface Emits {
  (e: 'indicator-click', indicator: KeyIndicator): void
}

const emit = defineEmits<Emits>()

// 类型定义
interface KeyIndicator {
  id: string
  name: string
  value: number
  trend: number
  status: 'normal' | 'warning' | 'danger'
  color: string
  icon: any
  unit?: string
}

interface ScoreData {
  score: number
  trend: number
  history: number[]
  timestamp: string
}

// 响应式数据
const timeRange = ref('realtime')
const trendChartCanvas = ref<HTMLCanvasElement | null>(null)
const trendChart = ref<Chart | null>(null)

// 当前评分数据
const currentScore = ref<ScoreData>({
  score: 78.5,
  trend: 2.3,
  history: [],
  timestamp: new Date().toLocaleString('zh-CN')
})

// 关键指标数据
const keyIndicators = ref<KeyIndicator[]>([
  {
    id: 'vulnerability',
    name: '漏洞数量',
    value: 45,
    trend: -12.5,
    status: 'warning',
    color: '#E6A23C',
    icon: markRaw(Warning),
    unit: '个'
  },
  {
    id: 'attack',
    name: '攻击次数',
    value: 238,
    trend: 8.3,
    status: 'danger',
    color: '#F56C6C',
    icon: markRaw(Key)
  },
  {
    id: 'asset',
    name: '资产数量',
    value: 324,
    trend: 3.2,
    status: 'normal',
    color: '#67C23A',
    icon: markRaw(Monitor) // 使用Monitor替代Server，因为Server可能不存在
  },
  {
    id: 'security-event',
    name: '安全事件',
    value: 15,
    trend: -18.7,
    status: 'normal',
    color: '#409EFF',
    icon: markRaw(Bell)
  },
  {
    id: 'risk-asset',
    name: '风险资产',
    value: 68,
    trend: 5.1,
    status: 'warning',
    color: '#E6A23C',
    icon: markRaw(Monitor)
  },
  {
    id: 'compliance',
    name: '合规率',
    value: 89,
    trend: 2.4,
    status: 'normal',
    color: '#67C23A',
    icon: markRaw(Lock),
    unit: '%'
  }
])

// 图表历史数据（生成24小时的数据点）
const scoreHistory = ref<number[]>([])

// 计算属性
const scoreLevelConfig = {
  excellent: { min: 90, max: 100, text: '优秀', class: 'score-excellent', color: '#67C23A' },
  good: { min: 80, max: 89, text: '良好', class: 'score-good', color: '#409EFF' },
  normal: { min: 70, max: 79, text: '一般', class: 'score-normal', color: '#E6A23C' },
  poor: { min: 60, max: 69, text: '较差', class: 'score-poor', color: '#F56C6C' },
  critical: { min: 0, max: 59, text: '严重', class: 'score-critical', color: '#C06C84' }
}

// 方法
const getScoreLevelClass = (score: number) => {
  for (const config of Object.values(scoreLevelConfig)) {
    if (score >= config.min && score <= config.max) {
      return config.class
    }
  }
  return ''
}

const getScoreLevelText = (score: number) => {
  for (const config of Object.values(scoreLevelConfig)) {
    if (score >= config.min && score <= config.max) {
      return config.text
    }
  }
  return ''
}

const getTrendClass = (trend: number) => {
  if (trend > 0) return 'trend-increase'
  if (trend < 0) return 'trend-decrease'
  return 'trend-stable'
}

const getStatusTagType = (status: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const typeMap: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    normal: 'success',
    warning: 'warning',
    danger: 'danger'
  }
  return typeMap[status as keyof typeof typeMap] || 'info'
}

const getStatusText = (status: string) => {
  const textMap = {
    normal: '正常',
    warning: '警告',
    danger: '危险'
  }
  return textMap[status as keyof typeof textMap] || status
}

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const handleTimeRangeChange = () => {
  loadSecurityOverviewData()
}

const handleIndicatorClick = (indicator: KeyIndicator) => {
  emit('indicator-click', indicator)
}

const loadSecurityOverviewData = async () => {
  try {
    // 实际项目中，这里会调用API获取数据
    // const response = await getSecurityOverview(timeRange.value)
    // currentScore.value = response.data.score || currentScore.value
    // keyIndicators.value = response.data.indicators || keyIndicators.value
    // 目前使用模拟数据
    
    // 创建基本类型数据副本，避免响应式引用
    const timeRangeValue = String(timeRange.value)
    const currentScoreValue = {
      score: Number(currentScore.value.score),
      trend: Number(currentScore.value.trend),
      timestamp: String(currentScore.value.timestamp),
      history: [...currentScore.value.history].map(val => Number(val))
    }
    const keyIndicatorsValue = [...keyIndicators.value].map(indicator => ({
      ...indicator,
      value: Number(indicator.value),
      trend: Number(indicator.trend)
    }))
    
    // 更新模拟数据
    updateMockData(timeRangeValue, currentScoreValue, keyIndicatorsValue)
    
    // 使用 nextTick 确保DOM更新完成后再更新图表
    await nextTick()
    
    // 避免循环调用
    if (trendChart.value && !isUpdatingChart) {
      isUpdatingChart = true
      setTimeout(() => {
        updateTrendChart()
        isUpdatingChart = false
      }, 0)
    }
  } catch (error) {
    console.error('获取安全态势概览数据失败:', error)
    isUpdatingChart = false
  }
}

const updateMockData = (timeRangeValue?: string, currentScoreValue?: ScoreData, keyIndicatorsValue?: KeyIndicator[]) => {
  try {
    // 根据时间范围更新模拟数据
    const baseScore = 78.5
    const variation = (Math.random() - 0.5) * 10
    
    // 创建currentScore.value的副本以避免循环引用
    const currentScoreCopy = currentScoreValue || { ...currentScore.value }
    
    // 使用普通对象而不是响应式对象更新数据
    const newScoreData = {
      score: Math.round((baseScore + variation) * 10) / 10,
      trend: Math.round((Math.random() - 0.5) * 20 * 10) / 10,
      timestamp: new Date().toLocaleString('zh-CN'),
      history: currentScoreCopy.history || []
    }
    
    // 更新currentScore.value
    currentScore.value = newScoreData
    
    // 创建keyIndicators.value的副本以避免循环引用
    const keyIndicatorsCopy = keyIndicatorsValue || [...keyIndicators.value]
    
    // 更新关键指标，使用普通对象而不是响应式对象
    const updatedIndicators = keyIndicatorsCopy.map(indicator => ({
      ...indicator,
      value: Math.round(indicator.value * (0.9 + Math.random() * 0.2)),
      trend: Math.round((Math.random() - 0.5) * 20 * 10) / 10,
      status: getRandomStatus()
    }))
    
    // 更新keyIndicators.value
    keyIndicators.value = updatedIndicators
    
    // 更新历史数据
    updateHistoryData(timeRangeValue, newScoreData)
  } catch (error) {
    console.error('更新模拟数据时出错:', error)
  }
}

const getRandomStatus = () => {
  const statuses = ['normal', 'warning', 'danger']
  const weights = [0.6, 0.3, 0.1]
  const rand = Math.random()
  let cumulative = 0
  
  for (let i = 0; i < weights.length; i++) {
    cumulative += weights[i]
    if (rand < cumulative) {
      return statuses[i] as 'normal' | 'warning' | 'danger'
    }
  }
  return 'normal'
}

const updateHistoryData = (timeRangeValue?: string, currentScoreValue?: ScoreData) => {
  try {
    const history = []
    // 使用传入的参数或当前值，避免直接引用响应式数据
    const timeRangeVal = timeRangeValue || timeRange.value
    const currentScoreVal = currentScoreValue || { ...currentScore.value }
    
    const dataPoints = timeRangeVal === 'realtime' ? 24 : timeRangeVal === 'today' ? 24 : timeRangeVal === 'week' ? 7 : 30
    let baseScore = (currentScoreVal.score || 78.5) - 5 + Math.random() * 10
    
    for (let i = dataPoints - 1; i >= 0; i--) {
      const variation = Math.sin(i * 0.3) * 5 + (Math.random() - 0.5) * 3
      history.push(Math.max(0, Math.min(100, baseScore + variation)))
    }
    
    // 创建scoreHistory.value的副本以避免循环引用，然后更新
    scoreHistory.value = [...history]
  } catch (error) {
    console.error('更新历史数据时出错:', error)
  }
}

const initTrendChart = () => {
  if (!trendChartCanvas.value) return
  
  const ctx = trendChartCanvas.value.getContext('2d')
  if (!ctx) return
  
  try {
    // 销毁已存在的图表实例
    if (trendChart.value) {
      trendChart.value.destroy()
      trendChart.value = null
    }
    
    // 创建基本类型数据副本，避免任何响应式引用
    const rawData = [...scoreHistory.value].map(val => Number(val))
    const rawLabels = [...generateTimeLabels()].map(label => String(label))
    
    // 确保数据长度一致
    const dataLength = Math.min(rawLabels.length, rawData.length)
    const chartLabels = rawLabels.slice(0, dataLength)
    const chartData = rawData.slice(0, dataLength)
    
    // 使用最简配置初始化图表，避免复杂选项
    trendChart.value = new Chart(ctx, {
      type: 'line',
      data: {
        labels: chartLabels,
        datasets: [{
          label: '安全态势评分',
          data: chartData,
          borderColor: '#409EFF',
          backgroundColor: 'rgba(64, 158, 255, 0.1)',
          tension: 0.4,
          pointRadius: 3,
          pointHoverRadius: 5,
          pointBackgroundColor: '#409EFF',
          borderWidth: 2,
          fill: true
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            grid: {
              display: false
            }
          },
          y: {
            min: 0,
            max: 100
          }
        },
        plugins: {
          legend: {
            display: false
          }
        },
        interaction: {
          mode: 'index',
          intersect: false
        },
        events: ['mousemove', 'mouseout']
      }
    })
  } catch (error) {
    console.error('初始化趋势图表时出错:', error)
    console.error('错误详情:', {
      canvasExists: !!trendChartCanvas.value,
      contextExists: !!ctx,
      historyLength: scoreHistory.value?.length
    })
  }
}

const generateTimeLabels = (): string[] => {
  try {
    const labels: string[] = []
    // 创建scoreHistory.value的副本以避免循环引用
    const scoreHistoryCopy = [...scoreHistory.value]
    const dataPoints = scoreHistoryCopy.length
    
    // 添加安全检查
    if (dataPoints <= 0) {
      return []
    }
    
    const now = new Date()
    
    if (dataPoints === 24) {
      // 24小时数据
      for (let i = dataPoints - 1; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 60 * 60 * 1000)
        labels.push(time.getHours() + ':00')
      }
    } else if (dataPoints === 7) {
      // 7天数据
      for (let i = dataPoints - 1; i >= 0; i--) {
        const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
        labels.push(`周${'日一二三四五六'.charAt(date.getDay())}`)
      }
    } else {
      // 30天数据或其他情况，确保不会除以0
      const step = Math.max(1, Math.floor(dataPoints / 6))
      // 确保step不会导致无限循环
      if (step > 0) {
        for (let i = dataPoints - 1; i >= 0; i -= step) {
          const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
          labels.push(`${date.getMonth() + 1}/${date.getDate()}`)
        }
      } else {
        // fallback方案
        for (let i = dataPoints - 1; i >= 0; i--) {
          const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
          labels.push(`${date.getMonth() + 1}/${date.getDate()}`)
        }
      }
    }
    
    // 返回基本类型数组以避免循环引用
    return labels.map(label => String(label))
  } catch (error) {
    console.error('生成时间标签时出错:', error)
    return []
  }
}

// 添加标志避免循环调用
let isUpdatingChart = false

const updateTrendChart = () => {
  // 避免循环调用
  if (isUpdatingChart) {
    return
  }
  
  if (trendChart.value) {
    try {
      isUpdatingChart = true
      
      // 创建完全独立的基本类型数据副本，避免任何可能的循环引用
      const scoreHistoryCopy = scoreHistory.value.map(val => {
        if (typeof val === 'number' && !isNaN(val)) {
          return Number(val)
        }
        return 0
      })
      
      const labels = generateTimeLabels().map(label => {
        if (typeof label === 'string') {
          return String(label)
        }
        return ''
      })
      
      // 确保使用基本类型数据更新图表
      if (trendChart.value.data) {
        // 直接替换整个labels数组，而不是修改现有数组
        trendChart.value.data.labels = labels
        
        if (trendChart.value.data.datasets && trendChart.value.data.datasets[0]) {
          // 直接替换整个data数组，而不是修改现有数组
          trendChart.value.data.datasets[0].data = scoreHistoryCopy
        }
      }
      
      // 使用简单模式更新，避免任何动画或复杂计算
      trendChart.value.update('none')
    } catch (error) {
      console.error('更新趋势图表时出错:', error)
      // 添加更详细的错误信息
      console.error('错误详情:', {
        chartExists: !!trendChart.value,
        dataExists: !!trendChart.value?.data,
        datasetsExist: !!(trendChart.value?.data?.datasets),
        historyLength: scoreHistory.value?.length,
        historyType: typeof scoreHistory.value,
        historySample: scoreHistory.value?.slice(0, 3).map(val => ({type: typeof val, value: val}))
      })
    } finally {
      // 确保重置标志
      setTimeout(() => {
        isUpdatingChart = false
      }, 10)
    }
  }
}

// 添加防抖标志避免频繁调用
let resizeTimeout: number | null = null
let isHandlingResize = false

const handleResize = () => {
  if (resizeTimeout) {
    clearTimeout(resizeTimeout)
  }
  
  resizeTimeout = window.setTimeout(() => {
    // 避免循环调用
    if (isHandlingResize) {
      resizeTimeout = null
      return
    }
    
    isHandlingResize = true
    
    // 确保在更新前图表实例仍然存在
    if (trendChart.value) {
      try {
        trendChart.value.resize()
      } catch (error) {
        console.error('调整图表大小时出错:', error)
      }
    }
    
    isHandlingResize = false
    resizeTimeout = null
  }, 100) // 100ms防抖
}

// 生命周期钩子
onMounted(() => {
  try {
    // 初始化历史数据
    const history = []
    // 使用基本类型避免响应式引用
    const baseScore = 78.5 - 5 + Math.random() * 10
    
    for (let i = 23; i >= 0; i--) {
      // 生成波动的历史数据
      const variation = Math.sin(i * 0.3) * 5 + (Math.random() - 0.5) * 3
      history.push(Math.max(0, Math.min(100, baseScore + variation)))
    }
    
    // 直接设置基本类型数组
    scoreHistory.value = history.map(val => Number(val))
    
    // 先添加事件监听器
    window.addEventListener('resize', handleResize)
    
    // 延迟初始化图表，确保DOM完全准备好
    setTimeout(() => {
      initTrendChart()
      loadSecurityOverviewData()
    }, 150)
  } catch (error) {
    console.error('组件挂载时出错:', error)
    // 即使出错也要确保添加事件监听器
    window.addEventListener('resize', handleResize)
  }
})

onUnmounted(() => {
  try {
    // 清理resize timeout
    if (resizeTimeout) {
      clearTimeout(resizeTimeout)
      resizeTimeout = null
    }
    
    // 重置所有标志
    isUpdatingChart = false
    isHandlingResize = false
    
    // 移除事件监听器
    window.removeEventListener('resize', handleResize)
    
    // 销毁图表实例
    if (trendChart.value) {
      trendChart.value.destroy()
      trendChart.value = null
    }
  } catch (error) {
    console.error('组件卸载时清理资源出错:', error)
  }
})
</script>

<style scoped>
.security-score-overview {
  width: 100%;
  height: 100%;
}

.card-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.score-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.score-display {
  text-align: center;
  padding: 20px;
  border-radius: 12px;
  min-width: 200px;
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.score-label {
  font-size: 16px;
  color: var(--el-text-color-secondary);
}

.score-details {
  flex: 1;
  padding-left: 24px;
}

.score-metrics {
  display: flex;
  gap: 32px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.metric-value {
  font-size: 18px;
  font-weight: 600;
}

.key-indicators {
  margin-bottom: 24px;
}

.indicator-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.indicator-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--el-bg-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator-item:hover {
  background-color: var(--el-bg-color-overlay);
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.indicator-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-right: 16px;
}

.indicator-info {
  flex: 1;
}

.indicator-name {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.indicator-value {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 2px;
}

.indicator-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.indicator-status {
  margin-left: 8px;
}

.score-trend {
  flex: 1;
  min-height: 200px;
}

.trend-increase {
  color: #F56C6C;
}

.trend-decrease {
  color: #67C23A;
}

.trend-stable {
  color: #909399;
}

.score-excellent .score-value {
  color: #67C23A;
}

.score-good .score-value {
  color: #409EFF;
}

.score-normal .score-value {
  color: #E6A23C;
}

.score-poor .score-value {
  color: #F56C6C;
}

.score-critical .score-value {
  color: #C06C84;
}

.score-excellent {
  background-color: rgba(103, 194, 58, 0.1);
}

.score-good {
  background-color: rgba(64, 158, 255, 0.1);
}

.score-normal {
  background-color: rgba(230, 162, 60, 0.1);
}

.score-poor {
  background-color: rgba(245, 108, 108, 0.1);
}

.score-critical {
  background-color: rgba(192, 108, 132, 0.1);
}
</style>