<template>
  <div class="threat-container">
    <!-- 威胁概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #f0f9ff;">
              <el-icon :size="40" color="#409eff"><Warning /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.totalThreats }}</div>
              <div class="overview-label">总威胁数</div>
              <div class="overview-trend">
                <el-icon color="#67c23a"><CaretBottom /></el-icon>
                <span style="color: #67c23a;">-12%</span>
              </div>
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
              <div class="overview-value">{{ overview.highRisk }}</div>
              <div class="overview-label">高危威胁</div>
              <div class="overview-trend">
                <el-icon color="#f56c6c"><CaretTop /></el-icon>
                <span style="color: #f56c6c;">+8%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #f0f9ff;">
              <div class="globe-icon">
                <el-icon :size="40" color="#409eff"><Location /></el-icon>
              </div>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.countries }}</div>
              <div class="overview-label">威胁来源国家</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" style="background: #fef5e7;">
              <el-icon :size="40" color="#e6a23c">
                <TrendCharts :class="{ 'pulse-animation': autoRefresh }" />
              </el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.updates }}</div>
              <div class="overview-label">今日更新</div>
              <div v-if="autoRefresh" class="overview-trend" style="color: #67c23a;">
                <el-icon><TrendCharts /></el-icon>
                <span>实时监控中</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 威胁情报列表 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>最新威胁情报</span>
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-tag v-if="lastUpdateTime" type="info" size="small">
                  最后更新: {{ lastUpdateTime }}
                </el-tag>
                <el-button 
                  :type="autoRefresh ? 'success' : 'info'" 
                  size="small" 
                  @click="toggleAutoRefresh"
                >
                  <el-icon><TrendCharts /></el-icon>
                  {{ autoRefresh ? '实时更新中' : '已暂停' }}
                </el-button>
                <el-button type="primary" size="small" @click="manualRefresh">
                  <el-icon><Download /></el-icon>
                  手动刷新
                </el-button>
                <el-button type="danger" size="small" @click="clearStorageData">
                  <el-icon><Delete /></el-icon>
                  清空数据
                </el-button>
                <el-radio-group v-model="threatFilter" size="small" @change="filterThreats">
                  <el-radio-button :value="'all'">全部</el-radio-button>
                  <el-radio-button :value="'high'">高危</el-radio-button>
                  <el-radio-button :value="'medium'">中危</el-radio-button>
                  <el-radio-button :value="'low'">低危</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          
          <div class="threat-list">
            <div 
              v-for="threat in filteredThreats" 
              :key="threat.id" 
              :class="['threat-item', { 'new-item': threat.isNew }]"
              @click="showThreatDetail(threat)"
            >
              <div class="threat-header">
                <div class="threat-title">
                  <el-tag 
                    :type="getLevelType(threat.level)" 
                    size="small"
                    effect="dark"
                  >
                    {{ threat.level }}
                  </el-tag>
                  <span class="threat-name">{{ threat.name }}</span>
                </div>
                <div class="threat-time">{{ threat.time }}</div>
              </div>
              <div class="threat-desc">{{ threat.description }}</div>
              <div class="threat-footer">
                <el-tag size="small" type="info">{{ threat.type }}</el-tag>
                <el-tag size="small" type="warning">{{ threat.source }}</el-tag>
                <span class="threat-ip">目标IP: {{ threat.targetIp }}</span>
              </div>
            </div>
          </div>
          
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="threats.length"
            layout="prev, pager, next"
            style="margin-top: 20px; text-align: center;"
          />
        </el-card>
      </el-col>

      <!-- 威胁地图和统计 -->
      <el-col :span="10">
        <el-card style="margin-bottom: 20px;">
          <template #header>
            <span>威胁来源分布</span>
          </template>
          <div ref="mapChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>

        <el-card>
          <template #header>
            <span>威胁类型统计</span>
          </template>
          <div ref="typeChartRef" style="width: 100%; height: 250px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 威胁趋势图 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>威胁趋势分析</span>
      </template>
      <div ref="trendChartRef" style="width: 100%; height: 350px;"></div>
    </el-card>

    <!-- IOC指标 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>IOC 威胁指标</span>
          <el-button type="primary" size="small" @click="exportIOC">
            <el-icon><Download /></el-icon>
            导出IOC
          </el-button>
        </div>
      </template>
      
      <el-tabs v-model="iocTab">
        <el-tab-pane label="恶意IP" name="ip">
          <el-table :data="iocData.ips" border stripe max-height="400">
            <el-table-column prop="ip" label="IP地址" width="150" />
            <el-table-column prop="country" label="国家/地区" width="120" />
            <el-table-column prop="type" label="威胁类型" width="120">
              <template #default="scope">
                <el-tag size="small" type="danger">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confidence" label="置信度" width="100">
              <template #default="scope">
                <el-progress :percentage="scope.row.confidence" :color="getConfidenceColor(scope.row.confidence)" />
              </template>
            </el-table-column>
            <el-table-column prop="firstSeen" label="首次发现" width="180" />
            <el-table-column prop="lastSeen" label="最后活跃" width="180" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" link @click="blockIP(scope.row)">
                  封禁
                </el-button>
                <el-button type="info" size="small" link @click="viewDetails(scope.row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="恶意域名" name="domain">
          <el-table :data="iocData.domains" border stripe max-height="400">
            <el-table-column prop="domain" label="域名" width="200" />
            <el-table-column prop="type" label="威胁类型" width="120">
              <template #default="scope">
                <el-tag size="small" type="danger">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confidence" label="置信度" width="100">
              <template #default="scope">
                <el-progress :percentage="scope.row.confidence" :color="getConfidenceColor(scope.row.confidence)" />
              </template>
            </el-table-column>
            <el-table-column prop="registrar" label="注册商" width="150" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" link @click="blockDomain(scope.row)">
                  拦截
                </el-button>
                <el-button type="info" size="small" link @click="viewDetails(scope.row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="文件哈希" name="hash">
          <el-table :data="iocData.hashes" border stripe max-height="400">
            <el-table-column prop="hash" label="MD5哈希" width="280" />
            <el-table-column prop="fileName" label="文件名" width="200" />
            <el-table-column prop="type" label="威胁类型" width="120">
              <template #default="scope">
                <el-tag size="small" type="danger">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="size" label="文件大小" width="120" />
            <el-table-column prop="confidence" label="置信度" width="100">
              <template #default="scope">
                <el-progress :percentage="scope.row.confidence" :color="getConfidenceColor(scope.row.confidence)" />
              </template>
            </el-table-column>
            <el-table-column prop="firstSeen" label="首次发现" width="180" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 威胁详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="威胁详情" width="800px">
      <div v-if="currentThreat" class="threat-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="威胁名称">
            {{ currentThreat.name }}
          </el-descriptions-item>
          <el-descriptions-item label="威胁等级">
            <el-tag :type="getLevelType(currentThreat.level)">{{ currentThreat.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="威胁类型">
            {{ currentThreat.type }}
          </el-descriptions-item>
          <el-descriptions-item label="情报来源">
            {{ currentThreat.source }}
          </el-descriptions-item>
          <el-descriptions-item label="目标IP">
            {{ currentThreat.targetIp }}
          </el-descriptions-item>
          <el-descriptions-item label="发现时间">
            {{ currentThreat.time }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentThreat.description }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>处置建议</el-divider>
        <div class="suggestions">
          <el-alert
            title="建议立即采取以下措施："
            type="warning"
            :closable="false"
            style="margin-bottom: 15px;"
          />
          <ul>
            <li>立即隔离受影响的系统和网络段</li>
            <li>检查并更新安全策略和防火墙规则</li>
            <li>扫描系统中是否存在相关恶意文件或进程</li>
            <li>监控网络流量，识别异常行为</li>
            <li>更新相关安全设备的威胁情报库</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleThreat">处置威胁</el-button>
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
  Location,
  TrendCharts,
  CaretTop,
  CaretBottom,
  Download,
  Delete
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { getThreatIntelligence } from '@/api/report'

// 威胁情报类型
interface ThreatItem {
  id: number
  name: string
  level: string
  type: string
  source: string
  targetIp: string
  description: string
  time: string
  isNew?: boolean
}

// 概览数据
const overview = reactive({
  totalThreats: 1247,
  highRisk: 89,
  countries: 42,
  updates: 156
})

// 自动刷新配置
const autoRefresh = ref(true)
const refreshInterval = ref(30) // 秒
let refreshTimer: number | null = null
const lastUpdateTime = ref('')

// 本地存储键
const STORAGE_KEYS = {
  THREATS: 'threat_intelligence_data',
  OVERVIEW: 'threat_overview_data',
  LAST_UPDATE: 'threat_last_update_time'
}

// 从本地存储加载数据
const loadFromStorage = () => {
  try {
    // 加载威胁列表
    const savedThreats = localStorage.getItem(STORAGE_KEYS.THREATS)
    if (savedThreats) {
      const parsedThreats = JSON.parse(savedThreats)
      if (Array.isArray(parsedThreats) && parsedThreats.length > 0) {
        threats.value = parsedThreats
        console.log('从本地存储加载威胁数据:', parsedThreats.length, '条')
      }
    }
    
    // 加载概览数据
    const savedOverview = localStorage.getItem(STORAGE_KEYS.OVERVIEW)
    if (savedOverview) {
      const parsedOverview = JSON.parse(savedOverview)
      Object.assign(overview, parsedOverview)
      console.log('从本地存储加载概览数据')
    }
    
    // 加载最后更新时间
    const savedTime = localStorage.getItem(STORAGE_KEYS.LAST_UPDATE)
    if (savedTime) {
      lastUpdateTime.value = savedTime
    }
  } catch (error) {
    console.error('加载本地数据失败:', error)
  }
}

// 保存到本地存储
const saveToStorage = () => {
  try {
    // 保存威胁列表（移除isNew属性）
    const threatsToSave = threats.value.map(({ isNew, ...threat }) => threat)
    localStorage.setItem(STORAGE_KEYS.THREATS, JSON.stringify(threatsToSave))
    
    // 保存概览数据
    localStorage.setItem(STORAGE_KEYS.OVERVIEW, JSON.stringify(overview))
    
    // 保存最后更新时间
    if (lastUpdateTime.value) {
      localStorage.setItem(STORAGE_KEYS.LAST_UPDATE, lastUpdateTime.value)
    }
    
    console.log('威胁数据已保存到本地存储')
  } catch (error) {
    console.error('保存本地数据失败:', error)
  }
}

// 加载威胁情报数据
const loadThreatIntelligence = async () => {
  try {
    const res = await getThreatIntelligence({ period: 'week' })
    if (res.data && res.data.code === 200) {
      console.log('威胁情报数据:', res.data)
      // 更新概览数据（如果后端返回）
      if (res.data.data) {
        // 这里可以根据后端返回更新overview数据
      }
      // 更新最后刷新时间
      lastUpdateTime.value = new Date().toLocaleTimeString()
      ElMessage.success('威胁情报已更新')
    }
  } catch (error) {
    console.error('加载威胁情报失败:', error)
  }
}

// 生成新的威胁情报（模拟实时更新）
const generateNewThreat = () => {
  const types = ['APT攻击', 'DDoS攻击', '勒索软件', '漏洞利用', '钓鱼攻击', '恶意软件', '僵尸网络', '挖矿木马']
  const levels = ['高危', '中危', '低危']
  const sources = ['国家威胁情报中心', 'Microsoft Security', 'CloudFlare', 'CISA', 'Kaspersky', 'Akamai']
  const ips = ['192.168.1.100', '10.0.0.50', '172.16.10.25', '203.0.113.45', '198.51.100.66']
  
  const randomType = types[Math.floor(Math.random() * types.length)]
  const randomLevel = levels[Math.floor(Math.random() * levels.length)]
  const randomSource = sources[Math.floor(Math.random() * sources.length)]
  const randomIp = ips[Math.floor(Math.random() * ips.length)]
  
  return {
    id: Date.now(),
    name: `新型${randomType}活动检测`,
    level: randomLevel,
    type: randomType,
    source: randomSource,
    targetIp: randomIp,
    description: `系统检测到新的${randomType}威胁活动，正在针对目标系统进行攻击尝试`,
    time: new Date().toLocaleString('zh-CN', { 
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }),
    isNew: true // 标记为新威胁
  }
}

// 执行刷新
const performRefresh = async () => {
  // 加载真实数据
  await loadThreatIntelligence()
  
  // 模拟新增威胁（30%概率）
  if (Math.random() < 0.3) {
    const newThreat = generateNewThreat()
    threats.value.unshift(newThreat)
    // 限制列表长度
    if (threats.value.length > 20) {
      threats.value.pop()
    }
    
    // 3秒后移除新威胁标记
    setTimeout(() => {
      const threat = threats.value.find(t => t.id === newThreat.id)
      if (threat) {
        threat.isNew = false
        // 保存到本地存储
        saveToStorage()
      }
    }, 3000)
    
    // 更新概览数据
    overview.totalThreats++
    overview.updates++
    if (newThreat.level === '高危') {
      overview.highRisk++
    }
    
    // 保存到本地存储
    saveToStorage()
    
    ElMessage.info({
      message: `检测到新威胁：${newThreat.name}`,
      type: 'warning',
      duration: 5000
    })
  }
}

// 切换自动刷新
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    startAutoRefresh()
    ElMessage.success(`已开启自动刷新，每 ${refreshInterval.value} 秒更新一次`)
  } else {
    stopAutoRefresh()
    ElMessage.info('已关闭自动刷新')
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh() // 清除旧的定时器
  refreshTimer = window.setInterval(() => {
    performRefresh()
  }, refreshInterval.value * 1000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer !== null) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 手动刷新
const manualRefresh = () => {
  performRefresh()
  ElMessage.success('正在刷新威胁情报...')
}

// 清空本地数据
const clearStorageData = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有本地存储的威胁数据吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    localStorage.removeItem(STORAGE_KEYS.THREATS)
    localStorage.removeItem(STORAGE_KEYS.OVERVIEW)
    localStorage.removeItem(STORAGE_KEYS.LAST_UPDATE)
    
    ElMessage.success('本地数据已清空')
    
    // 重新加载默认数据
    location.reload()
  } catch {
    // 用户取消
  }
}

// 威胁列表
const threats = ref<ThreatItem[]>([
  {
    id: 1,
    name: 'APT攻击组织新型钓鱼活动',
    level: '高危',
    type: 'APT攻击',
    source: '国家威胁情报中心',
    targetIp: '192.168.1.100',
    description: '发现针对金融机构的APT攻击，使用新型钓鱼邮件传播恶意软件，已确认多个受害组织',
    time: '2025-01-15 14:30'
  },
  {
    id: 2,
    name: '大规模DDoS攻击预警',
    level: '高危',
    type: 'DDoS攻击',
    source: 'CloudFlare',
    targetIp: '10.0.0.50',
    description: '监测到僵尸网络正在准备发起大规模DDoS攻击，目标为多个互联网服务提供商',
    time: '2025-01-15 13:20'
  },
  {
    id: 3,
    name: '勒索软件变种LockBit 4.0',
    level: '高危',
    type: '勒索软件',
    source: 'Microsoft Security',
    targetIp: '172.16.10.25',
    description: 'LockBit勒索软件发布新版本，增强了加密算法和反检测能力，已有企业受害',
    time: '2025-01-15 12:15'
  },
  {
    id: 4,
    name: 'Log4j漏洞新利用方式',
    level: '中危',
    type: '漏洞利用',
    source: 'CISA',
    targetIp: '192.168.2.88',
    description: '发现Log4j漏洞的新型利用方式，攻击者可绕过部分安全防护措施',
    time: '2025-01-15 11:00'
  },
  {
    id: 5,
    name: 'Mirai僵尸网络活跃',
    level: '中危',
    type: '僵尸网络',
    source: 'Akamai',
    targetIp: '203.0.113.45',
    description: 'Mirai僵尸网络活跃度上升，正在扫描并感染物联网设备',
    time: '2025-01-15 10:30'
  },
  {
    id: 6,
    name: '钓鱼网站仿冒银行',
    level: '中危',
    type: '钓鱼攻击',
    source: 'PhishTank',
    targetIp: '198.51.100.66',
    description: '发现多个仿冒知名银行的钓鱼网站，诱导用户输入账号密码',
    time: '2025-01-15 09:45'
  },
  {
    id: 7,
    name: 'Emotet恶意软件传播',
    level: '低危',
    type: '恶意软件',
    source: 'Proofpoint',
    targetIp: '10.10.10.30',
    description: 'Emotet通过垃圾邮件传播，主要目标为欧洲地区用户',
    time: '2025-01-15 09:00'
  },
  {
    id: 8,
    name: '加密货币挖矿木马',
    level: '低危',
    type: '挖矿木马',
    source: 'Kaspersky',
    targetIp: '172.20.0.100',
    description: '发现新型加密货币挖矿木马，通过软件捆绑方式传播',
    time: '2025-01-15 08:30'
  }
])

// IOC数据
const iocData = reactive({
  ips: [
    {
      ip: '185.220.101.45',
      country: '俄罗斯',
      type: 'C&C服务器',
      confidence: 95,
      firstSeen: '2024-01-10 08:30',
      lastSeen: '2025-01-15 14:20',
      description: 'APT组织使用的命令控制服务器'
    },
    {
      ip: '103.224.182.251',
      country: '朝鲜',
      type: '钓鱼服务器',
      confidence: 88,
      firstSeen: '2024-01-12 10:15',
      lastSeen: '2025-01-15 12:00',
      description: '托管钓鱼网站的服务器'
    },
    {
      ip: '45.142.212.61',
      country: '乌克兰',
      type: 'DDoS肉鸡',
      confidence: 76,
      firstSeen: '2024-01-08 15:20',
      lastSeen: '2025-01-15 09:30',
      description: '参与DDoS攻击的僵尸主机'
    },
    {
      ip: '194.67.93.18',
      country: '荷兰',
      type: '恶意软件分发',
      confidence: 92,
      firstSeen: '2024-01-11 12:00',
      lastSeen: '2025-01-15 11:45',
      description: '分发勒索软件的服务器'
    }
  ],
  domains: [
    {
      domain: 'secure-bank-login.xyz',
      type: '钓鱼域名',
      confidence: 98,
      registrar: 'Namecheap',
      createTime: '2024-01-10',
      description: '仿冒银行的钓鱼网站'
    },
    {
      domain: 'update-security.online',
      type: '恶意软件',
      confidence: 85,
      registrar: 'GoDaddy',
      createTime: '2024-01-08',
      description: '分发恶意软件的域名'
    },
    {
      domain: 'crypto-wallet-verify.com',
      type: '钓鱼域名',
      confidence: 91,
      registrar: 'Cloudflare',
      createTime: '2024-01-12',
      description: '仿冒加密货币钱包'
    }
  ],
  hashes: [
    {
      hash: '5d41402abc4b2a76b9719d911017c592',
      fileName: 'invoice.pdf.exe',
      type: '勒索软件',
      size: '2.3 MB',
      confidence: 97,
      firstSeen: '2024-01-13 10:00',
      description: 'LockBit勒索软件样本'
    },
    {
      hash: '098f6bcd4621d373cade4e832627b4f6',
      fileName: 'update.msi',
      type: '木马程序',
      size: '5.1 MB',
      confidence: 89,
      firstSeen: '2024-01-11 14:30',
      description: '远程控制木马'
    },
    {
      hash: '5f4dcc3b5aa765d61d8327deb882cf99',
      fileName: 'system32.dll',
      type: '挖矿木马',
      size: '1.8 MB',
      confidence: 82,
      firstSeen: '2024-01-09 16:20',
      description: '加密货币挖矿程序'
    }
  ]
})

const threatFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(5)
const iocTab = ref('ip')
const detailDialogVisible = ref(false)
const currentThreat = ref<any>(null)

// 图表引用
const mapChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

let mapChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

// 过滤威胁列表
const filteredThreats = computed(() => {
  let filtered = threats.value
  if (threatFilter.value !== 'all') {
    const levelMap: Record<string, string> = {
      high: '高危',
      medium: '中危',
      low: '低危'
    }
    filtered = threats.value.filter(t => t.level === levelMap[threatFilter.value])
  }
  const start = (currentPage.value - 1) * pageSize.value
  return filtered.slice(start, start + pageSize.value)
})

// 获取等级类型
const getLevelType = (level: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const map: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '高危': 'danger',
    '中危': 'warning',
    '低危': 'success'
  }
  return map[level] || 'info'
}

// 获取置信度颜色
const getConfidenceColor = (confidence: number) => {
  if (confidence >= 90) return '#67c23a'
  if (confidence >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 过滤威胁
const filterThreats = () => {
  currentPage.value = 1
}

// 显示威胁详情
const showThreatDetail = (threat: any) => {
  currentThreat.value = threat
  detailDialogVisible.value = true
}

// 处置威胁
const handleThreat = () => {
  ElMessage.success('已添加到处置队列')
  detailDialogVisible.value = false
}

// 封禁IP
const blockIP = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要封禁IP ${row.ip} 吗？`, '提示', {
      type: 'warning'
    })
    ElMessage.success('IP已添加到黑名单')
  } catch {
    // 用户取消
  }
}

// 拦截域名
const blockDomain = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要拦截域名 ${row.domain} 吗？`, '提示', {
      type: 'warning'
    })
    ElMessage.success('域名已添加到拦截列表')
  } catch {
    // 用户取消
  }
}

// 查看详情
const viewDetails = (row: any) => {
  ElMessage.info('详情功能开发中')
}

// 导出IOC
const exportIOC = () => {
  try {
    // 获取当前选中的IOC数据
    let dataToExport = [];
    let fileName = 'ioc_data';
    
    switch (iocTab.value) {
      case 'ip':
        dataToExport = iocData.ips;
        fileName = 'ioc_ips.csv';
        break;
      case 'domain':
        dataToExport = iocData.domains;
        fileName = 'ioc_domains.csv';
        break;
      case 'hash':
        dataToExport = iocData.hashes;
        fileName = 'ioc_hashes.csv';
        break;
      default:
        dataToExport = [...iocData.ips, ...iocData.domains, ...iocData.hashes];
        fileName = 'ioc_all.csv';
    }
    
    // 转换为CSV格式
    const csvContent = convertToCSV(dataToExport);
    
    // 创建并下载文件
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('IOC数据导出成功');
  } catch (error) {
    console.error('导出IOC数据失败:', error);
    ElMessage.error('IOC数据导出失败: ' + (error instanceof Error ? error.message : '未知错误'));
  }
}

// 将数据转换为CSV格式
const convertToCSV = (data: any[]) => {
  if (!data || data.length === 0) {
    return '';
  }
  
  // 获取所有字段名
  const headers = Object.keys(data[0]);
  
  // 创建CSV头部
  const csvHeader = headers.join(',') + '\n';
  
  // 创建CSV内容
  const csvBody = data.map(row => {
    return headers.map(fieldName => {
      let fieldValue = row[fieldName];
      // 如果字段值包含逗号或引号，需要用引号包围并转义引号
      if (typeof fieldValue === 'string' && (fieldValue.includes(',') || fieldValue.includes('"'))) {
        fieldValue = '"' + fieldValue.replace(/"/g, '""') + '"';
      }
      return fieldValue;
    }).join(',');
  }).join('\n');
  
  return csvHeader + csvBody;
}

// 初始化图表
const initCharts = () => {
  if (mapChartRef.value) {
    mapChart = echarts.init(mapChartRef.value)
  }
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
  }
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  updateCharts()
  
  window.addEventListener('resize', () => {
    mapChart?.resize()
    typeChart?.resize()
    trendChart?.resize()
  })
}

// 更新图表
const updateCharts = () => {
  // 威胁来源地图
  const geoData = [
    { name: '美国', value: 234 },
    { name: '中国', value: 189 },
    { name: '俄罗斯', value: 167 },
    { name: '朝鲜', value: 98 },
    { name: '伊朗', value: 76 },
    { name: '英国', value: 65 },
    { name: '德国', value: 54 },
    { name: '法国', value: 43 }
  ]
  
  const mapOption: EChartsOption = {
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        data: geoData
      }
    ]
  }
  mapChart?.setOption(mapOption)
  
  // 威胁类型统计
  const typeOption: EChartsOption = {
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: [
          { value: 156, name: 'APT攻击' },
          { value: 234, name: 'DDoS攻击' },
          { value: 189, name: '勒索软件' },
          { value: 98, name: '钓鱼攻击' },
          { value: 145, name: '恶意软件' },
          { value: 67, name: '漏洞利用' }
        ],
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
  
  // 威胁趋势
  const dates = []
  const highData = []
  const mediumData = []
  const lowData = []
  
  for (let i = 13; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString())
    highData.push(Math.floor(Math.random() * 30) + 20)
    mediumData.push(Math.floor(Math.random() * 50) + 30)
    lowData.push(Math.floor(Math.random() * 40) + 20)
  }
  
  const trendOption: EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['高危', '中危', '低危']
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '威胁数量'
    },
    series: [
      {
        name: '高危',
        type: 'line',
        smooth: true,
        data: highData,
        itemStyle: { color: '#f56c6c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
          ])
        }
      },
      {
        name: '中危',
        type: 'line',
        smooth: true,
        data: mediumData,
        itemStyle: { color: '#e6a23c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.1)' }
          ])
        }
      },
      {
        name: '低危',
        type: 'line',
        smooth: true,
        data: lowData,
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      }
    ]
  }
  trendChart?.setOption(trendOption)
}

onMounted(() => {
  // 从本地存储加载数据
  loadFromStorage()
  
  initCharts()
  loadThreatIntelligence()
  // 启动自动刷新
  if (autoRefresh.value) {
    startAutoRefresh()
  }
})

onUnmounted(() => {
  // 清理图表
  mapChart?.dispose()
  typeChart?.dispose()
  trendChart?.dispose()
  // 停止自动刷新
  stopAutoRefresh()
})
</script>

<style scoped>
.threat-container {
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

.overview-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 13px;
}

.threat-list {
  max-height: 600px;
  overflow-y: auto;
}

.threat-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.threat-item.new-item {
  animation: flash 2s;
  border-color: #409eff;
  background: #f0f9ff;
}

@keyframes flash {
  0%, 100% {
    background: #ffffff;
  }
  50% {
    background: #f0f9ff;
  }
}

.threat-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.threat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.threat-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.threat-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.threat-time {
  font-size: 12px;
  color: #909399;
}

.threat-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.6;
}

.threat-footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.threat-ip {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.threat-detail {
  padding: 10px 0;
}

.suggestions {
  padding: 10px;
}

.suggestions ul {
  list-style: none;
  padding-left: 0;
}

.suggestions li {
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
}

.suggestions li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #e6a23c;
  font-weight: bold;
}

.globe-icon {
  animation: pulse 2s infinite;
}

.pulse-animation {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.05);
  }
}

:deep(.el-progress__text) {
  font-size: 12px !important;
}
</style>
