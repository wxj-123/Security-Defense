<template>
  <div class="statistics-container">
    <!-- 时间范围选择 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadData"
          />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="eventType" placeholder="全部" clearable @change="loadData">
            <el-option label="全部" value="" />
            <el-option label="SQL注入" value="SQL注入" />
            <el-option label="XSS攻击" value="XSS攻击" />
            <el-option label="DDoS攻击" value="DDoS攻击" />
            <el-option label="暴力破解" value="暴力破解" />
            <el-option label="恶意扫描" value="恶意扫描" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="exportReport">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card total">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="50" color="#409eff"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总事件数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card high">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="50" color="#f56c6c"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.high }}</div>
              <div class="stat-label">高危事件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card handled">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="50" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.handled }}</div>
              <div class="stat-label">已处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="50" color="#e6a23c"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>事件趋势分析</span>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>事件类型分布</span>
          </template>
          <div ref="typeChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>事件等级分布</span>
          </template>
          <div ref="levelChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>TOP10 攻击源IP</span>
          </template>
          <div ref="ipChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>事件详细统计</span>
      </template>
      <el-table :data="detailData" border stripe>
        <el-table-column prop="type" label="事件类型" width="150" />
        <el-table-column prop="total" label="总数" width="100" />
        <el-table-column prop="high" label="高危" width="100">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.high }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="medium" label="中危" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.medium }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="low" label="低危" width="100">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.low }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handled" label="已处理" width="100" />
        <el-table-column prop="pending" label="待处理" width="100" />
        <el-table-column prop="rate" label="处理率" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.rate" :color="getProgressColor(row.rate)" />
          </template>
        </el-table-column>
        <el-table-column prop="avgTime" label="平均处理时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Download,
  DataAnalysis,
  WarningFilled,
  CircleCheck,
  Clock
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { getEventStatistics, exportEventStatistics } from '@/api/report'

const dateRange = ref<[Date, Date]>([new Date(new Date().setDate(new Date().getDate() - 7)), new Date()])
const eventType = ref('')

// 图表引用
const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const levelChartRef = ref<HTMLElement>()
const ipChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let levelChart: echarts.ECharts | null = null
let ipChart: echarts.ECharts | null = null

// 统计数据
const stats = reactive({
  total: 647,
  high: 89,
  handled: 589,
  pending: 58
})

// 详细数据
const detailData = ref([
  {
    type: 'SQL注入',
    total: 156,
    high: 34,
    medium: 78,
    low: 44,
    handled: 142,
    pending: 14,
    rate: 91,
    avgTime: '2.3小时'
  },
  {
    type: 'XSS攻击',
    total: 89,
    high: 12,
    medium: 45,
    low: 32,
    handled: 81,
    pending: 8,
    rate: 91,
    avgTime: '1.8小时'
  },
  {
    type: 'DDoS攻击',
    total: 45,
    high: 23,
    medium: 18,
    low: 4,
    handled: 40,
    pending: 5,
    rate: 89,
    avgTime: '0.5小时'
  },
  {
    type: '暴力破解',
    total: 234,
    high: 15,
    medium: 89,
    low: 130,
    handled: 220,
    pending: 14,
    rate: 94,
    avgTime: '3.2小时'
  },
  {
    type: '恶意扫描',
    total: 123,
    high: 5,
    medium: 56,
    low: 62,
    handled: 106,
    pending: 17,
    rate: 86,
    avgTime: '4.1小时'
  }
])

// 加载数据
const loadData = async () => {
  try {
    const res = await getEventStatistics({
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1],
      type: eventType.value
    })
    
    // 更新统计
    Object.assign(stats, res.data)
    
    // 更新图表
    updateCharts()
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
  }
  if (levelChartRef.value) {
    levelChart = echarts.init(levelChartRef.value)
  }
  if (ipChartRef.value) {
    ipChart = echarts.init(ipChartRef.value)
  }
  
  updateCharts()
  
  window.addEventListener('resize', () => {
    trendChart?.resize()
    typeChart?.resize()
    levelChart?.resize()
    ipChart?.resize()
  })
}

// 更新图表
const updateCharts = () => {
  // 趋势图
  const dates = []
  const counts = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString())
    counts.push(Math.floor(Math.random() * 100) + 50)
  }
  
  const trendOption: EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '事件数量'
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: counts,
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }
    ]
  }
  trendChart?.setOption(trendOption)
  
  // 类型分布饼图
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
        data: detailData.value.map(item => ({
          name: item.type,
          value: item.total
        })),
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
  
  // 等级分布柱状图
  const levelOption: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['高危', '中危', '低危']
    },
    xAxis: {
      type: 'category',
      data: detailData.value.map(item => item.type)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '高危',
        type: 'bar',
        stack: 'total',
        data: detailData.value.map(item => item.high),
        itemStyle: { color: '#f56c6c' }
      },
      {
        name: '中危',
        type: 'bar',
        stack: 'total',
        data: detailData.value.map(item => item.medium),
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: '低危',
        type: 'bar',
        stack: 'total',
        data: detailData.value.map(item => item.low),
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  levelChart?.setOption(levelOption)
  
  // TOP IP 横向柱状图
  const ipData = [
    { ip: '192.168.1.100', count: 156 },
    { ip: '10.0.0.23', count: 134 },
    { ip: '172.16.5.88', count: 98 },
    { ip: '203.0.113.42', count: 87 },
    { ip: '198.51.100.88', count: 76 },
    { ip: '192.168.2.45', count: 65 },
    { ip: '10.10.10.50', count: 54 },
    { ip: '172.20.0.100', count: 43 },
    { ip: '192.168.3.30', count: 32 },
    { ip: '10.0.1.200', count: 21 }
  ]
  
  const ipOption: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ipData.map(item => item.ip)
    },
    series: [
      {
        type: 'bar',
        data: ipData.map(item => item.count),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  ipChart?.setOption(ipOption)
}

// 导出报表
const exportReport = async () => {
  try {
    ElMessage.success('报表导出中，请稍候...')
    
    // 构造报表数据
    const reportData = {
      dateRange: dateRange.value,
      eventType: eventType.value,
      stats: stats,
      detailData: detailData.value
    }
    
    // 调用后端导出接口
    const response = await exportEventStatistics(reportData, 'excel')
    console.log('导出响应:', response)
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `事件统计报表_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    
    // 清理URL对象和DOM元素
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('报表导出成功')
  } catch (error: any) {
    console.error('导出失败:', error)
    ElMessage.error('报表导出失败: ' + (error.message || '未知错误'))
  }
}

// 获取进度条颜色
const getProgressColor = (rate: number) => {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  initCharts()
  loadData()
})

onUnmounted(() => {
  trendChart?.dispose()
  typeChart?.dispose()
  levelChart?.dispose()
  ipChart?.dispose()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-card.total {
  border-left-color: #409eff;
}

.stat-card.high {
  border-left-color: #f56c6c;
}

.stat-card.handled {
  border-left-color: #67c23a;
}

.stat-card.pending {
  border-left-color: #e6a23c;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

:deep(.el-progress__text) {
  font-size: 12px !important;
}
</style>
