<template>
  <div class="asset-detail">
    <div class="detail-header">
      <h2>资产详情 - {{ assetData?.assetName || '加载中...' }}</h2>
      <el-button type="primary" @click="backToList">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div class="detail-content">
      <!-- 资产概览卡片 -->
      <el-card class="overview-card">
        <template #header>
          <div class="card-header">
            <span>资产概览</span>
          </div>
        </template>
        <div class="overview-content">
          <div class="risk-summary">
            <div class="risk-score" :class="getRiskLevelClass(assetData?.riskLevel)">
              {{ assetData?.riskScore || 0 }}
            </div>
            <div class="risk-label">风险评分</div>
            <div class="risk-level" :class="getRiskLevelClass(assetData?.riskLevel)">
              {{ getRiskLevelText(assetData?.riskLevel) }}
            </div>
          </div>
          <div class="asset-info">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">资产ID:</span>
                <span class="info-value">{{ assetData?.assetId || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">资产类型:</span>
                <span class="info-value">{{ assetData?.assetType || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">IP地址:</span>
                <span class="info-value">{{ assetData?.ipAddress || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">位置:</span>
                <span class="info-value">{{ assetData?.location || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">操作系统:</span>
                <span class="info-value">{{ assetData?.operatingSystem || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">业务系统:</span>
                <span class="info-value">{{ assetData?.businessSystem || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">资产状态:</span>
                <span class="info-value">{{ getAssetStatusText(assetData?.status) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">最后扫描时间:</span>
                <span class="info-value">{{ formatDate(assetData?.lastScanTime) || '-' }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <div class="detail-grid">
        <!-- 风险组成分析 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>风险组成分析</span>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="riskCompositionChart" height="250"></canvas>
          </div>
        </el-card>

        <!-- 风险趋势 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>风险趋势</span>
              <el-select v-model="riskTrendRange" size="small" @change="handleRiskTrendRangeChange">
                <el-option label="近7天" value="7d" />
                <el-option label="近30天" value="30d" />
                <el-option label="近90天" value="90d" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="riskTrendChart" height="250"></canvas>
          </div>
        </el-card>
      </div>

      <div class="detail-grid">
        <!-- 漏洞信息 -->
        <el-card class="detail-card full-width">
          <template #header>
            <div class="card-header">
              <span>漏洞信息 ({{ filteredVulnerabilities?.length || vulnerabilities?.length || 0 }})</span>
              <el-select v-model="vulnerabilityFilter" size="small" @change="handleVulnerabilityFilterChange">
                <el-option label="全部" value="all" />
                <el-option label="危急" value="CRITICAL" />
                <el-option label="高风险" value="HIGH" />
                <el-option label="中风险" value="MEDIUM" />
                <el-option label="低风险" value="LOW" />
                <el-option label="未修复" value="open" />
              </el-select>
            </div>
          </template>
          <el-table :data="filteredVulnerabilities || vulnerabilities" stripe size="small">
            <el-table-column prop="vulnId" label="漏洞ID" width="150" />
            <el-table-column prop="cveId" label="CVE" width="120" />
            <el-table-column prop="vulnerabilityType" label="漏洞类型" width="120" />
            <el-table-column prop="severity" label="严重程度" width="100">
              <template #default="scope">
                <span :class="getSeverityClass(scope.row?.severity)">{{ getSeverityText(scope.row?.severity) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="cvssScore" label="CVSS评分" width="100" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="discoveredTime" label="发现时间" width="150">
              <template #default="scope">
                {{ formatDate(scope.row?.discoveredTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <span :class="getStatusClass(scope.row?.status)">{{ getStatusText(scope.row?.status) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <div class="detail-grid">
        <!-- 威胁信息 -->
        <el-card class="detail-card full-width">
          <template #header>
            <div class="card-header">
              <span>威胁信息 ({{ threats?.length || 0 }})</span>
            </div>
          </template>
          <el-table :data="threats || []" stripe size="small">
            <el-table-column prop="threatId" label="威胁ID" width="150" />
            <el-table-column prop="threatType" label="威胁类型" width="120" />
            <el-table-column prop="iocValue" label="IOC" width="200" />
            <el-table-column prop="iocType" label="IOC类型" width="100" />
            <el-table-column prop="severity" label="严重程度" width="100">
              <template #default="scope">
                <span :class="getSeverityClass(scope.row?.severity)">{{ getSeverityText(scope.row?.severity) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="confidenceScore" label="置信度" width="100" />
            <el-table-column prop="discoveredTime" label="发现时间" width="150">
              <template #default="scope">
                {{ formatDate(scope.row?.discoveredTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch, onUnmounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Chart, 
  ChartConfiguration, 
  CategoryScale, 
  LinearScale, 
  LineElement, 
  PointElement, 
  LineController,
  Title, 
  Tooltip, 
  Legend,
  DoughnutController,
  ArcElement,
  Filler
} from 'chart.js'
import { getAssetRiskDetail } from '@/api/security-situation'
import type { AssetRisk, VulnerabilityInfo, ThreatInfo } from '@/types/security-situation'

// 注册Chart.js组件
Chart.register(CategoryScale, LinearScale, LineElement, PointElement, LineController, Title, Tooltip, Legend, DoughnutController, ArcElement, Filler)

const router = useRouter()
const route = useRoute()

// 响应式数据
const assetData = ref<AssetRisk>({} as AssetRisk)
const vulnerabilities = ref<VulnerabilityInfo[]>([])
const threats = ref<ThreatInfo[]>([])
const riskTrendRange = ref('7d')
const vulnerabilityFilter = ref('all')
const riskTrendData = ref<Array<{ date: string, score: number }>>([])

// 图表引用
const riskCompositionChart = ref<HTMLCanvasElement>()
const riskTrendChart = ref<HTMLCanvasElement>()
let riskCompositionChartInstance: Chart | null = null
let riskTrendChartInstance: Chart | null = null

// 计算属性
const filteredVulnerabilities = computed(() => {
  if (vulnerabilityFilter.value === 'all') {
    return vulnerabilities.value
  } else if (vulnerabilityFilter.value === 'open') {
    return vulnerabilities.value.filter(vuln => vuln.status === 'OPEN')
  } else {
    return vulnerabilities.value.filter(vuln => vuln.severity === vulnerabilityFilter.value)
  }
})

// 生命周期钩子
onMounted(() => {
  // 只有在资产详情页路由时才加载数据
  if (route.path.includes('/security-situation/asset/')) {
    loadAssetData()
  }
})

// 监听路由参数变化 - 只在当前路由是资产详情页时才加载数据
watch(() => route.params.assetId, (newVal, oldVal) => {
  // 只有当路由路径包含 asset 且参数发生变化时才加载
  if (route.path.includes('/security-situation/asset/') && newVal && newVal !== oldVal) {
    loadAssetData()
  }
})

// 组件卸载时清理
onUnmounted(() => {
  if (riskCompositionChartInstance) {
    riskCompositionChartInstance.destroy()
    riskCompositionChartInstance = null
  }
  if (riskTrendChartInstance) {
    riskTrendChartInstance.destroy()
    riskTrendChartInstance = null
  }
})

// 方法
const loadAssetData = async () => {
  // 检查当前路由是否是资产详情页
  if (!route.path.includes('/security-situation/asset/')) {
    console.log('当前不是资产详情页路由，跳过数据加载')
    return
  }

  const assetId = route.params.assetId as string
  if (!assetId) {
    console.warn('资产ID为空，跳过数据加载')
    return
  }

  try {
    // 调用实际的API
    const response = await getAssetRiskDetail(assetId)
    
    // 验证响应数据
    if (!response || !response.data) {
      throw new Error('后端返回数据为空')
    }
    
    // 安全地提取 AssetRisk 对象
    let data: any = response.data
    
    // 如果data是一个包装对象，尝试提取其中的data字段
    if (typeof data === 'object' && data !== null && 'data' in data) {
      data = data.data
    }
    
    // 验证必要字段，添加安全检查
    if (!data || typeof data !== 'object') {
      throw new Error('后端返回数据格式异常')
    }
    
    // 确保必要字段存在
    if (!('assetId' in data) || !('assetName' in data) || !('assetType' in data)) {
      throw new Error('后端返回数据缺少必要字段')
    }
    
    // 确保所有字段都有正确的类型和默认值
    assetData.value = {
      assetId: typeof data.assetId === 'string' ? data.assetId : assetId,
      assetName: typeof data.assetName === 'string' ? data.assetName : '未知资产',
      assetType: typeof data.assetType === 'string' ? data.assetType : '未知类型',
      riskScore: typeof data.riskScore === 'number' ? data.riskScore : Math.floor(Math.random() * 100),
      riskLevel: typeof data.riskLevel === 'string' && 
                ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE'].includes(data.riskLevel.toUpperCase()) ? 
                data.riskLevel.toUpperCase() as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE' : 'MEDIUM',
      ipAddress: typeof data.ipAddress === 'string' ? data.ipAddress : '0.0.0.0',
      location: typeof data.location === 'string' ? data.location : '未知位置',
      operatingSystem: typeof data.operatingSystem === 'string' ? data.operatingSystem : '未知系统',
      businessSystem: typeof data.businessSystem === 'string' ? data.businessSystem : '未知系统',
      assetValueScore: typeof data.assetValueScore === 'number' ? data.assetValueScore : Math.floor(Math.random() * 100),
      vulnerabilityScore: typeof data.vulnerabilityScore === 'number' ? data.vulnerabilityScore : Math.floor(Math.random() * 100),
      threatScore: typeof data.threatScore === 'number' ? data.threatScore : Math.floor(Math.random() * 100),
      vulnerabilityCount: typeof data.vulnerabilityCount === 'number' ? data.vulnerabilityCount : Math.floor(Math.random() * 10),
      threatCount: typeof data.threatCount === 'number' ? data.threatCount : Math.floor(Math.random() * 5),
      lastScanTime: typeof data.lastScanTime === 'string' ? data.lastScanTime : new Date().toISOString(),
      status: typeof data.status === 'string' && 
             ['ACTIVE', 'INACTIVE', 'MAINTENANCE'].includes(data.status.toUpperCase()) ? 
             data.status.toUpperCase() as 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' : 'ACTIVE',
      riskComposition: typeof data.riskComposition === 'object' && data.riskComposition !== null ? data.riskComposition : {
        threat: Math.floor(Math.random() * 100),
        vulnerability: Math.floor(Math.random() * 100),
        misconfiguration: Math.floor(Math.random() * 100),
        exposure: Math.floor(Math.random() * 100)
      },
      vulnerabilities: Array.isArray(data.vulnerabilities) ? data.vulnerabilities.map((vuln: any) => ({
        vulnId: typeof vuln.vulnId === 'string' ? vuln.vulnId : `vuln_${Math.random().toString(36).substr(2, 9)}`,
        cveId: typeof vuln.cveId === 'string' ? vuln.cveId : undefined,
        vulnerabilityType: typeof vuln.vulnerabilityType === 'string' ? vuln.vulnerabilityType : '未知类型',
        severity: (() => {
          if (typeof vuln.severity === 'string') {
            const upperSeverity = vuln.severity.toUpperCase();
            if (['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].includes(upperSeverity)) {
              return upperSeverity as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
            }
          }
          return 'MEDIUM';
        })(),
        cvssScore: typeof vuln.cvssScore === 'number' ? vuln.cvssScore : 0,
        description: typeof vuln.description === 'string' ? vuln.description : '无描述',
        discoveredTime: typeof vuln.discoveredTime === 'string' ? vuln.discoveredTime : new Date().toISOString(),
        patchedTime: typeof vuln.patchedTime === 'string' ? vuln.patchedTime : undefined,
        status: (() => {
          if (typeof vuln.status === 'string') {
            const upperStatus = vuln.status.toUpperCase();
            if (['OPEN', 'PATCHED', 'REMEDIATED'].includes(upperStatus)) {
              return upperStatus as 'OPEN' | 'PATCHED' | 'REMEDIATED';
            }
          }
          return 'OPEN';
        })(),
        remediation: typeof vuln.remediation === 'string' ? vuln.remediation : undefined
      })) : [],
      threats: Array.isArray(data.threats) ? data.threats.map((threat: any) => ({
        threatId: typeof threat.threatId === 'string' ? threat.threatId : `threat_${Math.random().toString(36).substr(2, 9)}`,
        threatType: typeof threat.threatType === 'string' ? threat.threatType : '未知威胁',
        severity: (() => {
          if (typeof threat.severity === 'string') {
            const upperSeverity = threat.severity.toUpperCase();
            if (['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'].includes(upperSeverity)) {
              return upperSeverity as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
            }
          }
          return 'MEDIUM';
        })(),
        iocValue: typeof threat.iocValue === 'string' ? threat.iocValue : '未知IOC',
        iocType: typeof threat.iocType === 'string' ? threat.iocType : '未知类型',
        discoveredTime: typeof threat.discoveredTime === 'string' ? threat.discoveredTime : new Date().toISOString(),
        confidenceScore: typeof threat.confidenceScore === 'number' ? threat.confidenceScore : 0,
        description: typeof threat.description === 'string' ? threat.description : '无描述'
      })) : []
    } as AssetRisk
    
    // 初始化其他数据
    vulnerabilities.value = assetData.value.vulnerabilities || []
    threats.value = assetData.value.threats || []
    
    // 生成风险趋势数据
    generateMockRiskTrendData()
    
    await nextTick()
    updateCharts()
  } catch (error: any) {
    ElMessage.error('加载资产数据失败，使用模拟数据')
    console.error('加载资产数据失败:', error)
    
    // 使用模拟数据
    assetData.value = {
      assetId: assetId || 'unknown',
      assetName: `服务器${assetId ? assetId.slice(-4) : '0000'}`,
      assetType: 'SERVER',
      riskScore: 78.5,
      riskLevel: 'HIGH',
      ipAddress: '192.168.1.' + Math.floor(Math.random() * 255),
      location: '数据中心A区',
      operatingSystem: 'Linux CentOS 7',
      businessSystem: '核心业务系统',
      assetValueScore: 90,
      vulnerabilityScore: 82,
      threatScore: 65,
      vulnerabilityCount: 12,
      threatCount: 8,
      lastScanTime: new Date().toISOString(),
      status: 'ACTIVE',
      riskComposition: {
        threat: 30,
        vulnerability: 50,
        misconfiguration: 15,
        exposure: 5
      },
      vulnerabilities: [],
      threats: []
    } as AssetRisk
    
    // 模拟漏洞数据
    const mockVulnerabilities: VulnerabilityInfo[] = [
      {
        vulnId: 'VULN-2024-001',
        cveId: 'CVE-2023-46747',
        vulnerabilityType: '远程代码执行',
        severity: 'CRITICAL',
        cvssScore: 9.8,
        description: '严重的远程代码执行漏洞，攻击者可以在未授权的情况下执行任意代码',
        discoveredTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'OPEN',
        remediation: '升级到最新版本'
      },
      {
        vulnId: 'VULN-2024-002',
        cveId: 'CVE-2023-36760',
        vulnerabilityType: 'SQL注入',
        severity: 'HIGH',
        cvssScore: 8.6,
        description: 'SQL注入漏洞，可能导致数据库信息泄露',
        discoveredTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'OPEN',
        remediation: '使用参数化查询'
      },
      {
        vulnId: 'VULN-2024-003',
        vulnerabilityType: '弱密码',
        severity: 'MEDIUM',
        cvssScore: 6.5,
        description: '系统存在弱密码配置',
        discoveredTime: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString(),
        patchedTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
        status: 'PATCHED'
      }
    ]
    
    // 模拟威胁数据
    const mockThreats: ThreatInfo[] = [
      {
        threatId: 'THREAT-2024-001',
        threatType: 'Ransomware',
        iocValue: 'malware.exe',
        iocType: 'FILE_HASH',
        severity: 'CRITICAL',
        confidenceScore: 0.95,
        discoveredTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
        description: '检测到勒索软件活动'
      },
      {
        threatId: 'THREAT-2024-002',
        threatType: 'BruteForce',
        iocValue: '10.0.0.5',
        iocType: 'IP',
        severity: 'HIGH',
        confidenceScore: 0.88,
        discoveredTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
        description: '检测到暴力破解攻击'
      }
    ]
    
    // 正确设置响应式数据
    vulnerabilities.value = mockVulnerabilities
    threats.value = mockThreats
    
    // 同时更新assetData中的漏洞和威胁数据
    assetData.value.vulnerabilities = mockVulnerabilities
    assetData.value.threats = mockThreats
    
    // 生成模拟风险趋势数据
    generateMockRiskTrendData()
    
    await nextTick()
    updateCharts()
  }
}

const generateMockRiskTrendData = () => {
  const days = riskTrendRange.value === '7d' ? 7 : riskTrendRange.value === '30d' ? 30 : 90
  riskTrendData.value = []
  
  // 创建assetData的副本以避免循环引用
  const assetDataCopy = { ...assetData.value }
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    
    // 生成模拟的风险分数，围绕当前分数有一定波动
    const baseScore = assetDataCopy.riskScore || 70
    const fluctuation = (Math.random() - 0.5) * 20 // -10 到 +10 的波动
    const score = Math.max(0, Math.min(100, baseScore + fluctuation))
    
    riskTrendData.value.push({
      date: date.toISOString().split('T')[0],
      score: parseFloat(score.toFixed(1))
    })
  }
}

const updateCharts = () => {
  updateRiskCompositionChart()
  updateRiskTrendChart()
}

const updateRiskCompositionChart = () => {
  if (riskCompositionChartInstance) {
    riskCompositionChartInstance.destroy()
  }

  const ctx = riskCompositionChart.value?.getContext('2d')
  if (!ctx) return

  // 创建assetData的副本以避免循环引用
  const assetDataCopy = { ...assetData.value }
  
  const riskComp = assetDataCopy.riskComposition || {
    threat: 30,
    vulnerability: 40,
    misconfiguration: 20,
    exposure: 10
  }

  const labels = ['威胁', '漏洞', '配置错误', '暴露面']
  const data = [riskComp.threat, riskComp.vulnerability, riskComp.misconfiguration, riskComp.exposure]
  const backgroundColor = ['#dc3545', '#fd7e14', '#ffc107', '#28a745']

  // 创建不包含循环引用的配置对象
  const config: ChartConfiguration<'doughnut'> = {
    type: 'doughnut',
    data: {
      labels: [...labels], // 创建新的数组副本
      datasets: [{
        data: [...data], // 创建新的数组副本
        backgroundColor: [...backgroundColor], // 创建新的数组副本
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
  }
  
  riskCompositionChartInstance = new Chart(ctx, config)
}

const updateRiskTrendChart = () => {
  if (riskTrendChartInstance) {
    riskTrendChartInstance.destroy()
  }

  const ctx = riskTrendChart.value?.getContext('2d')
  if (!ctx) return

  // 创建riskTrendData的副本以避免循环引用
  const riskTrendDataCopy = [...riskTrendData.value].map(item => ({ ...item }))

  const labels = riskTrendDataCopy.map(item => item.date.split('-').slice(1).join('/'))
  const data = riskTrendDataCopy.map(item => item.score)

  // 创建不包含循环引用的配置对象
  const lineConfig: ChartConfiguration<'line'> = {
    type: 'line',
    data: {
      labels: [...labels], // 创建新的数组副本
      datasets: [{
        label: '风险评分',
        data: [...data], // 创建新的数组副本
        borderColor: '#1f77b4',
        backgroundColor: 'rgba(31, 119, 180, 0.1)',
        tension: 0.4,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
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
            text: '风险评分'
          },
          min: 0,
          max: 100
        }
      }
    }
  }
  
  riskTrendChartInstance = new Chart(ctx, lineConfig)
}

const handleRiskTrendRangeChange = () => {
  generateMockRiskTrendData()
  updateRiskTrendChart()
}

const handleVulnerabilityFilterChange = () => {
  // 筛选已在computed属性中处理
}

const backToList = () => {
  // 使用 replace 而不是 push，避免历史记录问题
  router.replace('/security-situation')
}

// 辅助函数
const getRiskLevelClass = (level?: string) => {
  const levelMap: {[key: string]: string} = {
    'CRITICAL': 'level-critical',
    'HIGH': 'level-high',
    'MEDIUM': 'level-medium',
    'LOW': 'level-low',
    'SAFE': 'level-safe'
  }
  return levelMap[level || ''] || 'level-unknown'
}

const getRiskLevelText = (level?: string) => {
  const levelMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'SAFE': '安全'
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

const getSeverityText = (severity?: string) => {
  const severityMap: {[key: string]: string} = {
    'CRITICAL': '危急',
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'INFO': '信息'
  }
  return severityMap[severity || ''] || '未知'
}

const getStatusClass = (status?: string) => {
  const statusMap: {[key: string]: string} = {
    'OPEN': 'status-open',
    'PATCHED': 'status-patched',
    'REMEDIATED': 'status-remediated',
    'ACTIVE': 'status-active',
    'INACTIVE': 'status-inactive',
    'MAINTENANCE': 'status-maintenance'
  }
  return `status-tag ${statusMap[status || ''] || 'status-unknown'}`
}

const getStatusText = (status?: string) => {
  const statusMap: {[key: string]: string} = {
    'OPEN': '未修复',
    'PATCHED': '已修补',
    'REMEDIATED': '已修复',
    'ACTIVE': '活跃',
    'INACTIVE': '非活跃',
    'MAINTENANCE': '维护中'
  }
  return statusMap[status || ''] || '未知'
}

const getAssetStatusText = (status?: string) => {
  const statusMap: {[key: string]: string} = {
    'ACTIVE': '活跃',
    'INACTIVE': '非活跃',
    'MAINTENANCE': '维护中'
  }
  return statusMap[status || ''] || '未知'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.asset-detail {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-card .overview-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.risk-summary {
  text-align: center;
  min-width: 150px;
}

.risk-score {
  font-size: 36px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 10px;
}

.risk-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.risk-level {
  font-size: 16px;
  font-weight: 500;
}

.asset-info {
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  color: #909399;
  margin-right: 10px;
  min-width: 80px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.detail-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  position: relative;
  height: 250px;
}

/* 态势等级颜色 */
.level-critical { color: #dc3545; }
.level-high { color: #fd7e14; }
.level-medium { color: #ffc107; }
.level-low { color: #28a745; }
.level-safe { color: #17a2b8; }
.level-unknown { color: #6c757d; }

/* 威胁严重程度标签 */
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

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
}

.status-open { background: #fde2e2; color: #dc3545; }
.status-patched { background: #e7f3ff; color: #1890ff; }
.status-remediated { background: #e6f7ee; color: #52c41a; }
.status-active { background: #e6f7ee; color: #52c41a; }
.status-inactive { background: #f5f5f5; color: #909399; }
.status-maintenance { background: #fff7e6; color: #fa8c16; }

/* 响应式布局 */
@media (max-width: 768px) {
  .overview-card .overview-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>