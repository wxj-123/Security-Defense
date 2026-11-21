<template>
  <div class="threat-trend-analysis">
    <el-card class="card-wrapper">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <div class="header-actions">
            <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
              <el-radio-button value="24h">最近24小时</el-radio-button>
              <el-radio-button value="7d">最近7天</el-radio-button>
              <el-radio-button value="30d">最近30天</el-radio-button>
            </el-radio-group>
            <el-select v-model="selectedThreatTypes" multiple collapse-tags placeholder="选择威胁类型" size="small"
              @change="handleThreatTypeChange"
            >
              <el-option 
                v-for="type in threatTypeOptions" 
                :key="type.value" 
                :label="type.label" 
                :value="type.value"
              ></el-option>
            </el-select>
          </div>
        </div>
      </template>
      
      <div class="chart-container">
        <div ref="chartRef" id="threat-trend-chart" style="width: 100%; height: 300px;"></div>
      </div>
      
      <div class="threat-summary">
        <div class="summary-item"
          v-for="summary in threatSummaryData"
          :key="summary.type"
          :class="{ active: selectedThreatTypes.includes(summary.type) }"
          @click="toggleThreatType(summary.type)"
        >
          <div class="summary-icon" :style="{ backgroundColor: summary.color + '20' }"
            :class="summary.iconClass"
          >
            <el-icon :size="20" :color="summary.color">
              <component :is="summary.icon" />
            </el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-title">{{ summary.label }}</div>
            <div class="summary-count">{{ summary.count }}</div>
            <div class="summary-trend" :class="getTrendClass(summary.trend)">
              <el-icon v-if="summary.trend > 0" :size="14" color="#F56C6C">
                <ArrowUpBold />
              </el-icon>
              <el-icon v-else-if="summary.trend < 0" :size="14" color="#67C23A">
                <ArrowDownBold />
              </el-icon>
              <el-icon v-else :size="14" color="#909399">
                <Remove />
              </el-icon>
              <span>{{ Math.abs(summary.trend) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import * as echarts from 'echarts'
import { debounce } from 'lodash-es'
import { 
  Warning, 
  Key, 
  Monitor, 
  Cloudy, 
  Cellphone, 
  ArrowUpBold, 
  ArrowDownBold,
  Remove
} from '@element-plus/icons-vue'

// Props
interface Props {
  title?: string
  chartWidth?: number
  chartHeight?: number
}

const props = withDefaults(defineProps<Props>(), {
  title: '威胁趋势分析',
  chartWidth: 400,
  chartHeight: 300
})

// Emits
interface Emits {
  (e: 'threat-click', threatType: string): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const timeRange = ref('7d')
const chartRef = ref<HTMLElement | null>(null)

// 威胁类型选项
const threatTypeOptions = [
  { value: 'malware', label: '恶意软件' },
  { value: 'ransomware', label: '勒索软件' },
  { value: 'phishing', label: '钓鱼攻击' },
  { value: 'bruteforce', label: '暴力破解' },
  { value: 'ddos', label: 'DDoS攻击' },
  { value: 'vulnerability', label: '漏洞利用' },
  { value: 'insider', label: '内部威胁' },
  { value: 'data-leak', label: '数据泄露' }
]

// 默认选中所有威胁类型
const selectedThreatTypes = ref(threatTypeOptions.map(option => option.value))

// 威胁图标映射
const threatIcons: Record<string, any> = {
  'malware': markRaw(Warning),
  'ransomware': markRaw(Warning), // 使用 Warning 替代不存在的 Shield
  'phishing': markRaw(Cloudy),
  'bruteforce': markRaw(Key),
  'ddos': markRaw(Monitor),
  'vulnerability': markRaw(Monitor), // 使用 Monitor 替代不存在的图标
  'insider': markRaw(Cellphone),
  'data-leak': markRaw(Warning) // 使用 Warning 替代不存在的 Shield
}

// 威胁颜色映射
const threatColors: Record<string, string> = {
  'malware': '#F56C6C',
  'ransomware': '#E6A23C',
  'phishing': '#409EFF',
  'bruteforce': '#67C23A',
  'ddos': '#909399',
  'vulnerability': '#C06C84',
  'insider': '#F56C6C',
  'data-leak': '#E6A23C'
}

// 模拟的威胁趋势数据
const threatTrendData = ref<any>({})

// 威胁摘要数据
const threatSummaryData = ref<any[]>([])

// ECharts 实例和更新锁
let chartInstance: echarts.ECharts | null = null;
let isUpdating = false; // 防止并发更新的锁

// 防抖函数：立即执行 + 取消冗余调用（关键修复防抖失效）
const debouncedUpdateChart = debounce(
  () => {
    if (isUpdating || !chartInstance) return;
    isUpdating = true; // 加锁，防止递归

    try {
      // 彻底标准化数据格式（解决 axis 解析 + 数据格式问题）
      const timeRangeValue = timeRange.value;
      let labels: string[] = [];
      const now = new Date();
      
      if (timeRangeValue === '24h') {
        // 最近24小时，每2小时一个点
        for (let i = 23; i >= 0; i -= 2) {
          const time = new Date(now.getTime() - i * 60 * 60 * 1000);
          labels.push(time.getHours() + ':00');
        }
      } else if (timeRangeValue === '7d') {
        // 最近7天，每天一个点
        for (let i = 6; i >= 0; i--) {
          const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000);
          labels.push(`${date.getMonth() + 1}/${date.getDate()}`);
        }
      } else {
        // 最近30天，每5天一个点
        for (let i = 25; i >= 0; i -= 5) {
          const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000);
          labels.push(`${date.getMonth() + 1}/${date.getDate()}`);
        }
      }

      // 创建图表系列数据 - 确保所有数据都是原始值，不是响应式引用
      const series = JSON.parse(JSON.stringify(selectedThreatTypes.value.map(type => {
        const option = threatTypeOptions.find(opt => opt.value === type);
        const label = option ? option.label : type;
        
        // 获取对应类型的数据
        const typeData = threatTrendData.value[type] || [];
        
        // 如果数据不足，用0填充
        const maxLen = Math.max(labels.length, typeData.length);
        const paddedData = [...typeData].concat(Array(maxLen - typeData.length).fill(0));
        
        return {
          name: label,
          type: 'line',
          data: paddedData,
          smooth: true,
          lineStyle: {
            color: threatColors[type],
            width: 2
          },
          itemStyle: {
            color: threatColors[type]
          },
          emphasis: {
            lineStyle: {
              width: 3
            }
          }
        };
      })));

      // 图表配置：纯普通对象（无任何响应式引用）
      const option = JSON.parse(JSON.stringify({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: selectedThreatTypes.value.map(type => {
            const option = threatTypeOptions.find(opt => opt.value === type);
            return option ? option.label : type;
          }),
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
          boundaryGap: false,
          data: [...labels], // 使用展开运算符确保是新数组
          axisLabel: { interval: 0 }
        },
        yAxis: {
          type: 'value',
          min: 0
        },
        series: series
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

// 方法
const getTrendClass = (trend: number) => {
  if (trend > 0) return 'trend-increase'
  if (trend < 0) return 'trend-decrease'
  return 'trend-stable'
}

const handleTimeRangeChange = async () => {
  if (isUpdating) return; // 防止递归调用
  await loadThreatTrendData();
}

const handleThreatTypeChange = () => {
  if (isUpdating) return; // 防止递归调用
  nextTick(() => {
    debouncedUpdateChart();
  });
}

const toggleThreatType = (type: string) => {
  const index = selectedThreatTypes.value.indexOf(type)
  if (index >= 0) {
    if (selectedThreatTypes.value.length > 1) {
      selectedThreatTypes.value.splice(index, 1)
    }
  } else {
    selectedThreatTypes.value.push(type)
  }
  
  // 更新图表
  nextTick(() => {
    debouncedUpdateChart();
  });
}

const loadThreatTrendData = async () => {
  try {
    // 实际项目中，这里会调用API获取数据
    // const response = await getThreatTrendData(timeRange.value, selectedThreatTypes.value)
    // threatTrendData.value = response.data || {}
    // 目前使用模拟数据
    
    // 生成模拟数据
    const mockTrendData = generateMockTrendData()
    const mockSummaryData = generateMockSummaryData()
    
    // 深拷贝:彻底切断响应式关联(保留markRaw标记的图标)
    threatTrendData.value = JSON.parse(JSON.stringify(mockTrendData))
    // 直接使用生成的数据,不进行深拷贝,以保留markRaw标记的图标组件
    threatSummaryData.value = mockSummaryData
    
    // 延迟更新：避开 Vue 异步更新队列（核心：避免和组件渲染递归）
    nextTick(() => {
      debouncedUpdateChart();
    });
  } catch (error) {
    console.error('获取威胁趋势数据失败:', error);
    // 兜底：清空数据避免图表解析错误
    threatTrendData.value = {};
    threatSummaryData.value = [];
    nextTick(() => {
      debouncedUpdateChart();
    });
  }
}

// 生成模拟的趋势数据
function generateMockTrendData() {
  const data: Record<string, number[]> = {};
  const timeRangeValue = timeRange.value;
  const points = timeRangeValue === '24h' ? 12 : timeRangeValue === '7d' ? 7 : 6;
  
  threatTypeOptions.forEach(option => {
    const typeData = [];
    let baseValue = Math.floor(Math.random() * 100) + 50;
    
    for (let i = 0; i < points; i++) {
      // 生成有波动的趋势数据
      const variation = Math.sin(i * 0.5) * (baseValue * 0.3) + (Math.random() - 0.5) * (baseValue * 0.2);
      typeData.push(Math.max(0, Math.round(baseValue + variation)));
    }
    
    data[option.value] = typeData;
  });
  
  return data;
}

// 生成模拟的摘要数据
function generateMockSummaryData() {
  return threatTypeOptions.map(option => {
    // 确保图标组件不会被响应式化 - 使用 markRaw 包装
    const iconComponent = markRaw(threatIcons[option.value]);
    return {
      type: option.value,
      label: option.label,
      icon: iconComponent, // 使用 markRaw 确保不被响应式化
      iconClass: `icon-${option.value}`,
      color: threatColors[option.value],
      count: Math.floor(Math.random() * 500) + 50,
      trend: Math.round((Math.random() - 0.5) * 100)
    };
  });
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
      series: [{ type: 'line', data: [] }]
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
    loadThreatTrendData();
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
.threat-trend-analysis {
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

.header-actions {
  display: flex;
  gap: 16px;
}

.chart-container {
  flex: 1;
  min-height: 300px;
  margin-bottom: 20px;
}

.threat-summary {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: var(--el-bg-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.summary-item:hover {
  background-color: var(--el-bg-color-overlay);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.summary-item.active {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.summary-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  margin-right: 12px;
}

.summary-info {
  flex: 1;
}

.summary-title {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 4px;
}

.summary-count {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 2px;
}

.summary-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
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
</style>