<template>
  <div class="threat-intelligence-detail">
    <div class="detail-header">
      <h2>威胁情报详情</h2>
      <el-button type="primary" @click="backToList">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div class="detail-content">
      <!-- 情报概览卡片 -->
      <el-card class="overview-card">
        <template #header>
          <div class="card-header">
            <span>情报概览</span>
            <el-tag :type="getSeverityType(threatData.severity)" size="default">
              {{ getSeverityText(threatData.severity) }}
            </el-tag>
          </div>
        </template>
        <div class="overview-content">
          <div class="ioc-display">
            <div class="ioc-value">{{ threatData.iocValue }}</div>
            <div class="ioc-type" :class="`ioc-type-${threatData.iocType?.toLowerCase()}`">
              {{ getIocTypeText(threatData.iocType) }}
            </div>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">情报ID:</span>
              <span class="info-value">{{ threatData.threatId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">威胁类型:</span>
              <span class="info-value">{{ threatData.threatType || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">置信度:</span>
              <span class="info-value">{{ (threatData.confidenceScore || 0) * 100 }}%</span>
            </div>
            <div class="info-item">
              <span class="info-label">首次发现时间:</span>
              <span class="info-value">{{ formatDate(threatData.firstSeen) || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后更新时间:</span>
              <span class="info-value">{{ formatDate(threatData.lastUpdated) || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">来源:</span>
              <span class="info-value">{{ threatData.source || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">情报提供者:</span>
              <span class="info-value">{{ threatData.provider || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">标签:</span>
              <div class="tags">
                <el-tag v-for="tag in threatData.tags" :key="tag" size="small" effect="plain">
                  {{ tag }}
                </el-tag>
                <span v-if="!threatData.tags || threatData.tags.length === 0">-</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <div class="detail-grid">
        <!-- 情报详情 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>情报详情</span>
            </div>
          </template>
          <div class="description">
            {{ threatData.description || '暂无描述信息' }}
          </div>
          
          <div v-if="threatData.additionalInfo" class="additional-info">
            <h4>附加信息</h4>
            <div class="info-list">
              <div v-for="(value, key) in threatData.additionalInfo" :key="key" class="info-entry">
                <span class="entry-key">{{ formatKey(key) }}:</span>
                <span class="entry-value">{{ formatValue(value) }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 影响范围 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>影响范围</span>
            </div>
          </template>
          <div class="impact-info">
            <div class="impact-stat">
              <div class="stat-number">{{ affectedAssets.length }}</div>
              <div class="stat-label">影响资产数</div>
            </div>
            <div class="impact-stat">
              <div class="stat-number">{{ affectedEvents.length }}</div>
              <div class="stat-label">相关事件数</div>
            </div>
          </div>
          
          <div class="affected-assets">
            <h4>受影响资产</h4>
            <el-table v-if="affectedAssets.length > 0" :data="affectedAssets" stripe size="small">
              <el-table-column prop="assetName" label="资产名称" />
              <el-table-column prop="assetType" label="资产类型" width="100" />
              <el-table-column prop="ipAddress" label="IP地址" width="120" />
              <el-table-column prop="riskScore" label="风险评分" width="100" />
            </el-table>
            <div v-else class="empty-message">暂无受影响资产信息</div>
          </div>
        </el-card>
      </div>

      <div class="detail-grid">
        <!-- 时间线 -->
        <el-card class="detail-card full-width">
          <template #header>
            <div class="card-header">
              <span>活动时间线</span>
            </div>
          </template>
          <div class="timeline">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in activities"
                :key="index"
                :timestamp="formatDate(activity.timestamp)"
                :type="getActivityType(activity.type)"
                :color="getActivityColor(activity.type)"
              >
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-description">{{ activity.description }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>

      <div class="detail-grid">
        <!-- 相关情报 -->
        <el-card class="detail-card full-width">
          <template #header>
            <div class="card-header">
              <span>相关情报</span>
            </div>
          </template>
          <el-table v-if="relatedThreats.length > 0" :data="relatedThreats" stripe size="small">
            <el-table-column prop="iocValue" label="IOC值" />
            <el-table-column prop="iocType" label="IOC类型" width="100">
              <template #default="scope">
                {{ getIocTypeText(scope.row.iocType) }}
              </template>
            </el-table-column>
            <el-table-column prop="threatType" label="威胁类型" width="120" />
            <el-table-column prop="severity" label="严重程度" width="100">
              <template #default="scope">
                <el-tag :type="getSeverityType(scope.row.severity)" size="small">
                  {{ getSeverityText(scope.row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confidenceScore" label="置信度" width="100">
              <template #default="scope">
                {{ (scope.row.confidenceScore || 0) * 100 }}%
              </template>
            </el-table-column>
            <el-table-column prop="lastUpdated" label="更新时间" width="150">
              <template #default="scope">
                {{ formatDate(scope.row.lastUpdated) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button link size="small" @click="viewRelatedThreat(scope.row.threatId)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-else class="empty-message">暂无相关情报</div>
        </el-card>
      </div>

      <div class="detail-grid">
        <!-- 处置建议 -->
        <el-card class="detail-card full-width">
          <template #header>
            <div class="card-header">
              <span>处置建议</span>
            </div>
          </template>
          <div class="recommendations">
            <div v-for="(recommendation, index) in threatData.recommendations" :key="index" class="recommendation-item">
              <div class="recommendation-number">{{ index + 1 }}</div>
              <div class="recommendation-content">
                <div class="recommendation-title">{{ recommendation.title }}</div>
                <div class="recommendation-detail">{{ recommendation.detail }}</div>
              </div>
            </div>
            <div v-if="!threatData.recommendations || threatData.recommendations.length === 0" class="empty-message">
              暂无处置建议
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getThreatIntelligenceDetail } from '@/api/security-situation'
import type { ThreatIntelligence, Activity, RelatedThreat, AssetRisk } from '@/types/security-situation'

const router = useRouter()
const route = useRoute()

// 响应式数据
const threatData = ref<ThreatIntelligence>({} as ThreatIntelligence)
const affectedAssets = ref<AssetRisk[]>([])
const affectedEvents = ref<any[]>([])
const activities = ref<Activity[]>([])
const relatedThreats = ref<RelatedThreat[]>([])

// 生命周期钩子
onMounted(() => {
  // 只有在详情页路由时才加载数据
  if (route.path.includes('/security-situation/threat/')) {
    loadThreatData()
  }
})

// 监听路由参数变化 - 只在当前路由是详情页时才加载数据
watch(() => route.params.threatId, (newVal, oldVal) => {
  // 只有当路由路径包含 threat 且参数发生变化时才加载
  if (route.path.includes('/security-situation/threat/') && newVal && newVal !== oldVal) {
    loadThreatData()
  }
})

// 方法
const loadThreatData = async () => {
  // 检查当前路由是否是详情页
  if (!route.path.includes('/security-situation/threat/')) {
    console.log('当前不是详情页路由，跳过数据加载')
    return
  }

  // 获取路由参数，支持多种参数传递方式
  let threatId = route.params.threatId as string

  // 如果 params 中没有，尝试从 query 中获取
  if (!threatId) {
    threatId = route.query.threatId as string
  }

  // 如果仍然没有，尝试从 URL 路径中解析
  if (!threatId) {
    const pathMatch = route.path.match(/\/threat\/([^\/]+)$/)
    if (pathMatch) {
      threatId = pathMatch[1]
    }
  }

  // 调试信息
  if (import.meta.env.DEV) {
    console.log('加载威胁情报详情，threatId:', threatId)
    console.log('路由参数:', route.params)
    console.log('查询参数:', route.query)
    console.log('完整路由:', route)
  }

  if (!threatId) {
    console.warn('威胁情报ID为空，跳过数据加载')
    // 不显示错误消息，因为可能是正常的页面跳转
    return
  }

  try {
    // 调用实际的API获取威胁情报详情
    const response = await getThreatIntelligenceDetail(threatId)

    if (response && response.data && Object.keys(response.data).length > 0) {
      // 成功获取到数据库数据
      threatData.value = response.data

      // 加载影响资产数据
      affectedAssets.value = response.data.affectedAssets || []

      // 加载影响事件数据
      affectedEvents.value = response.data.affectedEvents || []

      // 加载活动时间线数据
      activities.value = response.data.activities || []

      // 加载相关情报数据
      relatedThreats.value = response.data.relatedThreats || []

      if (import.meta.env.DEV) {
        console.log('威胁情报详情加载成功')
      }
    } else {
      // 数据库中没有数据，使用模拟数据作为示例
      if (import.meta.env.DEV) {
        console.warn('数据库未找到威胁情报，使用模拟数据')
      }
      ElMessage.warning('未找到威胁情报数据，显示示例数据')
      const mockData = generateMockThreatDetail(threatId, 1)
      threatData.value = mockData
      affectedAssets.value = mockData.affectedAssets || []
      affectedEvents.value = mockData.affectedEvents || []
      activities.value = mockData.activities || []
      relatedThreats.value = mockData.relatedThreats || []
    }
  } catch (error) {
    console.error('加载威胁情报数据失败:', error)
    ElMessage.error('加载失败: ' + (error as Error).message)

    // API调用失败，生成模拟数据避免页面空白
    try {
      const seed = parseInt(threatId.replace(/\D/g, '0')) || 1
      const mockData = generateMockThreatDetail(threatId, seed)
      threatData.value = mockData
      affectedAssets.value = mockData.affectedAssets || []
      affectedEvents.value = mockData.affectedEvents || []
      activities.value = mockData.activities || []
      relatedThreats.value = mockData.relatedThreats || []
      if (import.meta.env.DEV) {
        console.log('已使用模拟数据填充')
      }
      ElMessage.info('已显示示例数据')
    } catch (mockError) {
      console.error('生成模拟数据也失败:', mockError)
      ElMessage.error('页面加载失败，请稍后重试')
    }
  }
}

const backToList = () => {
  // 使用 replace 而不是 push，避免历史记录问题
  // 直接跳转到威胁情报列表页
  router.replace('/security-situation')
}

const viewRelatedThreat = (threatId: string) => {
  router.push(`/security-situation/threat/${threatId}`)
}

// 辅助函数
const getSeverityType = (severity?: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
  const severityMap: {[key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger'} = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'warning',
    'LOW': 'info',
    'INFO': 'info'
  }
  return severityMap[severity || ''] || 'info'
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

const getIocTypeText = (iocType?: string) => {
  const iocTypeMap: {[key: string]: string} = {
    'IP': 'IP地址',
    'DOMAIN': '域名',
    'URL': 'URL',
    'FILE_HASH': '文件哈希',
    'EMAIL': '邮箱',
    'HASH_MD5': 'MD5哈希',
    'HASH_SHA1': 'SHA1哈希',
    'HASH_SHA256': 'SHA256哈希'
  }
  return iocTypeMap[iocType || ''] || iocType || '未知'
}

const getActivityType = (type?: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
  const typeMap: {[key: string]: 'primary' | 'success' | 'warning' | 'info' | 'danger'} = {
    'creation': 'primary',
    'detection': 'danger',
    'update': 'warning',
    'response': 'success',
    'review': 'info'
  }
  return typeMap[type || ''] || 'info'
}

const getActivityColor = (type?: string) => {
  const colorMap: {[key: string]: string} = {
    'creation': '#1890ff',
    'detection': '#ff4d4f',
    'update': '#faad14',
    'response': '#52c41a',
    'review': '#13c2c2'
  }
  return colorMap[type || ''] || '#13c2c2'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const formatKey = (key: string) => {
  return key.split(/(?=[A-Z])/).join(' ').replace(/^\w/, c => c.toUpperCase())
}

// 生成模拟威胁情报详情
const generateMockThreatDetail = (threatId: string, seed: number) => {
  const iocTypes = ['IP', 'DOMAIN', 'URL', 'FILE_HASH', 'EMAIL']
  const threatTypes = ['BruteForce', 'Malware', 'DDoS', 'SQLInjection', 'XSSAttack', 'Phishing']
  const severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']

  const iocType = iocTypes[seed % iocTypes.length]
  const threatType = threatTypes[seed % threatTypes.length]
  const severity = severities[seed % severities.length]

  let iocValue = ''
  switch (iocType) {
    case 'IP':
      iocValue = `192.168.${(seed * 3) % 256}.${(seed * 5) % 256}`
      break
    case 'DOMAIN':
      iocValue = `malicious-${seed}.example.com`
      break
    case 'URL':
      iocValue = `http://malicious-site-${seed}.com/payload`
      break
    case 'FILE_HASH':
      iocValue = `a1b2c3d4e5f6${String(seed).padStart(4, '0')}`
      break
    case 'EMAIL':
      iocValue = `threat${seed}@malicious.com`
      break
  }

  return {
    threatId: threatId,
    iocValue: iocValue,
    iocType: iocType,
    threatType: threatType,
    severity: severity,
    confidenceScore: 70 + (seed % 30),
    source: '内部IDS',
    provider: '安全运营中心',
    description: `检测到${threatType}类型的威胁活动`,
    firstSeen: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
    lastUpdated: new Date().toISOString(),
    discoveredTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
    status: 'ACTIVE',
    tags: [threatType, iocType],
    affectedSystems: [],
    relatedCves: [],
    additionalInfo: {
      country: 'Unknown',
      asn: 'AS12345',
      attackPattern: 'T1110',
      toolsUsed: threatType,
      historicalActivities: 10
    },
    recommendations: [
      {
        title: '立即阻断威胁',
        detail: '在防火墙和IPS设备上配置阻断规则'
      },
      {
        title: '加强监控',
        detail: '加强对相关系统的安全监控'
      },
      {
        title: '通知安全团队',
        detail: '立即通知安全运营团队进行响应'
      }
    ],
    affectedAssets: [
      {
        assetId: `ASSET-${String(seed).padStart(3, '0')}`,
        assetName: 'Web服务器-01',
        assetType: 'SERVER',
        riskScore: 75.2,
        riskLevel: 'HIGH',
        vulnerabilityCount: 5,
        threatCount: 3,
        assetValueScore: 80,
        vulnerabilityScore: 70,
        threatScore: 65,
        status: 'ACTIVE'
      }
    ],
    affectedEvents: [
      { id: 'EVENT-001', type: '检测告警', count: 50 + seed },
      { id: 'EVENT-002', type: '访问尝试', count: 20 + seed }
    ],
    activities: [
      {
        timestamp: new Date().toISOString(),
        type: 'update',
        title: '情报更新',
        description: '威胁情报信息已更新'
      },
      {
        timestamp: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
        type: 'detection',
        title: '新检测',
        description: '检测到新的威胁活动'
      },
      {
        timestamp: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        type: 'creation',
        title: '情报创建',
        description: '首次发现该威胁'
      }
    ],
    relatedThreats: [
      {
        threatId: `THREAT-${String(seed + 1).padStart(4, '0')}`,
        iocValue: `192.168.${((seed + 1) * 3) % 256}.${((seed + 1) * 5) % 256}`,
        iocType: 'IP',
        threatType: 'BruteForce',
        severity: 'HIGH',
        confidenceScore: 75,
        discoveredTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
        description: '相关威胁情报'
      }
    ]
  }
}

const formatValue = (value: any) => {
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'object' && value !== null) {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}
</script>

<style scoped>
.threat-intelligence-detail {
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

.ioc-display {
  text-align: center;
  min-width: 200px;
}

.ioc-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
  word-break: break-all;
}

.ioc-type {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.ioc-type-ip { background: #e6f7ff; color: #1890ff; }
.ioc-type-domain { background: #f6ffed; color: #52c41a; }
.ioc-type-url { background: #fff7e6; color: #fa8c16; }
.ioc-type-file_hash, .ioc-type-hash_md5, .ioc-type-hash_sha1, .ioc-type-hash_sha256 { background: #f9f0ff; color: #722ed1; }
.ioc-type-email { background: #fff1f0; color: #ff4d4f; }

.info-grid {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  color: #909399;
  margin-right: 10px;
  min-width: 100px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
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

.description {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  padding: 10px 0;
}

.additional-info h4,
.affected-assets h4 {
  margin: 20px 0 10px 0;
  color: #303133;
  font-size: 16px;
}

.info-list {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.info-entry {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  padding: 10px 15px;
}

.info-entry:last-child {
  border-bottom: none;
}

.entry-key {
  color: #909399;
  margin-right: 10px;
  min-width: 120px;
}

.entry-value {
  color: #303133;
  flex: 1;
  word-break: break-all;
}

.impact-info {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.impact-stat {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.activity-content {
  padding: 5px 0;
}

.activity-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.activity-description {
  font-size: 14px;
  color: #606266;
}

.recommendation-item {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.recommendation-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.recommendation-number {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  background: #1890ff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.recommendation-content {
  flex: 1;
}

.recommendation-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.recommendation-detail {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.empty-message {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .overview-card .overview-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .info-label {
    margin-bottom: 5px;
  }
  
  .impact-info {
    flex-direction: column;
    gap: 15px;
  }
}
</style>