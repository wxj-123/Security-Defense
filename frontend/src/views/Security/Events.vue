<template>
  <div class="events-container">
    <!-- 响应引擎概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #f0f9ff;">
              <el-icon :size="40" color="#409eff"><Warning /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ stats.totalEvents }}</div>
              <div class="overview-label">待处理事件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #fef0f0;">
              <el-icon :size="40" color="#f56c6c"><WarnTriangleFilled /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ stats.highPriority }}</div>
              <div class="overview-label">高优先级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #f0f9ff;">
              <el-icon :size="40" color="#409eff"><TrendCharts /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ stats.automated }}</div>
              <div class="overview-label">自动处置</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #fef5e7;">
              <el-icon :size="40" color="#e6a23c"><Check /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ stats.resolved }}</div>
              <div class="overview-label">已解决</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 响应引擎控制面板 -->
    <el-card class="control-panel">
      <template #header>
        <div class="control-header">
          <span>自动化响应引擎</span>
          <div class="control-actions">
            <el-tag :type="engineStatus === 'running' ? 'success' : 'info'" size="small">
              {{ engineStatus === 'running' ? '运行中' : '已停止' }}
            </el-tag>
            <el-button 
              :type="engineStatus === 'running' ? 'danger' : 'success'" 
              size="small" 
              @click="toggleEngine"
            >
              {{ engineStatus === 'running' ? '停止引擎' : '启动引擎' }}
            </el-button>
            <el-button type="primary" size="small" @click="manualScan">
              <el-icon><Refresh /></el-icon>
              手动扫描
            </el-button>
          </div>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="engine-feature">
            <el-icon :size="32" color="#409eff"><Sort /></el-icon>
            <h3>智能事件分类</h3>
            <p>基于AI算法自动识别和分类安全事件</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="engine-feature">
            <el-icon :size="32" color="#67c23a"><MagicStick /></el-icon>
            <h3>优先级排序</h3>
            <p>根据威胁等级和影响范围自动排序</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="engine-feature">
            <el-icon :size="32" color="#e6a23c"><Lightning /></el-icon>
            <h3>自动处置</h3>
            <p>一键执行预定义的应急响应流程</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 事件列表 -->
    <el-card>
      <template #header>
        <div class="events-header">
          <span>安全事件列表</span>
          <div class="events-filter">
            <el-radio-group v-model="eventFilter" size="small">
              <el-radio-button :value="'all'">全部</el-radio-button>
              <el-radio-button :value="'pending'">待处理</el-radio-button>
              <el-radio-button :value="'processing'">处理中</el-radio-button>
              <el-radio-button :value="'resolved'">已解决</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <el-table :data="filteredEvents" border stripe>
        <el-table-column prop="id" label="事件ID" width="100" />
        <el-table-column prop="name" label="事件名称" width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getEventTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detectionTime" label="检测时间" width="180" />
        <el-table-column prop="responseTime" label="响应时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === '待处理'" 
              type="primary" 
              size="small" 
              @click="handleEvent(row)"
            >
              处置事件
            </el-button>
            <el-button 
              v-if="row.status === '处理中'" 
              type="success" 
              size="small" 
              @click="completeEvent(row)"
            >
              完成处置
            </el-button>
            <el-button size="small" @click="viewEventDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 处置对话框 -->
    <el-dialog v-model="responseDialogVisible" :title="`处置事件: ${currentEvent?.name}`" width="700px">
      <div v-if="currentEvent">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件ID">{{ currentEvent.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ currentEvent.type }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentEvent.priority)">{{ currentEvent.priority }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="检测时间">{{ currentEvent.detectionTime }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentEvent.description }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">自动化处置建议</el-divider>
        <div class="recommendations">
          <el-alert 
            v-for="(rec, index) in recommendations" 
            :key="index"
            :title="rec.title"
            :type="rec.type as any"
            :description="rec.description"
            show-icon
            style="margin-bottom: 10px;"
          />
        </div>
        
        <el-divider content-position="left">应急响应流程</el-divider>
        <el-steps :active="activeStep" finish-status="success" simple>
          <el-step title="隔离受感染主机" />
          <el-step title="阻断恶意流量" />
          <el-step title="收集取证信息" />
          <el-step title="清除恶意文件" />
          <el-step title="恢复系统服务" />
        </el-steps>
        
        <div class="step-content" v-if="activeStep < 5">
          <h4>{{ getStepTitle(activeStep) }}</h4>
          <p>{{ getStepDescription(activeStep) }}</p>
          <el-button type="primary" @click="nextStep">执行此步骤</el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="responseDialogVisible = false">取消</el-button>
        <el-button 
          v-if="activeStep === 5" 
          type="success" 
          @click="completeResponse"
        >
          完成处置
        </el-button>
      </template>
    </el-dialog>

    <!-- 事件详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="事件详情" 
      width="900px"
      :open-delay="0"
      :destroy-on-close="true"
      append-to-body
    >
      <div v-if="viewingEvent">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="事件ID">{{ viewingEvent.id }}</el-descriptions-item>
              <el-descriptions-item label="名称">{{ viewingEvent.name }}</el-descriptions-item>
              <el-descriptions-item label="类型">{{ viewingEvent.type }}</el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="getPriorityType(viewingEvent.priority)">{{ viewingEvent.priority }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(viewingEvent.status)">{{ viewingEvent.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="检测时间">{{ viewingEvent.detectionTime }}</el-descriptions-item>
              <el-descriptions-item label="响应时间">{{ viewingEvent.responseTime }}</el-descriptions-item>
              <el-descriptions-item label="解决时间">{{ viewingEvent.resolveTime }}</el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">{{ viewingEvent.description }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="详细分析" name="analysis">
            <el-card>
              <template #header>
                <span>攻击链分析</span>
              </template>
              <div ref="attackChainRef" id="attack-chain-chart" style="width: 800px; height: 400px; display: block !important;"></div>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane label="处置记录" name="actions">
            <el-timeline>
              <el-timeline-item
                v-for="(action, index) in viewingEvent.actions"
                :key="index"
                :timestamp="action.time"
                placement="top"
              >
                <el-card>
                  <h4>{{ action.title }}</h4>
                  <p>{{ action.description }}</p>
                  <p><strong>执行人:</strong> {{ action.operator }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Warning,
  WarnTriangleFilled,
  TrendCharts,
  Check,
  Refresh,
  Sort,
  MagicStick,
  Lightning
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

// 引擎状态
const engineStatus = ref<'running' | 'stopped'>('running')

// 统计数据
const stats = reactive({
  totalEvents: 24,
  highPriority: 8,
  automated: 15,
  resolved: 9
})

// 事件过滤
const eventFilter = ref('all')

// 事件列表
const events = ref([
  {
    id: 'EV20251110001',
    name: '大规模DDoS攻击',
    type: 'DDoS攻击',
    priority: '高危',
    status: '待处理',
    detectionTime: '2025-11-10 14:30:22',
    responseTime: '',
    resolveTime: '',
    description: '检测到针对核心业务系统的DDoS攻击，流量峰值达到10Gbps',
    actions: [
      { title: '启动DDoS防护', description: '启用流量清洗服务', operator: '系统', time: '2025-11-10 14:32:15' }
    ]
  },
  {
    id: 'EV20251110002',
    name: 'APT组织横向移动',
    type: 'APT攻击',
    priority: '高危',
    status: '处理中',
    detectionTime: '2025-11-10 13:45:18',
    responseTime: '2025-11-10 13:47:30',
    resolveTime: '',
    description: '检测到APT组织在内网中的横向移动行为，已感染3台主机',
    actions: [
      { title: '隔离受感染主机', description: '断开3台主机网络连接', operator: '安全管理员', time: '2025-11-10 13:48:22' },
      { title: '阻断恶意通信', description: '在防火墙添加阻断规则', operator: '安全管理员', time: '2025-11-10 13:50:15' }
    ]
  },
  {
    id: 'EV20251110003',
    name: '勒索软件加密活动',
    type: '勒索软件',
    priority: '紧急',
    status: '待处理',
    detectionTime: '2025-11-10 12:15:44',
    responseTime: '',
    resolveTime: '',
    description: '检测到勒索软件加密活动，已加密50个文件',
    actions: []
  },
  {
    id: 'EV20251110004',
    name: 'SQL注入攻击尝试',
    type: '漏洞利用',
    priority: '中危',
    status: '已解决',
    detectionTime: '2025-11-10 11:30:12',
    responseTime: '2025-11-10 11:32:45',
    resolveTime: '2025-11-10 11:45:20',
    description: '检测到针对Web应用的SQL注入攻击尝试',
    actions: [
      { title: '阻断攻击IP', description: '在防火墙添加黑名单规则', operator: '系统', time: '2025-11-10 11:32:45' },
      { title: '修复漏洞', description: '更新Web应用代码修复SQL注入漏洞', operator: '开发人员', time: '2025-11-10 11:40:30' }
    ]
  }
])

// 过滤后的事件
const filteredEvents = computed(() => {
  if (eventFilter.value === 'all') return events.value
  return events.value.filter(event => {
    if (eventFilter.value === 'pending') return event.status === '待处理'
    if (eventFilter.value === 'processing') return event.status === '处理中'
    if (eventFilter.value === 'resolved') return event.status === '已解决'
    return true
  })
})

// 对话框状态
const responseDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentEvent = ref<any>(null)
const viewingEvent = ref<any>(null)
const activeTab = ref('basic')

// 处置流程
const activeStep = ref(0)
const recommendations = ref([
  {
    title: '网络隔离',
    type: 'warning',
    description: '建议立即隔离受感染的网络区域，防止攻击扩散'
  },
  {
    title: '流量阻断',
    type: 'info',
    description: '在防火墙添加规则阻断恶意流量通信'
  },
  {
    title: '主机取证',
    type: 'success',
    description: '收集受感染主机的内存和磁盘镜像用于后续分析'
  }
])

// 图表引用
const attackChainRef = ref<HTMLElement>()
let attackChainChart: ECharts | null = null
let resizeHandler: (() => void) | null = null

// 切换引擎状态
const toggleEngine = () => {
  engineStatus.value = engineStatus.value === 'running' ? 'stopped' : 'running'
  ElMessage.success(`自动化响应引擎已${engineStatus.value === 'running' ? '启动' : '停止'}`)
}

// 手动扫描
const manualScan = () => {
  ElMessage.info('正在执行手动扫描...')
  setTimeout(() => {
    ElMessage.success('扫描完成，发现2个新威胁')
    // 模拟新增事件
    events.value.unshift({
      id: 'EV' + Date.now(),
      name: '新型恶意软件活动',
      type: '恶意软件',
      priority: '中危',
      status: '待处理',
      detectionTime: new Date().toLocaleString('zh-CN'),
      responseTime: '',
      resolveTime: '',
      description: '检测到新型恶意软件活动，正在尝试连接C&C服务器',
      actions: []
    })
    stats.totalEvents++
  }, 2000)
}

// 处置事件
const handleEvent = (event: any) => {
  currentEvent.value = event
  activeStep.value = 0
  responseDialogVisible.value = true
}

// 完成事件
const completeEvent = (event: any) => {
  event.status = '已解决'
  event.resolveTime = new Date().toLocaleString('zh-CN')
  stats.resolved++
  ElMessage.success(`事件 ${event.name} 处置完成`)
}

// 查看事件详情
const viewEventDetail = async (event: any) => {
  viewingEvent.value = event
  
  // 确保分析选项卡是激活状态
  activeTab.value = 'analysis'
  
  detailDialogVisible.value = true
  
  // 等待DOM更新
  await nextTick()
  
  // 延迟一段时间确保弹窗动画完成
  await new Promise(resolve => setTimeout(resolve, 800))
  
  // 直接初始化图表，不再轮询检测
  initAttackChainChart()
}

// 获取事件类型标签
const getEventTypeTag = (type: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    'DDoS攻击': 'danger',
    'APT攻击': 'warning',
    '勒索软件': 'danger',
    '漏洞利用': 'warning',
    '恶意软件': 'warning'
  }
  return types[type] || 'info'
}

// 获取优先级标签
const getPriorityType = (priority: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '紧急': 'danger',
    '高危': 'warning',
    '中危': 'primary',
    '低危': 'success'
  }
  return types[priority] || 'info'
}

// 获取状态标签
const getStatusType = (status: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const types: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '待处理': 'warning',
    '处理中': 'primary',
    '已解决': 'success'
  }
  return types[status] || 'info'
}

// 处置流程步骤标题
const getStepTitle = (step: number) => {
  const titles = [
    '隔离受感染主机',
    '阻断恶意流量',
    '收集取证信息',
    '清除恶意文件',
    '恢复系统服务'
  ]
  return titles[step] || ''
}

// 处置流程步骤描述
const getStepDescription = (step: number) => {
  const descriptions = [
    '断开受感染主机的网络连接，防止攻击扩散到其他系统',
    '在防火墙和IPS设备上添加规则，阻断恶意流量通信',
    '收集受感染主机的内存转储和磁盘镜像，用于后续分析',
    '使用杀毒软件清除恶意文件，删除恶意注册表项',
    '验证系统完整性，恢复被破坏的服务和数据'
  ]
  return descriptions[step] || ''
}

// 下一步
const nextStep = () => {
  if (activeStep.value < 5) {
    activeStep.value++
    ElMessage.success(`已完成步骤: ${getStepTitle(activeStep.value - 1)}`)
  }
}

// 完成处置
const completeResponse = () => {
  if (currentEvent.value) {
    currentEvent.value.status = '已解决'
    currentEvent.value.resolveTime = new Date().toLocaleString('zh-CN')
    stats.resolved++
    ElMessage.success(`事件 ${currentEvent.value.name} 处置完成`)
  }
  responseDialogVisible.value = false
}

// 初始化攻击链图表
const initAttackChainChart = () => {
  let chartDom = attackChainRef.value
  
  // 检查容器是否存在
  if (!chartDom) {
    console.warn('攻击链图表容器不存在')
    return
  }
  
  console.log('容器宽高：', chartDom.clientWidth, chartDom.clientHeight)
  
  // 如果容器宽高为0，尝试强制设置尺寸
  if (chartDom.clientWidth === 0 || chartDom.clientHeight === 0) {
    chartDom.style.width = '800px'
    chartDom.style.height = '400px'
    chartDom.style.display = 'block'
    
    // 强制重排
    chartDom.offsetHeight
    
    console.log('强制设置尺寸后容器宽高：', chartDom.clientWidth, chartDom.clientHeight)
  }
  
  // 再次检查尺寸
  if (chartDom.clientWidth === 0 || chartDom.clientHeight === 0) {
    // 尝试通过选择器重新获取元素
    const element = document.querySelector('#attack-chain-chart')
    if (element) {
      chartDom = element as HTMLElement
      chartDom.style.width = '800px'
      chartDom.style.height = '400px'
      chartDom.style.display = 'block'
      chartDom.offsetHeight
      console.log('重新获取元素后容器宽高：', chartDom.clientWidth, chartDom.clientHeight)
    }
  }
  
  // 最终检查尺寸
  if (!chartDom || chartDom.clientWidth === 0 || chartDom.clientHeight === 0) {
    // 最后的兜底方案：创建一个全新的容器
    const tabPanel = document.querySelector('.el-tab-pane[name="analysis"] .el-card__body')
    if (tabPanel) {
      // 清空面板内容
      tabPanel.innerHTML = ''
      
      // 创建新的容器
      const newChartContainer = document.createElement('div')
      newChartContainer.id = 'attack-chain-chart'
      newChartContainer.style.width = '800px'
      newChartContainer.style.height = '400px'
      newChartContainer.style.display = 'block'
      
      // 添加到面板
      tabPanel.appendChild(newChartContainer)
      
      // 使用新容器
      chartDom = newChartContainer
      console.log('创建全新容器后宽高：', chartDom.clientWidth, chartDom.clientHeight)
    }
  }
  
  // 最终检查尺寸
  if (!chartDom || chartDom.clientWidth === 0 || chartDom.clientHeight === 0) {
    console.warn('容器宽度或高度仍为0，无法初始化图表')
    return
  }

  // 初始化图表（先销毁旧实例，避免重复渲染）
  attackChainChart?.dispose()
  attackChainChart = echarts.init(chartDom)
  
  // 设置图表配置项并渲染
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: ['初始入侵', '权限提升', '横向移动', '数据收集', '数据加密', '勒索要求']
    },
    yAxis: {
      type: 'value',
      name: '攻击强度'
    },
    series: [{
      data: [10, 25, 40, 35, 50, 45],
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      },
      itemStyle: {
        color: '#409eff'
      }
    }]
  }
  
  attackChainChart.setOption(option)

  // 监听窗口resize，自适应图表尺寸
  const handleResize = () => {
    attackChainChart?.resize()
  }
  
  // 移除之前的事件监听器（如果有）
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  
  window.addEventListener('resize', handleResize)
  resizeHandler = handleResize
}

// 组件挂载
onMounted(() => {
  // 模拟引擎自动处理
  const autoProcess = setInterval(() => {
    if (engineStatus.value === 'running') {
      // 随机处理一个待处理事件
      const pendingEvents = events.value.filter(e => e.status === '待处理')
      if (pendingEvents.length > 0 && Math.random() < 0.3) {
        const event = pendingEvents[Math.floor(Math.random() * pendingEvents.length)]
        event.status = '处理中'
        event.responseTime = new Date().toLocaleString('zh-CN')
        stats.automated++
        ElMessage.info(`自动化引擎正在处置事件: ${event.name}`)
      }
    }
  }, 5000)
})

// 组件卸载
onUnmounted(() => {
  attackChainChart?.dispose()
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
})
</script>

<style scoped>
.events-container {
  padding: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  border-left: 4px solid #409eff;
}

.overview-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.overview-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.control-panel {
  margin-bottom: 20px;
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.engine-feature {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.engine-feature:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.engine-feature h3 {
  margin: 15px 0 10px;
  color: #303133;
}

.engine-feature p {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
}

.events-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-content {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f5f7fa;
}

.step-content h4 {
  margin: 0 0 10px;
  color: #303133;
}

.step-content p {
  margin: 0 0 20px;
  color: #606266;
  line-height: 1.6;
}
</style>