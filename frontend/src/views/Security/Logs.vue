<template>
  <div class="logs-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实时日志</span>
          <div class="header-actions">
            <el-button
              :type="isStreaming ? 'danger' : 'success'"
              size="small"
              @click="toggleStreaming"
            >
              <el-icon><component :is="isStreaming ? VideoPause : VideoPlay" /></el-icon>
              {{ isStreaming ? '暂停' : '开始' }}
            </el-button>
            <el-button type="warning" size="small" @click="clearLogs">
              <el-icon><component :is="Delete" /></el-icon>
              清空
            </el-button>
            <el-button type="primary" size="small" @click="exportLogs">
              <el-icon><component :is="Download" /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="日志级别">
          <el-select v-model="filterLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="DEBUG" value="DEBUG">
              <el-tag type="info" size="small">DEBUG</el-tag>
            </el-option>
            <el-option label="INFO" value="INFO">
              <el-tag type="success" size="small">INFO</el-tag>
            </el-option>
            <el-option label="WARN" value="WARN">
              <el-tag type="warning" size="small">WARN</el-tag>
            </el-option>
            <el-option label="ERROR" value="ERROR">
              <el-tag type="danger" size="small">ERROR</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="来源IP">
          <el-input
            v-model="filterSource"
            placeholder="输入IP地址"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="filterKeyword"
            placeholder="搜索日志内容"
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><component :is="Search" /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志统计 -->
      <div class="log-stats">
        <el-space :size="20">
          <span>总计: <strong>{{ filteredLogs.length }}</strong> 条</span>
          <span>
            <el-tag type="info" size="small">DEBUG: {{ countByLevel('DEBUG') }}</el-tag>
          </span>
          <span>
            <el-tag type="success" size="small">INFO: {{ countByLevel('INFO') }}</el-tag>
          </span>
          <span>
            <el-tag type="warning" size="small">WARN: {{ countByLevel('WARN') }}</el-tag>
          </span>
          <span>
            <el-tag type="danger" size="small">ERROR: {{ countByLevel('ERROR') }}</el-tag>
          </span>
        </el-space>
      </div>

      <!-- 日志列表 -->
      <div ref="logContainer" class="log-list" @scroll="handleScroll">
        <el-scrollbar height="600px">
          <div
            v-for="log in filteredLogs"
            :key="log.id"
            :class="['log-item', `log-${log.level.toLowerCase()}`]"
          >
            <div class="log-header">
              <el-tag :type="getLevelType(log.level)" size="small">{{ log.level }}</el-tag>
              <span class="log-time">{{ log.timestamp }}</span>
              <span class="log-source">{{ log.source }}</span>
            </div>
            <div class="log-message" v-html="highlightKeyword(log.message)"></div>
            <div v-if="log.details" class="log-details">
              <el-collapse>
                <el-collapse-item title="详细信息" name="1">
                  <pre>{{ log.details }}</pre>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
          
          <div v-if="filteredLogs.length === 0" class="empty-logs">
            <el-empty description="暂无日志数据" />
          </div>
        </el-scrollbar>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogStream } from '@/api/security'
import { VideoPlay, VideoPause, Delete, Download, Search } from '@element-plus/icons-vue'

interface LogEntry {
  id: number
  timestamp: string
  level: string
  message: string
  source: string
  details?: string
}

const logs = ref<LogEntry[]>([])
const isStreaming = ref(true)
const autoScroll = ref(true)
const logContainer = ref<HTMLElement>()

// 筛选条件
const filterLevel = ref('')
const filterSource = ref('')
const filterKeyword = ref('')
const appliedFilters = ref({
  level: '',
  source: '',
  keyword: ''
})

// 过滤后的日志
const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    if (appliedFilters.value.level && log.level !== appliedFilters.value.level) {
      return false
    }
    if (appliedFilters.value.source && !log.source.includes(appliedFilters.value.source)) {
      return false
    }
    if (appliedFilters.value.keyword && !log.message.toLowerCase().includes(appliedFilters.value.keyword.toLowerCase())) {
      return false
    }
    return true
  })
})

// 按级别统计
const countByLevel = (level: string) => {
  return filteredLogs.value.filter(log => log.level === level).length
}

// 加载日志
const loadLogs = async () => {
  try {
    const res = await getLogStream()
    const newLogs = res.data.map((item: any, index: number) => ({
      id: Date.now() + index,
      timestamp: item.timestamp,
      level: item.level,
      message: item.message,
      source: item.source,
      details: item.details
    }))
    
    // 添加新日志到列表顶部
    logs.value = [...newLogs, ...logs.value]
    
    // 限制日志数量，最多保留1000条
    if (logs.value.length > 1000) {
      logs.value = logs.value.slice(0, 1000)
    }
    
    // 自动滚动到顶部
    if (autoScroll.value) {
      scrollToTop()
    }
  } catch (error) {
    console.error('加载日志失败:', error)
  }
}

// 生成模拟日志
const generateMockLog = () => {
  const levels = ['DEBUG', 'INFO', 'WARN', 'ERROR']
  const sources = ['192.168.1.100', '10.0.0.23', '172.16.5.88', '192.168.2.45', '203.0.113.42']
  const messages = [
    '用户登录成功',
    '检测到可疑的SQL注入尝试',
    '防火墙规则已更新',
    '系统健康检查完成',
    '发现异常流量模式',
    'API请求超时',
    '数据库连接建立',
    '缓存刷新完成',
    'SSL证书即将过期',
    '定时任务执行成功',
    '文件上传完成',
    '权限验证失败',
    '会话已过期',
    '网络连接异常',
    '内存使用率过高'
  ]
  
  const level = levels[Math.floor(Math.random() * levels.length)]
  const source = sources[Math.floor(Math.random() * sources.length)]
  const message = messages[Math.floor(Math.random() * messages.length)]
  
  const newLog: LogEntry = {
    id: Date.now() + Math.random(),
    timestamp: new Date().toLocaleString('zh-CN', { 
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }),
    level,
    message,
    source,
    details: level === 'ERROR' ? `Error Stack Trace:\n  at Function.executeQuery\n  at Connection.connect\n  at Database.init` : undefined
  }
  
  logs.value.unshift(newLog)
  
  // 限制日志数量
  if (logs.value.length > 1000) {
    logs.value.pop()
  }
  
  // 自动滚动
  if (autoScroll.value) {
    scrollToTop()
  }
}

// 切换日志流
const toggleStreaming = () => {
  isStreaming.value = !isStreaming.value
  if (isStreaming.value) {
    ElMessage.success('日志流已开启')
    startStreaming()
  } else {
    ElMessage.warning('日志流已暂停')
    stopStreaming()
  }
}

// 清空日志
const clearLogs = () => {
  logs.value = []
  ElMessage.success('日志已清空')
}

// 导出日志
const exportLogs = () => {
  const content = filteredLogs.value.map(log => 
    `[${log.timestamp}] [${log.level}] [${log.source}] ${log.message}`
  ).join('\n')
  
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `security-logs-${new Date().getTime()}.txt`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('日志导出成功')
}

// 应用筛选
const applyFilter = () => {
  appliedFilters.value = {
    level: filterLevel.value,
    source: filterSource.value,
    keyword: filterKeyword.value
  }
}

// 重置筛选
const resetFilter = () => {
  filterLevel.value = ''
  filterSource.value = ''
  filterKeyword.value = ''
  appliedFilters.value = {
    level: '',
    source: '',
    keyword: ''
  }
}

// 获取日志级别标签类型
const getLevelType = (level: string) => {
  const types: Record<string, any> = {
    'DEBUG': 'info',
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger'
  }
  return types[level] || 'info'
}

// 高亮关键词
const highlightKeyword = (text: string) => {
  if (!appliedFilters.value.keyword) return text
  const keyword = appliedFilters.value.keyword
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

// 滚动到顶部
const scrollToTop = () => {
  setTimeout(() => {
    const container = logContainer.value?.querySelector('.el-scrollbar__wrap')
    if (container) {
      container.scrollTop = 0
    }
  }, 100)
}

// 处理滚动事件
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  // 如果用户手动滚动了，暂时关闭自动滚动
  if (target.scrollTop > 50) {
    autoScroll.value = false
  }
}

// 定时器
let streamingTimer: number | null = null

// 开始日志流
const startStreaming = () => {
  if (streamingTimer) return
  
  // 初始加载
  loadLogs()
  
  // 每2秒生成一条新日志（模拟实时日志）
  streamingTimer = window.setInterval(() => {
    if (isStreaming.value) {
      generateMockLog()
    }
  }, 2000)
}

// 停止日志流
const stopStreaming = () => {
  if (streamingTimer) {
    clearInterval(streamingTimer)
    streamingTimer = null
  }
}

onMounted(() => {
  loadLogs()
  startStreaming()
})

onUnmounted(() => {
  stopStreaming()
})
</script>

<style scoped>
.logs-container {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-form {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.log-stats {
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.log-list {
  background: #1e1e1e;
  border-radius: 4px;
  padding: 15px;
}

.log-item {
  margin-bottom: 10px;
  padding: 10px;
  background: #2d2d2d;
  border-left: 3px solid #409eff;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  transition: all 0.3s;
}

.log-item:hover {
  background: #333;
  transform: translateX(2px);
}

.log-item.log-debug {
  border-left-color: #909399;
}

.log-item.log-info {
  border-left-color: #67c23a;
}

.log-item.log-warn {
  border-left-color: #e6a23c;
}

.log-item.log-error {
  border-left-color: #f56c6c;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.log-time {
  color: #909399;
  font-size: 12px;
}

.log-source {
  color: #67c23a;
  font-size: 12px;
}

.log-message {
  color: #e0e0e0;
  line-height: 1.6;
  word-break: break-all;
}

.log-message :deep(mark) {
  background-color: #f56c6c;
  color: #fff;
  padding: 2px 4px;
  border-radius: 2px;
}

.log-details {
  margin-top: 10px;
}

.log-details pre {
  background: #1e1e1e;
  color: #e0e0e0;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 0;
}

.empty-logs {
  padding: 40px;
  text-align: center;
}

:deep(.el-scrollbar__wrap) {
  background: #1e1e1e;
}

:deep(.el-collapse) {
  border: none;
  background: transparent;
}

:deep(.el-collapse-item__header) {
  background: transparent;
  color: #909399;
  border: none;
  font-size: 12px;
}

:deep(.el-collapse-item__wrap) {
  background: transparent;
  border: none;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 0;
  color: #e0e0e0;
}
</style>
