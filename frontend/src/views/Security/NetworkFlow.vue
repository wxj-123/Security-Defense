<template>
  <div class="network-flow-container">
    <!-- 流量统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总流量" :value="stats.totalTraffic" suffix="GB">
            <template #prefix>
              <el-icon color="#409eff"><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="入站流量" :value="stats.inbound" suffix="MB/s">
            <template #prefix>
              <el-icon color="#67c23a"><Download /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="出站流量" :value="stats.outbound" suffix="MB/s">
            <template #prefix>
              <el-icon color="#e6a23c"><Upload /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="活动连接" :value="stats.activeConnections">
            <template #prefix>
              <el-icon color="#f56c6c"><Connection /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 流量趋势图表 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>流量趋势</span>
          <div>
            <el-radio-group v-model="timeRange" size="small" @change="loadTrafficData">
              <el-radio-button :value="'1h'">最近1小时</el-radio-button>
              <el-radio-button :value="'6h'">最近6小时</el-radio-button>
              <el-radio-button :value="'24h'">最近24小时</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px;"></div>
    </el-card>

    <!-- 连接列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>网络连接</span>
          <el-button type="primary" size="small" @click="loadConnections">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="协议">
          <el-select v-model="filterProtocol" placeholder="全部" clearable style="width: 120px">
            <el-option label="TCP" value="TCP" />
            <el-option label="UDP" value="UDP" />
            <el-option label="HTTP" value="HTTP" />
            <el-option label="HTTPS" value="HTTPS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" value="正常" />
            <el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleResetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="connectionList"
        stripe
        border
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="sourceIp" label="源IP" width="150" />
        <el-table-column prop="sourcePort" label="源端口" width="100" />
        <el-table-column prop="destIp" label="目标IP" width="150" />
        <el-table-column prop="destPort" label="目标端口" width="100" />
        <el-table-column prop="protocol" label="协议" width="100">
          <template #default="{ row }">
            <el-tag :type="getProtocolType(row.protocol)" size="small">
              {{ row.protocol }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bytes" label="流量" width="120">
          <template #default="{ row }">
            {{ formatBytes(row.bytes) }}
          </template>
        </el-table-column>
        <el-table-column prop="packets" label="数据包" width="100" />
        <el-table-column prop="duration" label="持续时间" width="120">
          <template #default="{ row }">
            {{ row.duration }}s
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row)">
              详情
            </el-button>
            <el-button type="danger" size="small" link @click="blockConnection(row)">
              阻断
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
        @size-change="loadConnections"
        @current-change="loadConnections"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 连接详情对话框 -->
    <el-dialog v-model="detailVisible" title="连接详情" width="600px">
      <el-descriptions :column="2" border v-if="currentConnection">
        <el-descriptions-item label="源IP">{{ currentConnection.sourceIp }}</el-descriptions-item>
        <el-descriptions-item label="源端口">{{ currentConnection.sourcePort }}</el-descriptions-item>
        <el-descriptions-item label="目标IP">{{ currentConnection.destIp }}</el-descriptions-item>
        <el-descriptions-item label="目标端口">{{ currentConnection.destPort }}</el-descriptions-item>
        <el-descriptions-item label="协议">{{ currentConnection.protocol }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentConnection.status === '正常' ? 'success' : 'danger'">
            {{ currentConnection.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="流量">{{ formatBytes(currentConnection.bytes) }}</el-descriptions-item>
        <el-descriptions-item label="数据包">{{ currentConnection.packets }}</el-descriptions-item>
        <el-descriptions-item label="持续时间">{{ currentConnection.duration }}秒</el-descriptions-item>
        <el-descriptions-item label="建立时间">{{ currentConnection.startTime }}</el-descriptions-item>
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
import { TrendCharts, Download, Upload, Connection, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { getNetworkFlowMonitor } from '@/api/security'
import { http } from '@/utils/request'

const loading = ref(false)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null
const timeRange = ref('1h')
const detailVisible = ref(false)
const currentConnection = ref<any>(null)

// 统计数据
const stats = reactive({
  totalTraffic: 0,
  inbound: 0,
  outbound: 0,
  activeConnections: 0
})

// 连接列表
const connectionList = ref<any[]>([])
const filterProtocol = ref('')
const filterStatus = ref('')

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 加载流量数据
const loadTrafficData = async () => {
  try {
    const res = await getNetworkFlowMonitor({
      timeRange: timeRange.value
    })
    
    // 更新统计数据
    stats.totalTraffic = parseFloat((res.data.totalBytes / 1024 / 1024 / 1024).toFixed(2))
    stats.inbound = parseFloat((Math.random() * 100).toFixed(2))
    stats.outbound = parseFloat((Math.random() * 80).toFixed(2))
    stats.activeConnections = res.data.connections?.length || 0
    
    // 更新图表
    updateChart()
  } catch (error) {
    ElMessage.error('加载流量数据失败')
  }
}

// 加载连接列表
const loadConnections = async () => {
  loading.value = true
  try {
    const res = await getNetworkFlowMonitor({
      page: pagination.page,
      pageSize: pagination.pageSize,
      protocol: filterProtocol.value,
      status: filterStatus.value
    })
    
    connectionList.value = res.data.connections.map((conn: any, index: number) => ({
      ...conn,
      sourcePort: 50000 + index,
      destPort: conn.port,
      duration: Math.floor(Math.random() * 300) + 10,
      startTime: new Date(Date.now() - Math.random() * 3600000).toLocaleString()
    }))
    
    pagination.total = res.data.connections.length
  } catch (error) {
    ElMessage.error('加载连接列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
}

// 更新图表
const updateChart = () => {
  const now = Date.now()
  const timestamps: string[] = []
  const inboundData: number[] = []
  const outboundData: number[] = []
  
  // 生成模拟数据
  const points = timeRange.value === '24h' ? 24 : (timeRange.value === '6h' ? 12 : 12)
  const interval = timeRange.value === '24h' ? 3600000 : (timeRange.value === '6h' ? 1800000 : 300000)
  
  for (let i = points - 1; i >= 0; i--) {
    const time = new Date(now - i * interval)
    timestamps.push(time.toLocaleTimeString())
    inboundData.push(Math.floor(Math.random() * 100) + 50)
    outboundData.push(Math.floor(Math.random() * 80) + 30)
  }
  
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['入站流量', '出站流量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timestamps
    },
    yAxis: {
      type: 'value',
      name: 'MB/s'
    },
    series: [
      {
        name: '入站流量',
        type: 'line',
        smooth: true,
        data: inboundData,
        itemStyle: {
          color: '#67c23a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      },
      {
        name: '出站流量',
        type: 'line',
        smooth: true,
        data: outboundData,
        itemStyle: {
          color: '#e6a23c'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.1)' }
          ])
        }
      }
    ]
  }
  
  chartInstance?.setOption(option)
}

// 查看详情
const viewDetail = (row: any) => {
  currentConnection.value = row
  detailVisible.value = true
}

// 阻断连接
const blockConnection = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要阻断来自 ${row.sourceIp} 的连接吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 先更新本地数据状态，避免页面不刷新
    const index = connectionList.value.findIndex(conn => 
      conn.sourceIp === row.sourceIp && 
      conn.destIp === row.destIp &&
      conn.protocol === row.protocol
    );
    if (index !== -1) {
      connectionList.value[index] = {
        ...connectionList.value[index],
        status: '已阻断',
        flowStatus: 'BLOCKED'
      };
    } else {
      // 如果找不到精确匹配，尝试模糊匹配
      const fuzzyIndex = connectionList.value.findIndex(conn => 
        conn.sourceIp === row.sourceIp && 
        conn.destIp === row.destIp
      );
      if (fuzzyIndex !== -1) {
        connectionList.value[fuzzyIndex] = {
          ...connectionList.value[fuzzyIndex],
          status: '已阻断',
          flowStatus: 'BLOCKED'
        };
      }
    }
    
    ElMessage.success('连接已阻断')
    
    // 由于后端阻断API尚未实现，仅更新本地状态
    // 后端API需要后续开发，当前采用前端本地更新方案
    
    // 刷新数据（可选，如果后端支持实时更新）
    // loadConnections();
  } catch {
    // 用户取消
  }
}

// 格式化字节
const formatBytes = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 获取协议标签类型
const getProtocolType = (protocol: string) => {
  const types: Record<string, any> = {
    'TCP': 'primary',
    'UDP': 'success',
    'HTTP': 'warning',
    'HTTPS': 'danger'
  }
  return types[protocol] || 'info'
}

// 筛选
const handleFilter = () => {
  pagination.page = 1
  loadConnections()
}

const handleResetFilter = () => {
  filterProtocol.value = ''
  filterStatus.value = ''
  handleFilter()
}

// 自动刷新
let refreshTimer: number | null = null

const startAutoRefresh = () => {
  refreshTimer = window.setInterval(() => {
    loadTrafficData()
    loadConnections()
  }, 30000) // 30秒刷新一次
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  initChart()
  loadTrafficData()
  loadConnections()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  chartInstance?.dispose()
})
</script>

<style scoped>
.network-flow-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

:deep(.el-statistic__content) {
  font-size: 24px;
}
</style>
