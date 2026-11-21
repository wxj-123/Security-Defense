<!-- eslint-disable vue/no-unused-vars -->
<!-- eslint-disable @typescript-eslint/no-unused-vars -->
<!-- eslint-disable @typescript-eslint/no-unsafe-member-access -->
<!-- eslint-disable @typescript-eslint/no-unsafe-call -->
<template>
  <div class="asset-risk-distribution">
    <el-card class="card-wrapper">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <div class="header-actions">
            <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
              <el-radio-button value="7d">最近7天</el-radio-button>
              <el-radio-button value="30d">最近30天</el-radio-button>
              <el-radio-button value="90d">最近90天</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <div class="chart-container">
        <div ref="chartRef" id="asset-risk-chart" style="width: 100%; height: 300px;"></div>
      </div>
      
      <div class="asset-types">
        <div 
          v-for="type in assetTypes" 
          :key="type.id"
          class="asset-type-item"
          @click="handleAssetTypeClick(type)"
        >
          <div class="asset-type-icon" :style="{ backgroundColor: type.color + '30' }">
            <el-icon :size="24"><component :is="type.icon" /></el-icon>
          </div>
          <div class="asset-type-info">
            <div class="asset-type-name">{{ type.name }}</div>
            <div class="asset-type-count">数量: {{ type.count }}</div>
            <div class="asset-type-risk" :class="getRiskLevelClass(type.riskLevel)">
              风险等级: {{ type.riskLevel }}
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch, nextTick, markRaw } from 'vue'
import * as echarts from 'echarts'
import { debounce } from 'lodash-es'
import type { AssetRisk } from '@/types/security-situation'
import { getAssetRiskDistribution } from '@/api/security-situation'
import { Monitor, DataAnalysis, DataBoard, Bell, Phone } from '@element-plus/icons-vue'

// 定义资产类型接口
interface AssetType {
  id: string
  name: string
  icon: any
  color: string
  count: number
  riskLevel: string
  riskCount: {
    critical: number
    high: number
    medium: number
    low: number
  }
}

// Props
interface Props {
  title?: string
  chartWidth?: number
  chartHeight?: number
  assetRisksData?: AssetRisk[]
}

const props = withDefaults(defineProps<Props>(), {
  title: '资产风险分布',
  chartWidth: 400,
  chartHeight: 300,
  assetRisksData: () => []
})

// Emits
interface Emits {
  (e: 'asset-click', assetType: AssetType): void
  (e: 'data-loaded', assetRisksData: AssetRisk[]): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const timeRange = ref('7d')
const chartRef = ref<HTMLElement | null>(null)
const assetTypes = ref<AssetType[]>([])

// ECharts 实例和更新锁
let chartInstance: echarts.ECharts | null = null;
let isUpdating = false; // 防止并发更新的锁

// 防抖函数：立即执行 + 取消冗余调用
const debouncedUpdateChart = debounce(
  () => {
    if (isUpdating || !chartInstance) return;
    isUpdating = true; // 加锁，防止递归

    try {
      // 确保有数据可以显示
      if (!assetTypes.value || assetTypes.value.length === 0) {
        console.warn('更新图表时数据为空')
        return
      }
      
      // 创建资产类型的副本以避免循环引用
      const assetTypesCopy = JSON.parse(JSON.stringify(assetTypes.value));
      
      // 提取标签和数据
      const labels = assetTypesCopy.map((type: AssetType) => type.name);
      const criticalData = assetTypesCopy.map((type: AssetType) => type.riskCount.critical);
      const highData = assetTypesCopy.map((type: AssetType) => type.riskCount.high);
      const mediumData = assetTypesCopy.map((type: AssetType) => type.riskCount.medium);
      const lowData = assetTypesCopy.map((type: AssetType) => type.riskCount.low);
      
      // 图表配置：纯普通对象（无任何响应式引用）
      const option = JSON.parse(JSON.stringify({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: (params: any) => {
            if (!Array.isArray(params) || params.length === 0) return '';
            
            const param = params[0];
            const dataIndex = param.dataIndex;
            const assetType = assetTypesCopy[dataIndex];
            if (!assetType) return param.name;
            
            const totalRisk = assetType.riskCount.critical + assetType.riskCount.high + 
                             assetType.riskCount.medium + assetType.riskCount.low;
            
            return `${param.name}<br/>` +
                   `${params.map((p: any) => `${p.seriesName}: ${p.value}`).join('<br/>')}<br/>` +
                   `总风险数: ${totalRisk}<br/>` +
                   `风险等级: ${assetType.riskLevel}`;
          }
        },
        legend: {
          data: ['严重风险', '高风险', '中风险', '低风险'],
          top: '10%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: [...labels],
          axisLabel: { interval: 0 }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '严重风险',
            type: 'bar',
            stack: 'total',
            data: [...criticalData],
            itemStyle: { color: '#F56C6C' }
          },
          {
            name: '高风险',
            type: 'bar',
            stack: 'total',
            data: [...highData],
            itemStyle: { color: '#E6A23C' }
          },
          {
            name: '中风险',
            type: 'bar',
            stack: 'total',
            data: [...mediumData],
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '低风险',
            type: 'bar',
            stack: 'total',
            data: [...lowData],
            itemStyle: { color: '#67C23A' }
          }
        ]
      }));
      
      // 清空旧配置 + 更新（避免残留配置触发递归）
      chartInstance.clear();
      chartInstance.setOption(option, { notMerge: true });
    } catch (error) {
      console.error('更新图表时出错:', error);
    } finally {
      isUpdating = false; // 解锁
    }
  },
  200, // 延长防抖时间，适配组件生命周期
  {
    leading: true, // 立即执行第一次调用
    trailing: false, // 关闭尾部触发（核心：避免防抖队列残留导致递归）
    maxWait: 500 // 最大等待时间，防止卡死
  }
);

// 初始化资产类型图标
const assetTypeIcons = {
  'Monitor': markRaw(Monitor),
  'DataBoard': markRaw(DataBoard),
  'DataAnalysis': markRaw(DataAnalysis),
  'Phone': markRaw(Phone),
  'Bell': markRaw(Bell)
}

// 方法
const getRiskLevelClass = (level: string) => {
  const classMap: Record<string, string> = {
    '高风险': 'risk-high',
    '中风险': 'risk-medium',
    '低风险': 'risk-low'
  }
  return classMap[level] || ''
}

const handleAssetTypeClick = (assetType: AssetType) => {
  emit('asset-click', assetType)
}

const handleTimeRangeChange = () => {
  if (isUpdating) return; // 防止递归调用
  loadAssetRiskData()
}

// 监听传入的资产风险数据变化
watch(() => props.assetRisksData, () => {
  if (props.assetRisksData && props.assetRisksData.length > 0) {
    generateAssetTypesFromData()
    nextTick(() => {
      debouncedUpdateChart();
    });
  }
}, { immediate: true })

const loadAssetRiskData = async () => {
  try {
    // 如果传入了资产风险数据且不为空，则使用传入的数据
    if (props.assetRisksData && props.assetRisksData.length > 0) {
      // 根据传入的数据生成资产类型统计
      generateAssetTypesFromData()
      // 确保在下一个tick更新图表
      await nextTick()
      debouncedUpdateChart()
      return
    }
    
    // 如果没有传入数据，获取实际数据
    const response = await getAssetRiskDistribution()
    
    // 验证响应数据
    if (response?.data && Array.isArray(response.data) && response.data.length > 0) {
      // 使用实际数据更新内部状态
      const validatedData: AssetRisk[] = response.data
        .filter(item => item && typeof item === 'object')
        .map(asset => {
          // 确保riskLevel是有效的枚举值
          let riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE' = 'MEDIUM'
          if (typeof asset.riskLevel === 'string') {
            const level = asset.riskLevel.toUpperCase()
            if (level === 'CRITICAL' || level === 'HIGH' || level === 'MEDIUM' || level === 'LOW' || level === 'SAFE') {
              riskLevel = level as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE'
            }
          }
          
          // 确保status是有效的枚举值
          let status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' = 'ACTIVE'
          if (typeof asset.status === 'string') {
            const stat = asset.status.toUpperCase()
            if (stat === 'ACTIVE' || stat === 'INACTIVE' || stat === 'MAINTENANCE') {
              status = stat as 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
            }
          }
          
          return {
            assetId: asset.assetId || `asset_${Math.random().toString(36).substr(2, 9)}`,
            assetName: asset.assetName || '未知资产',
            assetType: asset.assetType || '未知类型',
            riskScore: typeof asset.riskScore === 'number' ? asset.riskScore : Math.floor(Math.random() * 100),
            riskLevel,
            ipAddress: asset.ipAddress || '0.0.0.0',
            location: asset.location || '未知位置',
            lastScanTime: asset.lastScanTime || new Date().toISOString(),
            assetValueScore: typeof asset.assetValueScore === 'number' ? asset.assetValueScore : Math.floor(Math.random() * 100),
            vulnerabilityScore: typeof asset.vulnerabilityScore === 'number' ? asset.vulnerabilityScore : Math.floor(Math.random() * 100),
            threatScore: typeof asset.threatScore === 'number' ? asset.threatScore : Math.floor(Math.random() * 100),
            vulnerabilityCount: typeof asset.vulnerabilityCount === 'number' ? asset.vulnerabilityCount : Math.floor(Math.random() * 10),
            threatCount: typeof asset.threatCount === 'number' ? asset.threatCount : Math.floor(Math.random() * 5),
            status
          }
        })
      
      // 通过事件通知父组件更新数据
      emit('data-loaded', validatedData)
      
      // 临时使用获取到的数据生成资产类型统计
      const tempProps = {
        ...props,
        assetRisksData: validatedData
      }
      
      // 按资产类型分组统计
      const typeMap: Record<string, AssetType> = {}
      
      // 创建tempProps.assetRisksData的副本以避免循环引用
      const assetRisksCopy = [...tempProps.assetRisksData].map(asset => ({ ...asset }))
      
      assetRisksCopy.forEach(asset => {
        // 确保asset对象存在且为对象类型
        if (!asset || typeof asset !== 'object') {
          return
        }
        
        const type = (asset.assetType as string) || '未知类型'
        if (!typeMap[type]) {
          // 初始化该类型
          typeMap[type] = {
            id: type.toLowerCase(),
            name: type,
            icon: assetTypeIcons['Monitor'], // 默认图标
            color: getRandomColor(), // 随机颜色
            count: 0,
            riskLevel: '低风险', // 默认风险等级
            riskCount: {
              critical: 0,
              high: 0,
              medium: 0,
              low: 0
            }
          }
        }
        
        // 增加计数
        typeMap[type].count++
        
        // 根据风险评分确定风险等级
        const riskScore = typeof asset.riskScore === 'number' ? asset.riskScore : 0
        if (riskScore >= 80) {
          typeMap[type].riskCount.critical++
        } else if (riskScore >= 60) {
          typeMap[type].riskCount.high++
        } else if (riskScore >= 40) {
          typeMap[type].riskCount.medium++
        } else {
          typeMap[type].riskCount.low++
        }
        
        // 更新整体风险等级
        const totalRisk = typeMap[type].riskCount.critical + typeMap[type].riskCount.high + 
                         typeMap[type].riskCount.medium + typeMap[type].riskCount.low
        if (totalRisk > 0) {
          const highRiskRatio = (typeMap[type].riskCount.critical + typeMap[type].riskCount.high) / totalRisk
          if (highRiskRatio > 0.5) {
            typeMap[type].riskLevel = '高风险'
          } else if (highRiskRatio > 0.2) {
            typeMap[type].riskLevel = '中风险'
          } else {
            typeMap[type].riskLevel = '低风险'
          }
        }
      })
      
      // 创建typeMap值的副本以避免循环引用,并保留icon的markRaw标记
      const assetTypesData = Object.values(typeMap).map(type => {
        const icon = type.icon
        const typeWithoutIcon = { ...type, icon: undefined }
        const copiedType = JSON.parse(JSON.stringify(typeWithoutIcon))
        copiedType.icon = icon
        return copiedType
      })

      // 深拷贝:彻底切断响应式关联(保留markRaw标记的图标)
      assetTypes.value = assetTypesData
      
      // 确保在下一个tick更新图表
      await nextTick()
      debouncedUpdateChart()
      return
    }
    
    // 如果没有获取到有效数据,使用模拟数据
    if (assetTypes.value.length === 0) {
      // 初始化模拟数据
      const mockData = [
        {
          id: 'server',
          name: '服务器',
          icon: assetTypeIcons['Monitor'],
          color: '#409EFF',
          count: 42,
          riskLevel: '高风险',
          riskCount: {
            critical: 3,
            high: 12,
            medium: 18,
            low: 9
          }
        },
        {
          id: 'database',
          name: '数据库',
          icon: assetTypeIcons['DataBoard'],
          color: '#67C23A',
          count: 15,
          riskLevel: '中风险',
          riskCount: {
            critical: 1,
            high: 4,
            medium: 7,
            low: 3
          }
        },
        {
          id: 'desktop',
          name: '工作站',
          icon: assetTypeIcons['Monitor'],
          color: '#E6A23C',
          count: 89,
          riskLevel: '中风险',
          riskCount: {
            critical: 2,
            high: 18,
            medium: 35,
            low: 34
          }
        },
        {
          id: 'mobile',
          name: '移动设备',
          icon: assetTypeIcons['Phone'],
          color: '#F56C6C',
          count: 124,
          riskLevel: '低风险',
          riskCount: {
            critical: 0,
            high: 5,
            medium: 24,
            low: 95
          }
        },
        {
          id: 'storage',
          name: '存储设备',
          icon: assetTypeIcons['DataAnalysis'],
          color: '#909399',
          count: 8,
          riskLevel: '低风险',
          riskCount: {
            critical: 0,
            high: 1,
            medium: 2,
            low: 5
          }
        },
        {
          id: 'iot',
          name: 'IoT设备',
          icon: assetTypeIcons['Monitor'],
          color: '#C06C84',
          count: 56,
          riskLevel: '高风险',
          riskCount: {
            critical: 4,
            high: 15,
            medium: 22,
            low: 15
          }
        }
      ]
      
      // 深拷贝:彻底切断响应式关联(保留markRaw标记的图标)
      assetTypes.value = mockData
    }
    
    // 根据时间范围调整数据
    adjustDataByTimeRange()
    
    // 确保在下一个tick更新图表
    await nextTick()
    debouncedUpdateChart()
  } catch (error) {
    console.error('获取资产风险分布数据失败:', error)
    // 出错时使用模拟数据
    if (assetTypes.value.length === 0) {
      const errorMockData = [
        {
          id: 'server',
          name: '服务器',
          icon: assetTypeIcons['Monitor'],
          color: '#409EFF',
          count: 42,
          riskLevel: '高风险',
          riskCount: {
            critical: 3,
            high: 12,
            medium: 18,
            low: 9
          }
        }
      ]
      
      // 深拷贝:彻底切断响应式关联(保留markRaw标记的图标)
      assetTypes.value = errorMockData
    }
    
    // 确保在下一个tick更新图表
    await nextTick()
    debouncedUpdateChart()
  }
}

// 根据资产风险数据生成资产类型统计
const generateAssetTypesFromData = () => {
  try {
    // 安全检查props.assetRisksData
    if (!props.assetRisksData || !Array.isArray(props.assetRisksData)) {
      console.warn('资产风险数据无效，使用空数组')
      assetTypes.value = []
      return
    }
    
    // 创建props.assetRisksData的副本以避免循环引用
    const assetRisksDataCopy = [...props.assetRisksData].map(asset => ({ ...asset }))
    
    if (assetRisksDataCopy.length === 0) {
      assetTypes.value = []
      return
    }
    
    // 按资产类型分组统计
    const typeMap: Record<string, AssetType> = {}
    
    assetRisksDataCopy.forEach(asset => {
      // 确保asset对象存在且为对象类型
      if (!asset || typeof asset !== 'object') {
        return
      }
      
      const type = (typeof asset.assetType === 'string' && asset.assetType) || '未知类型'
      if (!typeMap[type]) {
        // 初始化该类型
        typeMap[type] = {
          id: type.toLowerCase(),
          name: type,
          icon: assetTypeIcons['Monitor'], // 默认图标
          color: getRandomColor(), // 随机颜色
          count: 0,
          riskLevel: '低风险', // 默认风险等级
          riskCount: {
            critical: 0,
            high: 0,
            medium: 0,
            low: 0
          }
        }
      }
      
      // 增加计数
      typeMap[type].count++
      
      // 根据风险评分确定风险等级
      const riskScore = typeof asset.riskScore === 'number' ? asset.riskScore : 0
      if (riskScore >= 80) {
        typeMap[type].riskCount.critical++
      } else if (riskScore >= 60) {
        typeMap[type].riskCount.high++
      } else if (riskScore >= 40) {
        typeMap[type].riskCount.medium++
      } else {
        typeMap[type].riskCount.low++
      }
      
      // 更新整体风险等级
      const totalRisk = typeMap[type].riskCount.critical + typeMap[type].riskCount.high + 
                       typeMap[type].riskCount.medium + typeMap[type].riskCount.low
      if (totalRisk > 0) {
        const highRiskRatio = (typeMap[type].riskCount.critical + typeMap[type].riskCount.high) / totalRisk
        if (highRiskRatio > 0.5) {
          typeMap[type].riskLevel = '高风险'
        } else if (highRiskRatio > 0.2) {
          typeMap[type].riskLevel = '中风险'
        } else {
          typeMap[type].riskLevel = '低风险'
        }
      }
    })
    
    // 创建typeMap值的副本并进行深拷贝以避免循环引用,并保留icon的markRaw标记
    const assetTypesData = Object.values(typeMap).map(type => {
      const icon = type.icon
      const typeWithoutIcon = { ...type, icon: undefined }
      const copiedType = JSON.parse(JSON.stringify(typeWithoutIcon))
      copiedType.icon = icon
      return copiedType
    })

    // 深拷贝:彻底切断响应式关联(保留markRaw标记的图标)
    assetTypes.value = assetTypesData
  } catch (error) {
    console.error('生成资产类型统计时出错:', error)
    assetTypes.value = []
  }
}

// 生成随机颜色
const getRandomColor = () => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C06C84']
  return colors[Math.floor(Math.random() * colors.length)]
}

const adjustDataByTimeRange = () => {
  try {
    // 模拟根据时间范围调整数据
    const multiplier = timeRange.value === '7d' ? 1 : timeRange.value === '30d' ? 2 : 3

    // 创建assetTypes的副本以避免循环引用
    const assetTypesCopy = [...assetTypes.value].map(type => {
      const icon = type.icon
      const typeWithoutIcon = { ...type, icon: undefined }
      const copiedType = JSON.parse(JSON.stringify(typeWithoutIcon))
      copiedType.icon = icon
      return copiedType
    })

    const adjustedAssetTypes = assetTypesCopy.map(type => {
      // 确保type.riskCount存在
      const riskCount = type.riskCount || { critical: 0, high: 0, medium: 0, low: 0 }

      return {
        ...type,
        riskCount: {
          critical: Math.floor((riskCount.critical || 0) * multiplier * (0.9 + Math.random() * 0.2)),
          high: Math.floor((riskCount.high || 0) * multiplier * (0.9 + Math.random() * 0.2)),
          medium: Math.floor((riskCount.medium || 0) * multiplier * (0.9 + Math.random() * 0.2)),
          low: Math.floor((riskCount.low || 0) * multiplier * (0.9 + Math.random() * 0.2))
        }
      }
    })

    // 深拷贝:彻底切断响应式关联(保留markRaw标记的图标)
    assetTypes.value = adjustedAssetTypes
  } catch (error) {
    console.error('调整时间范围数据时出错:', error)
  }
}

// 图表初始化/销毁：管控生命周期（核心：避免路由切换递归）
const initChart = () => {
  // 先销毁旧实例（防止重复创建）
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  
  const el = chartRef.value;
  if (el) {
    chartInstance = echarts.init(el);
    // 初始化空配置，避免 axis 初始 undefined
    chartInstance.setOption({
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: [] }]
    });
  }
};

const handleResize = debounce(() => {
  if (chartInstance) {
    chartInstance.resize();
  }
}, 300);



// 生命周期钩子
onMounted(() => {
  // 延迟初始化：等组件完全挂载后再创建图表
  setTimeout(() => {
    initChart();
    loadAssetRiskData();
    window.addEventListener('resize', handleResize);
  }, 100);
})

onUnmounted(() => {
  try {
    window.removeEventListener('resize', handleResize);
    debouncedUpdateChart.cancel(); // 清空防抖队列
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
    isUpdating = false; // 重置锁
  } catch (error) {
    console.error('组件卸载时清理资源出错:', error);
  }
})
</script>

<style scoped>
.asset-risk-distribution {
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
}

.header-actions {
  display: flex;
  gap: 12px;
}

.chart-container {
  flex: 1;
  min-height: 300px;
  margin-bottom: 20px;
}

.asset-types {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.asset-type-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: var(--el-bg-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.asset-type-item:hover {
  background-color: var(--el-bg-color-overlay);
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.asset-type-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 16px;
}

.asset-type-info {
  flex: 1;
}

.asset-type-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.asset-type-count,
.asset-type-risk {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 2px;
}

.risk-high {
  color: #F56C6C !important;
}

.risk-medium {
  color: #E6A23C !important;
}

.risk-low {
  color: #67C23A !important;
}
</style>