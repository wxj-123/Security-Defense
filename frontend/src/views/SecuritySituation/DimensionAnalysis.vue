<template>
  <div class="dimension-analysis">
    <div class="page-header">
      <h2>安全维度分析</h2>
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

    <!-- 维度评分概览 -->
    <div class="score-overview">
      <el-card class="overview-card">
        <template #header>
          <div class="card-header">
            <span>整体安全评分</span>
            <el-tag :type="getOverallScoreType(overallScore)" size="large">
              {{ overallScore }}分
            </el-tag>
          </div>
        </template>
        <div class="score-display">
          <div class="score-circle">
            <el-progress
              type="circle"
              :percentage="Math.max(0, Math.min(100, Number(overallScore)))"
              :width="120"
              :stroke-width="8"
              :color="getProgressColor(overallScore)"
            >
              <template #default="{ percentage }">
                <span class="score-text">{{ percentage }}</span>
              </template>
            </el-progress>
          </div>
          <div class="score-details">
            <div class="score-level">
              <span class="label">安全等级：</span>
              <el-tag :type="getOverallScoreType(overallScore)" size="large">
                {{ getSecurityLevel(overallScore) }}
              </el-tag>
            </div>
            <div class="update-time">
              <span>最后更新：{{ lastUpdateTime }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="content-layout">
      <!-- 雷达图 -->
      <div class="radar-section">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>安全维度雷达图</span>
              <el-select v-model="selectedDimensions" multiple placeholder="选择维度" style="width: 200px">
                <el-option
                  v-for="dimension in availableDimensions"
                  :key="dimension.key"
                  :label="dimension.label"
                  :value="dimension.key"
                />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="radarChart" height="400"></canvas>
          </div>
        </el-card>
      </div>

      <!-- 维度详细评分 -->
      <div class="dimensions-section">
        <el-card class="dimensions-card">
          <template #header>
            <div class="card-header">
              <span>维度详细评分</span>
            </div>
          </template>
          <div class="dimensions-list">
            <div 
              v-for="dimension in dimensionScores" 
              :key="dimension.key"
              class="dimension-item"
            >
              <div class="dimension-header">
                <div class="dimension-info">
                  <el-icon size="20" :color="getDimensionColor(dimension.score)">
                    <component :is="dimension.icon" />
                  </el-icon>
                  <span class="dimension-name">{{ dimension.label }}</span>
                </div>
                <div class="dimension-score">
                  <span class="score-value">{{ dimension.score }}</span>
                  <el-progress
                    :percentage="Math.max(0, Math.min(100, dimension.score))"
                    :color="getProgressColor(dimension.score)"
                    :stroke-width="6"
                    :show-text="false"
                    style="width: 100px"
                  />
                </div>
              </div>
              <div class="dimension-details">
                <div class="detail-item">
                  <span class="detail-label">状态：</span>
                  <el-tag :type="getDimensionStatusType(dimension.score)" size="small">
                    {{ getDimensionStatus(dimension.score) }}
                  </el-tag>
                </div>
                <div class="detail-item">
                  <span class="detail-label">趋势：</span>
                  <span :class="['trend-indicator', dimension.trend]">
                    <el-icon>
                      <ArrowUp v-if="dimension.trend === 'up'" />
                      <ArrowDown v-if="dimension.trend === 'down'" />
                      <Minus v-if="dimension.trend === 'stable'" />
                    </el-icon>
                    {{ getTrendText(dimension.trend) }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">关键指标：</span>
                  <span class="metrics-text">{{ dimension.keyMetrics }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 历史趋势 -->
    <div class="trend-section">
      <el-card class="trend-card">
        <template #header>
          <div class="card-header">
            <span>安全维度历史趋势</span>
            <el-select v-model="trendDimension" @change="updateTrendChart" style="width: 150px">
              <el-option
                v-for="dimension in availableDimensions"
                :key="dimension.key"
                :label="dimension.label"
                :value="dimension.key"
              />
            </el-select>
          </div>
        </template>
        <div class="chart-container">
          <canvas ref="trendChart" height="300"></canvas>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, markRaw } from 'vue'
import { Refresh, ArrowUp, ArrowDown, Minus, Lock, Monitor, Warning, Tools } from '@element-plus/icons-vue'
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
  RadarController,
  RadialLinearScale,
  Filler,
  LineController
} from 'chart.js'
import { getSituationOverview, getSituationTrend } from '@/api/security-situation'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, RadarController, RadialLinearScale, Filler, LineController)

// 响应式数据
const timeRange = ref<[string, string]>([
  new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' '),
  new Date().toISOString().slice(0, 19).replace('T', ' ')
])
const overallScore = ref(75)
const lastUpdateTime = ref('')
const selectedDimensions = ref<string[]>([])
const trendDimension = ref('threatDetection')

// 图表引用
const radarChart = ref<HTMLCanvasElement>()
const trendChart = ref<HTMLCanvasElement>()
let radarChartInstance: Chart | null = null
let trendChartInstance: Chart | null = null

// 可用维度定义
const availableDimensions = [
  { key: 'threatDetection', label: '威胁检测', icon: markRaw(Warning) },
  { key: 'vulnerability', label: '漏洞管理', icon: markRaw(Warning) },
  { key: 'assetSecurity', label: '资产安全', icon: markRaw(Lock) },
  { key: 'networkTraffic', label: '网络流量', icon: markRaw(Monitor) },
  { key: 'securityConfig', label: '安全配置', icon: markRaw(Tools) }
]

// 维度评分数据
const dimensionScores = computed(() => {
  const defaultData = {
    threatDetection: { score: 85, trend: 'up', keyMetrics: '检测率、误报率、响应时间' },
    vulnerability: { score: 72, trend: 'stable', keyMetrics: '漏洞数量、修复率、高危漏洞' },
    assetSecurity: { score: 68, trend: 'down', keyMetrics: '资产覆盖率、风险评分、合规率' },
    networkTraffic: { score: 80, trend: 'up', keyMetrics: '异常流量、带宽使用、连接数' },
    securityConfig: { score: 75, trend: 'stable', keyMetrics: '配置合规率、策略覆盖率' }
  }

  return availableDimensions.map(dimension => ({
    ...dimension,
    ...defaultData[dimension.key as keyof typeof defaultData],
    score: Math.max(0, Math.min(100, defaultData[dimension.key as keyof typeof defaultData]?.score || 0))
  }))
})

// 生命周期钩子
onMounted(() => {
  fetchData()
  selectedDimensions.value = availableDimensions.map(d => d.key)
})

// 方法
const fetchData = async () => {
  try {
    // 获取整体评分数据
    const response = await getSituationOverview()
    const data = response.data as any
    
    // 检查是否包含SituationData类型的数据字段
    if ('situationIndex' in data) {
      // 处理后端SituationData对象
      overallScore.value = Math.max(0, Math.min(100, data.situationIndex))
    } 
    // 检查是否包含模拟数据字段
    else if ('situationScore' in data) {
      // 处理模拟数据
      overallScore.value = Math.max(0, Math.min(100, data.situationScore))
    } else if ('overallScore' in data) {
      // 处理其他格式数据
      overallScore.value = Math.max(0, Math.min(100, data.overallScore))
    } else {
      // 默认值
      overallScore.value = 75
    }
    
    lastUpdateTime.value = new Date().toLocaleString('zh-CN')
    updateRadarChart()
    updateTrendChart()
  } catch (error) {
    ElMessage.error('获取安全维度数据失败')
    console.error('获取数据失败:', error)
  }
}

const updateRadarChart = async () => {
  await nextTick()
  
  if (radarChartInstance) {
    radarChartInstance.destroy()
  }

  const ctx = radarChart.value?.getContext('2d')
  if (!ctx) return

  const selectedData = dimensionScores.value.filter(d => 
    selectedDimensions.value.length === 0 || selectedDimensions.value.includes(d.key)
  )

  radarChartInstance = new Chart(ctx, {
    type: 'radar',
    data: {
      labels: selectedData.map(d => d.label),
      datasets: [{
        label: '安全评分',
        data: selectedData.map(d => d.score),
        backgroundColor: 'rgba(64, 158, 255, 0.2)',
        borderColor: '#409eff',
        pointBackgroundColor: '#409eff',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: '#409eff',
        pointRadius: 6,
        pointHoverRadius: 8
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
          suggestedMax: 100,
          ticks: {
            stepSize: 20
          }
        }
      },
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          callbacks: {
            label: (context: any) => {
              const value = context.parsed?.r
              if (value == null || value === undefined) return ''
              return `${context.label}: ${value}分`
            }
          }
        }
      },
      // 添加安全的事件处理配置
      events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
    }
  })
}

const updateTrendChart = async () => {
  await nextTick()
  
  if (trendChartInstance) {
    trendChartInstance.destroy()
  }

  const ctx = trendChart.value?.getContext('2d')
  if (!ctx) return

  // 生成模拟趋势数据
  const labels = []
  const data = []
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(Date.now() - i * 24 * 60 * 60 * 1000)
    labels.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))
    data.push(Math.max(0, Math.min(100, Math.floor(Math.random() * 30) + 60))) // 60-90之间，确保在0-100范围内
  }

  trendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: availableDimensions.find(d => d.key === trendDimension.value)?.label || '维度评分',
        data,
        borderColor: '#409eff',
        backgroundColor: 'rgba(64, 158, 255, 0.1)',
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: false,
          min: 0,
          max: 100,
          title: {
            display: true,
            text: '评分'
          }
        }
      },
      plugins: {
        legend: {
          display: false
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

const handleTimeRangeChange = () => {
  updateTrendChart()
}

// 辅助函数
const getOverallScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'success'
  if (score >= 70) return 'warning'
  if (score >= 60) return 'warning'
  return 'danger'
}

const getSecurityLevel = (score: number) => {
  if (score >= 90) return '非常安全'
  if (score >= 80) return '安全'
  if (score >= 70) return '较安全'
  if (score >= 60) return '有风险'
  return '高风险'
}

const getProgressColor = (value: number) => {
  if (value >= 80) return '#28a745'
  if (value >= 60) return '#ffc107'
  return '#dc3545'
}

const getDimensionColor = (score: number) => {
  if (score >= 80) return '#28a745';
  if (score >= 60) return '#ffc107';
  return '#dc3545'
}

const getDimensionStatusType = (score: number) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

const getDimensionStatus = (score: number) => {
  if (score >= 80) return '良好'
  if (score >= 60) return '需关注'
  return '高风险'
}

const getTrendText = (trend: string) => {
  const trendMap: {[key: string]: string} = {
    'up': '上升',
    'down': '下降',
    'stable': '稳定'
  }
  return trendMap[trend] || '未知'
}
</script>

<style scoped>
.dimension-analysis {
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

.score-overview {
  margin-bottom: 20px;
}

.overview-card {
  background: #2C3E50;
  color: white;
}

.overview-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.overview-card :deep(.el-card__body) {
  padding: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 40px;
}

.score-circle {
  flex-shrink: 0;
}

.score-text {
  font-size: 20px;
  font-weight: bold;
  color: white;
}

.score-details {
  flex: 1;
}

.score-level {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.score-level .label {
  font-size: 16px;
  font-weight: 500;
  color: #909399;
}

.update-time {
  font-size: 14px;
  opacity: 0.8;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.radar-section, .dimensions-section {
  min-height: 500px;
}

.chart-container {
  position: relative;
  height: 400px;
}

.dimensions-list {
  max-height: 400px;
  overflow-y: auto;
}

.dimension-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  transition: background-color 0.3s;
}

.dimension-item:hover {
  background-color: #f8f9fa;
}

.dimension-item:last-child {
  border-bottom: none;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dimension-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dimension-name {
  font-weight: 500;
  color: #303133;
  font-size: 16px;
}

.dimension-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-value {
  font-weight: bold;
  color: #303133;
  min-width: 30px;
}

.dimension-details {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding-left: 30px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.detail-label {
  color: #909399;
}

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 14px;
}

.trend-indicator.up {
  color: #67c23a;
}

.trend-indicator.down {
  color: #f56c6c;
}

.trend-indicator.stable {
  color: #909399;
}

.metrics-text {
  color: #606266;
  font-size: 13px;
}

.trend-section {
  min-height: 350px;
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
  
  .score-display {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .dimension-details {
    flex-direction: column;
    gap: 5px;
  }
}
</style>