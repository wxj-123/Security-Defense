<template>
  <div class="risk-heatmap">
    <div class="page-header">
      <h2>资产风险热力图</h2>
      <div class="header-controls">
        <el-select v-model="viewMode" @change="handleViewModeChange" style="width: 150px">
          <el-option label="网格视图" value="grid" />
          <el-option label="列表视图" value="list" />
          <el-option label="地图视图" value="map" />
        </el-select>
        <el-select v-model="riskFilter" @change="handleFilterChange" style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="高危" value="high" />
          <el-option label="中危" value="medium" />
          <el-option label="低危" value="low" />
        </el-select>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 热力图统计 -->
    <div class="stats-row">
      <el-card class="stat-card" v-for="(stat, index) in heatmapStats" :key="index">
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

    <!-- 网格视图 -->
    <div v-if="viewMode === 'grid'" class="grid-view">
      <el-card class="heatmap-card">
        <template #header>
          <div class="card-header">
            <span>资产风险分布网格</span>
            <div class="legend">
              <span class="legend-item">
                <div class="legend-color critical"></div>
                <span>危急</span>
              </span>
              <span class="legend-item">
                <div class="legend-color high"></div>
                <span>高危</span>
              </span>
              <span class="legend-item">
                <div class="legend-color medium"></div>
                <span>中危</span>
              </span>
              <span class="legend-item">
                <div class="legend-color low"></div>
                <span>低危</span>
              </span>
              <span class="legend-item">
                <div class="legend-color safe"></div>
                <span>安全</span>
              </span>
            </div>
          </div>
        </template>
        <div class="heatmap-grid" v-loading="loading">
          <div
            v-for="asset in filteredHeatmapData"
            :key="asset.assetId"
            class="grid-item"
            :class="getRiskClass(asset.riskScore)"
            @click="viewAssetDetail(asset)"
            :title="`${asset.assetName} - 风险分数: ${asset.riskScore}`"
          >
            <div class="asset-info">
              <div class="asset-name">{{ asset.assetName }}</div>
              <div class="asset-type">{{ asset.assetType }}</div>
              <div class="risk-score">{{ asset.riskScore }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 列表视图 -->
    <div v-if="viewMode === 'list'" class="list-view">
      <el-card class="list-card">
        <template #header>
          <div class="card-header">
            <span>资产风险列表</span>
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
          :data="paginatedListData"
          stripe
          v-loading="loading"
          @row-click="viewAssetDetail"
          row-class-name="table-row-hover"
        >
          <el-table-column prop="assetName" label="资产名称" min-width="150" />
          <el-table-column prop="assetType" label="资产类型" width="120" />
          <el-table-column prop="ipAddress" label="IP地址" width="130" />
          <el-table-column prop="riskScore" label="风险分数" width="150">
            <template #default="scope">
              <div class="risk-progress">
                <el-progress 
                  :percentage="Number(scope.row.riskScore)" 
                  :color="getProgressColor(scope.row.riskScore)"
                  :stroke-width="10"
                />
                <span class="progress-text">{{ scope.row.riskScore }}</span>
              </div>
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
            :total="filteredAssetList.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 地图视图 -->
    <div v-if="viewMode === 'map'" class="map-view">
      <el-card class="map-card">
        <template #header>
          <div class="card-header">
            <span>资产地理分布图</span>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="refreshMapData">
                <el-icon><Refresh /></el-icon>
                刷新地图
              </el-button>
            </div>
          </div>
        </template>
        <div class="map-container">
          <div ref="mapRef" class="map-chart" v-loading="mapLoading"></div>
          <div class="map-legend">
            <div class="legend-title">风险等级</div>
            <div class="legend-items">
              <div class="legend-item">
                <div class="legend-color critical"></div>
                <span>危急 (80-100)</span>
              </div>
              <div class="legend-item">
                <div class="legend-color high"></div>
                <span>高危 (60-79)</span>
              </div>
              <div class="legend-item">
                <div class="legend-color medium"></div>
                <span>中危 (40-59)</span>
              </div>
              <div class="legend-item">
                <div class="legend-color low"></div>
                <span>低危 (20-39)</span>
              </div>
              <div class="legend-item">
                <div class="legend-color safe"></div>
                <span>安全 (0-19)</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 风险分布图表 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>风险等级分布</span>
            </template>
            <div class="chart-container">
              <canvas ref="riskDistChart" height="300"></canvas>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>地理位置风险分析</span>
            </template>
            <div class="chart-container">
              <canvas ref="locationChart" height="300"></canvas>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, markRaw, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Search, Location, InfoFilled, Warning, SuccessFilled } from '@element-plus/icons-vue'
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
  BarController,
  DoughnutController,
  ArcElement
} from 'chart.js'
import * as echarts from 'echarts'
import { getAssetRiskDistribution, getAssetGeographicDistribution } from '@/api/security-situation'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, BarElement, BarController, DoughnutController, ArcElement)

// 响应式数据
const viewMode = ref('grid')
const riskFilter = ref('all')
const assetsData = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// 地图相关数据
const geographicData = ref<any>(null)
const mapLoading = ref(false)
const mapRef = ref<HTMLDivElement>()
let mapInstance: echarts.ECharts | null = null

// 图表引用
const riskDistChart = ref<HTMLCanvasElement>()
const locationChart = ref<HTMLCanvasElement>()
let riskDistChartInstance: Chart | null = null
let locationChartInstance: Chart | null = null

// 路由
const router = useRouter()

// 计算属性
const filteredHeatmapData = computed(() => {
  let data = assetsData.value
  
  if (riskFilter.value !== 'all') {
    const filterMap: {[key: string]: number[]} = {
      'high': [80, 100],
      'medium': [40, 79],
      'low': [0, 39]
    }
    const [min, max] = filterMap[riskFilter.value] || [0, 100]
    data = data.filter(asset => asset.riskScore >= min && asset.riskScore <= max)
  }
  
  return data
})

const filteredAssetList = computed(() => {
  let data = assetsData.value
  
  if (riskFilter.value !== 'all') {
    const filterMap: {[key: string]: string} = {
      'high': 'CRITICAL',
      'medium': 'HIGH,MEDIUM',
      'low': 'LOW,SAFE'
    }
    const levels = filterMap[riskFilter.value]?.split(',') || []
    data = data.filter(asset => levels.includes(asset.riskLevel))
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(asset => 
      asset.assetName?.toLowerCase().includes(keyword) ||
      asset.ipAddress?.toLowerCase().includes(keyword) ||
      asset.location?.toLowerCase().includes(keyword)
    )
  }
  
  return data
})

const paginatedListData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAssetList.value.slice(start, end)
})

const heatmapStats = computed(() => {
  const stats = {
    total: assetsData.value.length,
    critical: assetsData.value.filter(a => a.riskScore >= 80).length,
    high: assetsData.value.filter(a => a.riskScore >= 60 && a.riskScore < 80).length,
    avgScore: Math.round(assetsData.value.reduce((sum, a) => sum + (a.riskScore || 0), 0) / assetsData.value.length || 0)
  }
  
  return [
    { label: '资产总数', value: stats.total, color: '#409eff', icon: markRaw(InfoFilled) },
    { label: '危急资产', value: stats.critical, color: '#dc3545', icon: markRaw(Warning) },
    { label: '高危资产', value: stats.high, color: '#fd7e14', icon: markRaw(Warning) },
    { label: '平均分数', value: stats.avgScore, color: '#17a2b8', icon: markRaw(SuccessFilled) }
  ]
})

// 生命周期钩子
onMounted(() => {
  fetchData()
})

onUnmounted(() => {
  // 清理地图实例
  if (mapInstance) {
    mapInstance.dispose()
    mapInstance = null
  }
})

// 方法
const fetchData = async () => {
  loading.value = true
  try {
    const response = await getAssetRiskDistribution()
    
    // 增强数据验证和类型保护
    let data: any[] = []
    
    if (Array.isArray(response?.data)) {
      data = response.data
    } else if (response?.data && typeof response.data === 'object') {
      // 如果返回的是单个对象，转换为数组
      data = [response.data]
    } else {
      // 如果数据格式不正确，使用空数组
      data = []
    }
    
    // 进一步验证数组中的每个元素
    assetsData.value = data
      .filter(item => item && typeof item === 'object')
      .map(asset => ({
        ...asset,
        assetId: asset.assetId || `asset_${Math.random().toString(36).substr(2, 9)}`,
        assetName: asset.assetName || '未知资产',
        assetType: asset.assetType || '未知类型',
        ipAddress: asset.ipAddress || '0.0.0.0',
        location: asset.location || '未知位置',
        riskScore: typeof asset.riskScore === 'number' ? asset.riskScore : Math.floor(Math.random() * 100),
        riskLevel: getRiskLevelFromScore(typeof asset.riskScore === 'number' ? asset.riskScore : Math.floor(Math.random() * 100)),
        lastScanTime: asset.lastScanTime || new Date().toISOString()
      }))
    
    updateCharts()
  } catch (error) {
    ElMessage.error('获取资产风险数据失败')
    console.error('获取数据失败:', error)
    // 如果API调用失败，使用模拟数据
    assetsData.value = generateMockData()
    updateCharts()
  } finally {
    loading.value = false
  }
}

const updateCharts = async () => {
  await nextTick()
  updateRiskDistChart()
  updateLocationChart()
}

const updateRiskDistChart = () => {
  if (riskDistChartInstance) {
    riskDistChartInstance.destroy()
  }

  const ctx = riskDistChart.value?.getContext('2d')
  if (!ctx) return

  const ranges = [
    { label: '危急(80-100)', min: 80, max: 100, color: '#dc3545' },
    { label: '高危(60-79)', min: 60, max: 79, color: '#fd7e14' },
    { label: '中危(40-59)', min: 40, max: 59, color: '#ffc107' },
    { label: '低危(20-39)', min: 20, max: 39, color: '#28a745' },
    { label: '安全(0-19)', min: 0, max: 19, color: '#17a2b8' }
  ]

  const data = ranges.map(range => ({
    label: range.label,
    count: assetsData.value.filter(a => a.riskScore >= range.min && a.riskScore <= range.max).length,
    backgroundColor: range.color
  }))

  riskDistChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: data.map(d => d.label),
      datasets: [{
        label: '资产数量',
        data: data.map(d => d.count),
        backgroundColor: data.map(d => d.backgroundColor),
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
  })
}

const updateLocationChart = () => {
  if (locationChartInstance) {
    locationChartInstance.destroy()
  }

  const ctx = locationChart.value?.getContext('2d')
  if (!ctx) return

  const locationCount: {[key: string]: number} = {}
  assetsData.value.forEach(asset => {
    const location = asset.location || '未知位置'
    locationCount[location] = (locationCount[location] || 0) + 1
  })

  locationChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: Object.keys(locationCount),
      datasets: [{
        data: Object.values(locationCount),
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
      },
      // 添加安全的事件处理配置
      events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
    }
  })
}

// 事件处理
const refreshData = () => {
  fetchData()
  ElMessage.success('数据已刷新')
}

const handleViewModeChange = () => {
  currentPage.value = 1
}

const handleFilterChange = () => {
  currentPage.value = 1
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

const viewAssetDetail = (asset: any) => {
  router.push(`/security-situation/asset/${asset.assetId}`)
}

// 辅助函数
const getRiskClass = (score: number) => {
  if (score >= 80) return 'critical'
  if (score >= 60) return 'high'
  if (score >= 40) return 'medium'
  if (score >= 20) return 'low'
  return 'safe'
}

const getRiskLevelFromScore = (score: number) => {
  if (score >= 80) return 'CRITICAL'
  if (score >= 60) return 'HIGH'
  if (score >= 40) return 'MEDIUM'
  if (score >= 20) return 'LOW'
  return 'SAFE'
}

const getRiskLevelType = (level: string) => {
  const typeMap: {[key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger'} = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'warning',
    'LOW': 'success',
    'SAFE': 'success'
  }
  return typeMap[level] || 'info'
}

const getRiskLevelText = (level: string) => {
  const textMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高危',
    'MEDIUM': '中危',
    'LOW': '低危',
    'SAFE': '安全'
  }
  return textMap[level] || level
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

// 生成模拟数据函数
const generateMockData = () => {
  const assetTypes = ['服务器', '网络设备', '工作站', '数据库', '应用系统', '移动设备']
  const locations = ['北京数据中心', '上海机房', '广州办公室', '深圳研发中心', '成都分部', '武汉分公司']
  
  const mockData = []
  
  for (let i = 0; i < 50; i++) {
    const randomType = assetTypes[Math.floor(Math.random() * assetTypes.length)]
    const randomLocation = locations[Math.floor(Math.random() * locations.length)]
    
    mockData.push({
      assetId: `asset_${i + 1}`,
      assetName: `${randomType}${i + 1}`,
      assetType: randomType,
      ipAddress: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
      location: randomLocation,
      riskScore: Math.floor(Math.random() * 100),
      riskLevel: getRiskLevelFromScore(Math.floor(Math.random() * 100)),
      lastScanTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
    })
  }
  
  return mockData
}

// 地图相关方法
const initMap = async () => {
  await nextTick()
  const container = mapRef.value
  if (!container) return

  try {
    // 加载中国地图数据
    await loadChinaMap()
    
    // 初始化ECharts实例
    if (mapInstance) {
      mapInstance.dispose()
    }
    mapInstance = echarts.init(container)
    
    // 获取地理分布数据
    await fetchGeographicData()
    
    // 响应窗口大小变化
    window.addEventListener('resize', handleMapResize)
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

// 加载中国地图数据
const loadChinaMap = async () => {
  try {
    // 尝试从阿里云DataV加载中国地图数据
    const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    const geoJson = await response.json()
    echarts.registerMap('china', geoJson)
    return true
  } catch (error) {
    console.warn('加载中国地图数据失败，使用简化版:', error)
    // 如果加载失败，创建一个简化的中国地图轮廓
    createSimplifiedMap()
    return false
  }
}

// 创建简化版中国地图
const createSimplifiedMap = () => {
  // 使用简化的中国地图边界数据
  const chinaGeoJson = {
    type: 'FeatureCollection',
    features: [
      {
        type: 'Feature',
        properties: { name: '中国' },
        geometry: {
          type: 'Polygon',
          coordinates: [[[73.5, 53.5], [134.7, 53.5], [134.7, 18.2], [73.5, 18.2], [73.5, 53.5]]]
        }
      }
    ]
  }
  echarts.registerMap('china', chinaGeoJson)
}

const fetchGeographicData = async () => {
  mapLoading.value = true
  try {
    const response = await getAssetGeographicDistribution()
    geographicData.value = response.data.data
    updateMapChart()
  } catch (error) {
    console.error('获取地理分布数据失败:', error)
    ElMessage.error('获取地理分布数据失败')
  } finally {
    mapLoading.value = false
  }
}

const updateMapChart = () => {
  if (!mapInstance || !geographicData.value) return

  const { assets, locationStats } = geographicData.value
  
  // 准备散点数据
  const scatterData = assets.map((asset: any) => ({
    name: asset.assetName,
    value: [asset.longitude, asset.latitude, asset.riskScore],
    itemStyle: {
      color: getRiskColor(asset.riskScore)
    },
    riskLevel: asset.riskLevel,
    riskScore: asset.riskScore,
    location: asset.location,
    ipAddress: asset.ipAddress,
    assetType: asset.assetType
  }))

  // 准备统计数据
  const statData = Object.values(locationStats).map((stat: any) => ({
    name: stat.name,
    value: [stat.longitude, stat.latitude, stat.value],
    assetCount: stat.assetCount,
    highRiskCount: stat.highRiskCount
  }))

  const option = {
    backgroundColor: '#f5f7fa',
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.data) {
          const data = params.data
          if (data.riskScore !== undefined) {
            return `
              <div style="padding: 8px;">
                <div style="font-weight: bold; margin-bottom: 5px;">${data.name}</div>
                <div>类型: ${data.assetType}</div>
                <div>位置: ${data.location}</div>
                <div>IP: ${data.ipAddress}</div>
                <div style="margin-top: 5px;">
                  <span style="color: ${getRiskColor(data.riskScore)}">风险分数: ${data.riskScore}</span>
                  <span style="margin-left: 10px; padding: 2px 6px; border-radius: 3px; background: ${getRiskColor(data.riskScore)}; color: white;">${getRiskLevelText(data.riskLevel)}</span>
                </div>
              </div>
            `
          } else if (data.assetCount !== undefined) {
            return `
              <div style="padding: 8px;">
                <div style="font-weight: bold; margin-bottom: 5px;">${data.name}</div>
                <div>资产数量: ${data.assetCount}</div>
                <div>高危资产: ${data.highRiskCount}</div>
                <div>平均风险分数: ${data.value[2]}</div>
              </div>
            `
          }
        }
        return params.name
      }
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: false
        }
      },
      itemStyle: {
        areaColor: '#e3e7ed',
        borderColor: '#999'
      }
    },
    series: [
      {
        name: '资产分布',
        type: 'scatter',
        coordinateSystem: 'geo',
        data: scatterData,
        symbolSize: (val: any) => {
          const size = Math.max(8, Math.min(25, val[2] / 5))
          return size
        },
        label: {
          show: false,
          formatter: '{b}'
        },
        emphasis: {
          label: {
            show: true
          }
        },
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      {
        name: '城市统计',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: statData,
        symbolSize: (val: any) => Math.max(15, Math.min(30, val[2] / 3)),
        showEffectOn: 'render',
        rippleEffect: {
          brushType: 'stroke',
          scale: 3
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{b}',
          fontSize: 12
        },
        itemStyle: {
          color: '#409eff',
          shadowBlur: 10,
          shadowColor: '#409eff'
        },
        zlevel: 1
      }
    ]
  }

  mapInstance.setOption(option)
}

const getRiskColor = (score: number) => {
  if (score >= 80) return '#dc3545'
  if (score >= 60) return '#fd7e14'
  if (score >= 40) return '#ffc107'
  if (score >= 20) return '#28a745'
  return '#17a2b8'
}

const refreshMapData = () => {
  fetchGeographicData()
  ElMessage.success('地图数据已刷新')
}

const handleMapResize = () => {
  if (mapInstance) {
    mapInstance.resize()
  }
}

// 监听视图模式变化，切换到地图视图时初始化地图
watch(viewMode, (newMode) => {
  if (newMode === 'map') {
    nextTick(() => {
      initMap()
    })
  }
})


</script>

<style scoped>
.risk-heatmap {
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

/* 网格视图样式 */
.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  padding: 20px;
  max-height: 600px;
  overflow-y: auto;
}

.grid-item {
  aspect-ratio: 1;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
}

.grid-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.grid-item.critical {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
}

.grid-item.high {
  background: linear-gradient(135deg, #fd7e14, #e85d04);
  color: white;
}

.grid-item.medium {
  background: linear-gradient(135deg, #ffc107, #e0a800);
  color: #333;
}

.grid-item.low {
  background: linear-gradient(135deg, #28a745, #1e7e34);
  color: white;
}

.grid-item.safe {
  background: linear-gradient(135deg, #17a2b8, #138496);
  color: white;
}

.asset-info {
  text-align: center;
  padding: 10px;
}

.asset-name {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 12px;
}

.asset-type {
  font-size: 10px;
  opacity: 0.8;
  margin-bottom: 5px;
}

.risk-score {
  font-size: 16px;
  font-weight: bold;
}

.legend {
  display: flex;
  gap: 15px;
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
}

.legend-color.critical { background: #dc3545; }
.legend-color.high { background: #fd7e14; }
.legend-color.medium { background: #ffc107; }
.legend-color.low { background: #28a745; }
.legend-color.safe { background: #17a2b8; }

/* 列表视图样式 */
.table-row-hover:hover {
  background-color: #f5f7fa !important;
  cursor: pointer;
}

.risk-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  min-width: 30px;
  text-align: center;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 地图视图样式 */
.map-view {
  margin-bottom: 20px;
}

.map-card {
  margin-bottom: 20px;
}

.map-container {
  height: 600px;
  position: relative;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
}

.map-chart {
  width: 100%;
  height: 100%;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  max-width: 200px;
}

.legend-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
  font-size: 14px;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
}

.legend-color.critical { background: #dc3545; }
.legend-color.high { background: #fd7e14; }
.legend-color.medium { background: #ffc107; }
.legend-color.low { background: #28a745; }
.legend-color.safe { background: #17a2b8; }

.map-placeholder {
  text-align: center;
  color: #909399;
}

.map-placeholder p {
  margin: 10px 0;
}

.map-hint {
  font-size: 14px;
  opacity: 0.8;
}

/* 图表样式 */
.chart-section {
  margin-top: 20px;
}

.chart-container {
  position: relative;
  height: 300px;
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

/* 响应式布局 */
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
  
  .heatmap-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
  
  .stats-row {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}
</style>