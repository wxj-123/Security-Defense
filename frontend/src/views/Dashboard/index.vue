<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalEvents }}</div>
              <div class="stat-label">总事件数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.criticalEvents }}</div>
              <div class="stat-label">严重事件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.blockedAttacks }}</div>
              <div class="stat-label">已拦截攻击</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Odometer /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.systemHealth }}%</div>
              <div class="stat-label">系统健康度</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>攻击流量趋势</span>
              <el-radio-group v-model="attackPeriod" size="small" @change="loadAttackTrend">
                <el-radio-button :value="'1h'">1小时</el-radio-button>
                <el-radio-button :value="'6h'">6小时</el-radio-button>
                <el-radio-button :value="'24h'">24小时</el-radio-button>
                <el-radio-button :value="'7d'">7天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="attackChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>威胁类型分布</span>
          </template>
          <div ref="threatChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>最近安全事件</span>
          </template>
          <el-table :data="recentEvents" stripe>
            <el-table-column prop="type" label="事件类型" width="150" />
            <el-table-column prop="level" label="威胁等级" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="getLevelType(row.level)"
                  effect="dark"
                >
                  {{ getLevelText(row.level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源IP" width="150" />
            <el-table-column prop="target" label="目标IP" width="150" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="timestamp" label="时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { DocumentCopy, Warning, Check, Odometer } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { DashboardStats, SecurityEvent } from '@/types'
import { getDashboardStats, getRecentEvents, getAttackTrend, getThreatDistribution } from '@/api/dashboard'

const stats = ref<DashboardStats>({
  totalEvents: 0,
  criticalEvents: 0,
  blockedAttacks: 0,
  systemHealth: 0,
  networkTraffic: {
    inbound: 0,
    outbound: 0
  }
})

const recentEvents = ref<SecurityEvent[]>([])
const attackPeriod = ref('24h')
const attackChartRef = ref<HTMLElement>()
const threatChartRef = ref<HTMLElement>()

let attackChart: echarts.ECharts | null = null
let threatChart: echarts.ECharts | null = null
let refreshTimer: number | null = null

// 获取统计数据
const loadDashboardStats = async () => {
  try {
    const res = await getDashboardStats()
    if (import.meta.env.DEV) console.log('仪表盘统计数据:', res)
    stats.value = res.data || res
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最近事件
const loadRecentEvents = async () => {
  try {
    const res = await getRecentEvents(10)
    if (import.meta.env.DEV) console.log('最近事件数据:', res)
    recentEvents.value = res.data || res
  } catch (error) {
    console.error('获取最近事件失败:', error)
  }
}

// 加载攻击趋势图表
const loadAttackTrend = async () => {
  try {
    const res = await getAttackTrend(attackPeriod.value as any)
    if (import.meta.env.DEV) console.log('攻击趋势数据:', res)
    const data = res.data || res

    if (attackChart && data) {
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.labels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '攻击次数',
          type: 'line',
          data: data.values,
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ])
          }
        }]
      }
      attackChart.setOption(option)
    }
  } catch (error) {
    console.error('获取攻击趋势失败:', error)
  }
}

// 加载威胁分布图表
const loadThreatDistribution = async () => {
  try {
    const res = await getThreatDistribution()
    if (import.meta.env.DEV) console.log('威胁分布数据:', res)
    const data = res.data || res

    if (threatChart && data) {
      const option = {
        tooltip: {
          trigger: 'item'
        },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          data: data.map((item: any) => ({
            value: item.count,
            name: item.type
          }))
        }]
      }
      threatChart.setOption(option)
    }
  } catch (error) {
    console.error('获取威胁分布失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (attackChartRef.value) {
    attackChart = echarts.init(attackChartRef.value)
    loadAttackTrend()
  }
  
  if (threatChartRef.value) {
    threatChart = echarts.init(threatChartRef.value)
    loadThreatDistribution()
  }
  
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  attackChart?.resize()
  threatChart?.resize()
}

// 获取威胁等级标签类型
const getLevelType = (level: string) => {
  const types: Record<string, any> = {
    critical: 'danger',
    high: 'warning',
    medium: 'info',
    low: 'success'
  }
  return types[level] || 'info'
}

const getLevelText = (level: string) => {
  const texts: Record<string, string> = {
    critical: '严重',
    high: '高',
    medium: '中',
    low: '低'
  }
  return texts[level] || level
}

// 获取状态标签类型
const getStatusType = (status: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    ignored: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    ignored: '已忽略'
  }
  return texts[status] || status
}

// 定时刷新数据
const startAutoRefresh = () => {
  refreshTimer = window.setInterval(() => {
    loadDashboardStats()
    loadRecentEvents()
    loadAttackTrend()
  }, 30000) // 30秒刷新一次
}

onMounted(() => {
  loadDashboardStats()
  loadRecentEvents()
  initCharts()
  startAutoRefresh()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  attackChart?.dispose()
  threatChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
