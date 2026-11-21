<template>
  <div class="system-config">
    <div class="header">
      <div class="title">系统配置</div>
      <div class="actions">
        <el-button type="primary" @click="testConnections">
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
      </div>
    </div>

    <!-- 配置选项卡 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="基础配置" name="basic">
        <el-card>
          <el-form
            ref="basicFormRef"
            :model="basicConfig"
            label-width="150px"
            class="config-form"
          >
            <el-form-item label="系统名称">
              <el-input v-model="basicConfig.systemName" />
            </el-form-item>
            <el-form-item label="系统Logo">
              <el-input v-model="basicConfig.systemLogo" />
              <div class="form-tip">请输入Logo图片的URL地址</div>
            </el-form-item>
            <el-form-item label="会话超时时间(分钟)">
              <el-input-number
                v-model="basicConfig.sessionTimeout"
                :min="5"
                :max="1440"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item label="密码最小长度">
              <el-input-number
                v-model="basicConfig.passwordMinLength"
                :min="6"
                :max="20"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item label="密码复杂度">
              <el-select v-model="basicConfig.passwordComplexity" style="width: 100%">
                <el-option label="简单(纯数字或字母)" value="simple" />
                <el-option label="中等(数字+字母)" value="medium" />
                <el-option label="复杂(数字+字母+特殊字符)" value="complex" />
              </el-select>
            </el-form-item>
            <el-form-item label="告警通知方式">
              <el-checkbox-group v-model="basicConfig.notificationMethods">
                <el-checkbox value="email" label="邮件通知" />
                <el-checkbox value="sms" label="短信通知" />
                <el-checkbox value="system" label="系统通知" />
              </el-checkbox-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicConfig">保存配置</el-button>
              <el-button @click="resetBasicConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="数据源配置" name="datasource">
        <el-card>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="数据库连接">
              <div class="connection-status">
                <el-tag :type="dbConnectionStatus ? 'success' : 'danger'">
                  {{ dbConnectionStatus ? '已连接' : '连接失败' }}
                </el-tag>
                <el-button
                  type="primary"
                  link
                  @click="testDatabaseConnection"
                >
                  测试连接
                </el-button>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="Redis连接">
              <div class="connection-status">
                <el-tag :type="redisConnectionStatus ? 'success' : 'danger'">
                  {{ redisConnectionStatus ? '已连接' : '连接失败' }}
                </el-tag>
                <el-button
                  type="primary"
                  link
                  @click="testRedisConnection"
                >
                  测试连接
                </el-button>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="日志配置" name="log">
        <el-card>
          <el-form
            ref="logFormRef"
            :model="logConfig"
            label-width="150px"
            class="config-form"
          >
            <el-form-item label="日志保留天数">
              <el-input-number
                v-model="logConfig.logRetentionDays"
                :min="7"
                :max="365"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item label="操作日志启用">
              <el-switch v-model="logConfig.enableOperationLog" />
            </el-form-item>
            <el-form-item label="安全日志启用">
              <el-switch v-model="logConfig.enableSecurityLog" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveLogConfig">保存配置</el-button>
              <el-button @click="resetLogConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="备份配置" name="backup">
        <el-card>
          <el-form
            ref="backupFormRef"
            :model="backupConfig"
            label-width="150px"
            class="config-form"
          >
            <el-form-item label="自动备份启用">
              <el-switch v-model="backupConfig.enableAutoBackup" />
            </el-form-item>
            <el-form-item label="备份周期">
              <el-select v-model="backupConfig.backupCycle" style="width: 100%">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
            <el-form-item label="备份保留天数">
              <el-input-number
                v-model="backupConfig.backupRetentionDays"
                :min="7"
                :max="90"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item label="备份存储路径">
              <el-input v-model="backupConfig.backupPath" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBackupConfig">保存配置</el-button>
              <el-button @click="resetBackupConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection } from '@element-plus/icons-vue'
import { configApi, systemApi } from '@/api/system-manage'
import type { ApiResponse } from '@/types'

// 定义配置项类型
interface SysConfig {
  id: number
  configKey: string
  configValue: string
  configName: string
  configType: string
  description: string
}

// 定义连接测试结果类型
interface ConnectionTestResult {
  status: boolean
  database?: string
  redis?: string
}

// 响应式数据
const activeTab = ref('basic')
const dbConnectionStatus = ref(false)
const redisConnectionStatus = ref(false)

// 基础配置
const basicConfig = reactive({
  systemName: '',
  systemLogo: '',
  sessionTimeout: 30,
  passwordMinLength: 6,
  passwordComplexity: 'medium',
  notificationMethods: []
})

// 日志配置
const logConfig = reactive({
  logRetentionDays: 180,
  enableOperationLog: true,
  enableSecurityLog: true
})

// 备份配置
const backupConfig = reactive({
  enableAutoBackup: false,
  backupCycle: 'weekly',
  backupRetentionDays: 30,
  backupPath: ''
})

// 表单引用
const basicFormRef = ref()
const logFormRef = ref()
const backupFormRef = ref()

// 初始化
onMounted(() => {
  loadConfigs()
  testConnections()
})

// 加载配置
const loadConfigs = () => {
  // 加载基础配置
  configApi.getConfigByType('basic').then(res => {
    if (res.code === 200 && res.data.length > 0) {
      res.data.forEach((config: any) => {
        if (config.configKey === 'system.name') {
          basicConfig.systemName = config.configValue
        } else if (config.configKey === 'system.logo') {
          basicConfig.systemLogo = config.configValue
        } else if (config.configKey === 'session.timeout') {
          basicConfig.sessionTimeout = parseInt(config.configValue)
        } else if (config.configKey === 'password.min.length') {
          basicConfig.passwordMinLength = parseInt(config.configValue)
        } else if (config.configKey === 'password.complexity') {
          basicConfig.passwordComplexity = config.configValue
        } else if (config.configKey === 'notification.methods') {
          basicConfig.notificationMethods = config.configValue.split(',')
        }
      })
    }
  })

  // 加载日志配置
  configApi.getConfigByType('log').then(res => {
    if (res.code === 200 && res.data.length > 0) {
      res.data.forEach((config: any) => {
        if (config.configKey === 'log.retention.days') {
          logConfig.logRetentionDays = parseInt(config.configValue)
        } else if (config.configKey === 'log.enable.operation') {
          logConfig.enableOperationLog = config.configValue === 'true'
        } else if (config.configKey === 'log.enable.security') {
          logConfig.enableSecurityLog = config.configValue === 'true'
        }
      })
    }
  })

  // 加载备份配置
  configApi.getConfigByType('backup').then(res => {
    if (res.code === 200 && res.data.length > 0) {
      res.data.forEach((config: any) => {
        if (config.configKey === 'backup.auto.enable') {
          backupConfig.enableAutoBackup = config.configValue === 'true'
        } else if (config.configKey === 'backup.cycle') {
          backupConfig.backupCycle = config.configValue
        } else if (config.configKey === 'backup.retention.days') {
          backupConfig.backupRetentionDays = parseInt(config.configValue)
        } else if (config.configKey === 'backup.path') {
          backupConfig.backupPath = config.configValue
        }
      })
    }
  })
}

// 测试连接
const testConnections = () => {
  testDatabaseConnection()
  testRedisConnection()
}

const testDatabaseConnection = () => {
  systemApi.testDatabaseConnection().then(res => {
    if (res.code === 200) {
      dbConnectionStatus.value = res.data.status
      ElMessage.success(res.data.database)
    } else {
      dbConnectionStatus.value = false
      ElMessage.error('数据库连接测试失败')
    }
  })
}

const testRedisConnection = () => {
  systemApi.testRedisConnection().then(res => {
    if (res.code === 200) {
      redisConnectionStatus.value = res.data.status
      ElMessage.success(res.data.redis)
    } else {
      redisConnectionStatus.value = false
      ElMessage.error('Redis连接测试失败')
    }
  })
}

// 保存配置
const saveBasicConfig = () => {
  const configs = [
    {
      id: null,
      configKey: 'system.name',
      configValue: basicConfig.systemName,
      configName: '系统名称',
      configType: 'basic',
      description: '系统名称'
    },
    {
      id: null,
      configKey: 'system.logo',
      configValue: basicConfig.systemLogo,
      configName: '系统Logo',
      configType: 'basic',
      description: '系统Logo地址'
    },
    {
      id: null,
      configKey: 'session.timeout',
      configValue: basicConfig.sessionTimeout.toString(),
      configName: '会话超时时间',
      configType: 'basic',
      description: '会话超时时间(分钟)'
    },
    {
      id: null,
      configKey: 'password.min.length',
      configValue: basicConfig.passwordMinLength.toString(),
      configName: '密码最小长度',
      configType: 'basic',
      description: '密码最小长度'
    },
    {
      id: null,
      configKey: 'password.complexity',
      configValue: basicConfig.passwordComplexity,
      configName: '密码复杂度',
      configType: 'basic',
      description: '密码复杂度要求'
    },
    {
      id: null,
      configKey: 'notification.methods',
      configValue: basicConfig.notificationMethods.join(','),
      configName: '告警通知方式',
      configType: 'basic',
      description: '告警通知方式'
    }
  ]

  Promise.all(configs.map(config => configApi.updateConfig(config).catch(error => {
    console.error('配置更新失败:', error)
    return { code: 500, message: '更新失败', data: false }
  }))).then(responses => {
    const successCount = responses.filter(res => res.code === 200).length
    if (successCount === configs.length) {
      ElMessage.success('基础配置保存成功')
    } else {
      ElMessage.error(`部分配置保存失败 (${successCount}/${configs.length})`)
    }
  })
}

const saveLogConfig = () => {
  const configs = [
    {
      id: null,
      configKey: 'log.retention.days',
      configValue: logConfig.logRetentionDays.toString(),
      configName: '日志保留天数',
      configType: 'log',
      description: '操作日志保留天数'
    },
    {
      id: null,
      configKey: 'log.enable.operation',
      configValue: logConfig.enableOperationLog.toString(),
      configName: '操作日志启用',
      configType: 'log',
      description: '是否启用操作日志'
    },
    {
      id: null,
      configKey: 'log.enable.security',
      configValue: logConfig.enableSecurityLog.toString(),
      configName: '安全日志启用',
      configType: 'log',
      description: '是否启用安全日志'
    }
  ]

  Promise.all(configs.map(config => configApi.updateConfig(config).catch(error => {
    console.error('配置更新失败:', error)
    return { code: 500, message: '更新失败', data: false }
  }))).then(responses => {
    const successCount = responses.filter(res => res.code === 200).length
    if (successCount === configs.length) {
      ElMessage.success('日志配置保存成功')
    } else {
      ElMessage.error(`部分配置保存失败 (${successCount}/${configs.length})`)
    }
  })
}

const saveBackupConfig = () => {
  const configs = [
    {
      id: null,
      configKey: 'backup.auto.enable',
      configValue: backupConfig.enableAutoBackup.toString(),
      configName: '自动备份启用',
      configType: 'backup',
      description: '是否启用自动备份'
    },
    {
      id: null,
      configKey: 'backup.cycle',
      configValue: backupConfig.backupCycle,
      configName: '备份周期',
      configType: 'backup',
      description: '备份周期'
    },
    {
      id: null,
      configKey: 'backup.retention.days',
      configValue: backupConfig.backupRetentionDays.toString(),
      configName: '备份保留天数',
      configType: 'backup',
      description: '备份文件保留天数'
    },
    {
      id: null,
      configKey: 'backup.path',
      configValue: backupConfig.backupPath,
      configName: '备份存储路径',
      configType: 'backup',
      description: '备份文件存储路径'
    }
  ]

  Promise.all(configs.map(config => configApi.updateConfig(config).catch(error => {
    console.error('配置更新失败:', error)
    return { code: 500, message: '更新失败', data: false }
  }))).then(responses => {
    const successCount = responses.filter(res => res.code === 200).length
    if (successCount === configs.length) {
      ElMessage.success('备份配置保存成功')
    } else {
      ElMessage.error(`部分配置保存失败 (${successCount}/${configs.length})`)
    }
  })
}

// 重置配置
const resetBasicConfig = () => {
  loadConfigs()
}

const resetLogConfig = () => {
  loadConfigs()
}

const resetBackupConfig = () => {
  loadConfigs()
}

// 标签页切换
const handleTabClick = (tab: any) => {
  // 可以根据需要处理标签页切换逻辑
}
</script>

<style scoped>
.system-config {
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

.config-form {
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>