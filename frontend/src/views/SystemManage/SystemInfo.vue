<template>
  <div class="system-info">
    <div class="header">
      <div class="title">系统信息</div>
      <el-button type="primary" @click="refreshSystemInfo">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 系统信息卡片 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="info-card" header="JVM 信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="最大内存">
              {{ formatSize(systemInfo.jvm?.maxMemory) }}
            </el-descriptions-item>
            <el-descriptions-item label="已分配内存">
              {{ formatSize(systemInfo.jvm?.totalMemory) }}
            </el-descriptions-item>
            <el-descriptions-item label="已使用内存">
              {{ formatSize(systemInfo.jvm?.usedMemory) }}
            </el-descriptions-item>
            <el-descriptions-item label="空闲内存">
              {{ formatSize(systemInfo.jvm?.freeMemory) }}
            </el-descriptions-item>
            <el-descriptions-item label="内存使用率" :span="2">
              <el-progress
                :percentage="memoryUsagePercentage"
                :color="getMemoryUsageColor(memoryUsagePercentage)"
                :format="percentage => `内存使用率: ${percentage}%`"
              />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="info-card" header="操作系统信息">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">
              {{ systemInfo.os?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="系统版本">
              {{ systemInfo.os?.version }}
            </el-descriptions-item>
            <el-descriptions-item label="系统架构">
              {{ systemInfo.os?.arch }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="info-card" header="Java 环境">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="Java 版本">
              {{ systemInfo.java?.version }}
            </el-descriptions-item>
            <el-descriptions-item label="Java 供应商">
              {{ systemInfo.java?.vendor }}
            </el-descriptions-item>
            <el-descriptions-item label="Java 安装路径">
              {{ systemInfo.java?.home }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="info-card" header="数据库连接状态">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="数据库">
              <el-tag :type="dbConnectionStatus ? 'success' : 'danger'">
                {{ dbConnectionStatus ? '已连接' : '连接失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Redis">
              <el-tag :type="redisConnectionStatus ? 'success' : 'danger'">
                {{ redisConnectionStatus ? '已连接' : '连接失败' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="info-card" header="应用信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="应用名称">
              网络安全防御系统
            </el-descriptions-item>
            <el-descriptions-item label="应用版本">
              v1.0.0
            </el-descriptions-item>
            <el-descriptions-item label="构建时间">
              2025-11-23 10:30:00
            </el-descriptions-item>
            <el-descriptions-item label="启动时间">
              {{ startTime }}
            </el-descriptions-item>
            <el-descriptions-item label="运行时长">
              {{ runTime }}
            </el-descriptions-item>
            <el-descriptions-item label="环境">
              <el-tag>生产环境</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { systemApi } from '@/api/system-manage'

// 响应式数据
const systemInfo = reactive({
  jvm: {
    maxMemory: 0,
    totalMemory: 0,
    usedMemory: 0,
    freeMemory: 0
  },
  os: {
    name: '',
    version: '',
    arch: ''
  },
  java: {
    version: '',
    vendor: '',
    home: ''
  }
})

const dbConnectionStatus = ref(false)
const redisConnectionStatus = ref(false)
const startTime = ref('2025-11-23 10:30:00')
const runTime = ref('')
const refreshTimer = ref<NodeJS.Timeout | null>(null)

// 计算属性
const memoryUsagePercentage = computed(() => {
  const { maxMemory, usedMemory } = systemInfo.jvm
  if (!maxMemory || !usedMemory) return 0
  return Math.round((usedMemory / maxMemory) * 100)
})

// 初始化
onMounted(() => {
  loadSystemInfo()
  testConnections()
  calculateRunTime()
  
  // 每分钟刷新一次
  refreshTimer.value = setInterval(() => {
    loadSystemInfo()
    calculateRunTime()
  }, 60000)
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})

// 加载系统信息
const loadSystemInfo = () => {
  systemApi.getSystemInfo().then(res => {
    if (res.code === 200) {
      Object.assign(systemInfo, res.data)
    }
  })
}

// 测试连接
const testConnections = () => {
  systemApi.testDatabaseConnection().then(res => {
    if (res.code === 200) {
      dbConnectionStatus.value = res.data.status
    } else {
      dbConnectionStatus.value = false
    }
  })
  
  systemApi.testRedisConnection().then(res => {
    if (res.code === 200) {
      redisConnectionStatus.value = res.data.status
    } else {
      redisConnectionStatus.value = false
    }
  })
}

// 刷新系统信息
const refreshSystemInfo = () => {
  loadSystemInfo()
  testConnections()
  ElMessage.success('系统信息已刷新')
}

// 计算运行时长
const calculateRunTime = () => {
  const start = new Date(startTime.value)
  const now = new Date()
  const diff = now.getTime() - start.getTime()
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  runTime.value = `${days}天 ${hours}小时 ${minutes}分钟`
}

// 获取内存使用率颜色
const getMemoryUsageColor = (percentage: number) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

// 格式化大小
const formatSize = (size: number) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let fileSize = size
  
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  
  return `${fileSize.toFixed(2)} ${units[index]}`
}
</script>

<style scoped>
.system-info {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.info-card {
  margin-bottom: 20px;
}
</style>