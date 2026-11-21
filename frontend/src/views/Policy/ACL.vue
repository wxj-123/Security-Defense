<template>
  <div class="acl-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="总规则数" :value="stats.totalRules">
            <template #prefix>
              <el-icon color="#409eff"><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card active">
          <el-statistic title="启用规则" :value="stats.activeRules">
            <template #prefix>
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="白名单IP" :value="stats.whitelistCount">
            <template #prefix>
              <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card blocked">
          <el-statistic title="黑名单IP" :value="stats.blacklistCount">
            <template #prefix>
              <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 切换 -->
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- ACL 规则 -->
        <el-tab-pane label="ACL规则" name="rules">
          <div class="tab-header">
            <el-button type="primary" @click="openRuleDialog()">
              <el-icon><Plus /></el-icon>
              添加规则
            </el-button>
          </div>

          <el-table :data="rules" border stripe v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="规则名称" width="200" />
            <el-table-column prop="type" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeTag(row.type)" size="small">
                  {{ row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceIp" label="源IP" width="150" />
            <el-table-column prop="destIp" label="目标IP" width="150" />
            <el-table-column prop="port" label="端口" width="100" />
            <el-table-column prop="action" label="动作" width="100">
              <template #default="{ row }">
                <el-tag :type="row.action === '允许' ? 'success' : 'danger'" size="small">
                  {{ row.action }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="100" />
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="toggleRule(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="openRuleDialog(row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" link @click="deleteRule(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- IP 白名单 -->
        <el-tab-pane label="IP白名单" name="whitelist">
          <div class="tab-header">
            <el-space>
              <el-input
                v-model="whitelistIp"
                placeholder="请输入IP地址，如 192.168.1.100"
                style="width: 300px"
              >
                <template #prepend>
                  <el-icon><Location /></el-icon>
                </template>
              </el-input>
              <el-input
                v-model="whitelistRemark"
                placeholder="备注"
                style="width: 200px"
              />
              <el-button type="success" @click="addWhitelist">
                <el-icon><Plus /></el-icon>
                添加
              </el-button>
            </el-space>
          </div>

          <el-table :data="whitelist" border stripe>
            <el-table-column prop="ip" label="IP地址" width="200" />
            <el-table-column prop="remark" label="备注" />
            <el-table-column prop="addTime" label="添加时间" width="180" />
            <el-table-column prop="addBy" label="添加人" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" link @click="removeWhitelist(row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- IP 黑名单 -->
        <el-tab-pane label="IP黑名单" name="blacklist">
          <div class="tab-header">
            <el-space>
              <el-input
                v-model="blacklistIp"
                placeholder="请输入IP地址，如 192.168.1.100"
                style="width: 300px"
              >
                <template #prepend>
                  <el-icon><Location /></el-icon>
                </template>
              </el-input>
              <el-input
                v-model="blacklistRemark"
                placeholder="封禁原因"
                style="width: 200px"
              />
              <el-button type="danger" @click="addBlacklist">
                <el-icon><Plus /></el-icon>
                添加
              </el-button>
            </el-space>
          </div>

          <el-table :data="blacklist" border stripe>
            <el-table-column prop="ip" label="IP地址" width="200" />
            <el-table-column prop="reason" label="封禁原因" />
            <el-table-column prop="blockTime" label="封禁时间" width="180" />
            <el-table-column prop="blockBy" label="操作人" width="120" />
            <el-table-column prop="expireTime" label="过期时间" width="180">
              <template #default="{ row }">
                {{ row.expireTime || '永久' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" link @click="unblockIp(row)">
                  解封
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 用户权限 -->
        <el-tab-pane label="用户权限" name="permissions">
          <div class="tab-header">
            <el-button type="primary" @click="openPermissionDialog()">
              <el-icon><Plus /></el-icon>
              添加权限
            </el-button>
          </div>

          <el-table :data="permissions" border stripe>
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="getRoleTag(row.role)" size="small">
                  {{ getRoleDisplayName(row.role) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="resources" label="资源权限">
              <template #default="{ row }">
                <el-tag
                  v-for="resource in row.resources"
                  :key="resource"
                  size="small"
                  style="margin-right: 5px"
                >
                  {{ resource }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ipRestriction" label="IP限制" width="150" />
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="togglePermission(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="openPermissionDialog(row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" link @click="deletePermission(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ACL 规则对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="currentRule ? '编辑规则' : '添加规则'"
      width="600px"
    >
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="ruleForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="IP控制" value="IP控制" />
            <el-option label="端口控制" value="端口控制" />
            <el-option label="用户控制" value="用户控制" />
            <el-option label="时间控制" value="时间控制" />
          </el-select>
        </el-form-item>
        <el-form-item label="源IP">
          <el-input v-model="ruleForm.sourceIp" placeholder="如: 192.168.1.0/24" />
        </el-form-item>
        <el-form-item label="目标IP">
          <el-input v-model="ruleForm.destIp" placeholder="如: 0.0.0.0/0" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="ruleForm.port" placeholder="如: 80, 443, 8080-8090" />
        </el-form-item>
        <el-form-item label="动作">
          <el-radio-group v-model="ruleForm.action">
            <el-radio :value="'允许'">允许</el-radio>
            <el-radio :value="'拒绝'">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="ruleForm.priority" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="ruleForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="currentPermission ? '编辑权限' : '添加权限'"
      width="600px"
    >
      <el-form :model="permissionForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="permissionForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="permissionForm.role" placeholder="请选择" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="安全审计员" value="auditor" />
            <el-option label="运维人员" value="operator" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源权限">
          <el-checkbox-group v-model="permissionForm.resources">
            <el-checkbox value="首页" label="首页" />
            <el-checkbox value="安全监控" label="安全监控" />
            <el-checkbox value="策略管理" label="策略管理" />
            <el-checkbox value="报表分析" label="报表分析" />
            <el-checkbox value="安全态势感知" label="安全态势感知" />
            <el-checkbox value="系统管理" label="系统管理" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="IP限制">
          <el-input v-model="permissionForm.ipRestriction" placeholder="如: 192.168.1.0/24, 留空为不限制" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="permissionForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  CircleCheck,
  CircleCheckFilled,
  CircleCloseFilled,
  Plus,
  Location
} from '@element-plus/icons-vue'

const loading = ref(false)
const activeTab = ref('rules')

// 统计数据
const stats = reactive({
  totalRules: 0,
  activeRules: 0,
  whitelistCount: 0,
  blacklistCount: 0
})

// ACL 规则
const rules = ref<any[]>([
  {
    id: 1,
    name: '允许内网访问',
    type: 'IP控制',
    sourceIp: '192.168.1.0/24',
    destIp: '0.0.0.0/0',
    port: '*',
    action: '允许',
    priority: 10,
    enabled: true
  },
  {
    id: 2,
    name: '限制SSH访问',
    type: '端口控制',
    sourceIp: '0.0.0.0/0',
    destIp: '10.0.0.0/8',
    port: '22',
    action: '拒绝',
    priority: 5,
    enabled: true
  },
  {
    id: 3,
    name: '工作时间控制',
    type: '时间控制',
    sourceIp: '0.0.0.0/0',
    destIp: '0.0.0.0/0',
    port: '*',
    action: '允许',
    priority: 8,
    enabled: false
  }
])

// IP 白名单
const whitelist = ref<any[]>([
  {
    id: 1,
    ip: '192.168.1.100',
    remark: '管理员电脑',
    addTime: '2025-11-09 10:00:00',
    addBy: 'admin'
  },
  {
    id: 2,
    ip: '10.0.0.50',
    remark: '安全扫描器',
    addTime: '2025-11-09 11:30:00',
    addBy: 'admin'
  }
])

const whitelistIp = ref('')
const whitelistRemark = ref('')

// IP 黑名单
const blacklist = ref<any[]>([
  {
    id: 1,
    ip: '203.0.113.42',
    reason: '多次暴力破解尝试',
    blockTime: '2025-11-09 09:00:00',
    blockBy: 'system',
    expireTime: null
  },
  {
    id: 2,
    ip: '198.51.100.88',
    reason: 'DDoS攻击',
    blockTime: '2025-11-09 12:00:00',
    blockBy: 'admin',
    expireTime: '2025-11-16 12:00:00'
  }
])

const blacklistIp = ref('')
const blacklistRemark = ref('')

// 用户权限
const permissions = ref<any[]>([
  {
    id: 1,
    username: 'admin',
    role: 'admin',
    resources: ['首页', '安全监控', '策略管理', '报表分析', '安全态势感知', '系统管理'],
    ipRestriction: '',
    enabled: true
  },
  {
    id: 2,
    username: 'auditor',
    role: 'auditor',
    resources: ['首页', '安全监控', '报表分析', '安全态势感知'],
    ipRestriction: '192.168.1.0/24',
    enabled: true
  },
  {
    id: 3,
    username: 'operator',
    role: 'operator',
    resources: ['首页', '策略管理', '安全态势感知'],
    ipRestriction: '',
    enabled: true
  }
])

// 对话框
const ruleDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const currentRule = ref<any>(null)
const currentPermission = ref<any>(null)

const ruleForm = reactive({
  name: '',
  type: '',
  sourceIp: '',
  destIp: '',
  port: '',
  action: '允许',
  priority: 10,
  enabled: true
})

const permissionForm = reactive({
  username: '',
  role: '',
  resources: [] as string[],
  ipRestriction: '',
  enabled: true
})

// 更新统计
const updateStats = () => {
  stats.totalRules = rules.value.length
  stats.activeRules = rules.value.filter(r => r.enabled).length
  stats.whitelistCount = whitelist.value.length
  stats.blacklistCount = blacklist.value.length
}

// 打开规则对话框
const openRuleDialog = (rule?: any) => {
  if (rule) {
    currentRule.value = rule
    Object.assign(ruleForm, rule)
  } else {
    currentRule.value = null
    Object.assign(ruleForm, {
      name: '',
      type: '',
      sourceIp: '',
      destIp: '',
      port: '',
      action: '允许',
      priority: 10,
      enabled: true
    })
  }
  ruleDialogVisible.value = true
}

// 保存规则
const saveRule = () => {
  if (currentRule.value) {
    Object.assign(currentRule.value, ruleForm)
    ElMessage.success('规则已更新')
  } else {
    rules.value.push({
      id: rules.value.length + 1,
      ...ruleForm
    })
    ElMessage.success('规则已添加')
  }
  ruleDialogVisible.value = false
  updateStats()
}

// 切换规则状态
const toggleRule = (rule: any) => {
  ElMessage.success(`规则已${rule.enabled ? '启用' : '禁用'}`)
  updateStats()
}

// 删除规则
const deleteRule = async (rule: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '警告', {
      type: 'warning'
    })
    const index = rules.value.findIndex(r => r.id === rule.id)
    if (index !== -1) {
      rules.value.splice(index, 1)
      ElMessage.success('规则已删除')
      updateStats()
    }
  } catch {
    // 用户取消
  }
}

// 添加白名单
const addWhitelist = () => {
  if (!whitelistIp.value) {
    ElMessage.warning('请输入IP地址')
    return
  }
  
  whitelist.value.push({
    id: whitelist.value.length + 1,
    ip: whitelistIp.value,
    remark: whitelistRemark.value,
    addTime: new Date().toLocaleString(),
    addBy: 'admin'
  })
  
  whitelistIp.value = ''
  whitelistRemark.value = ''
  ElMessage.success('已添加到白名单')
  updateStats()
}

// 移除白名单
const removeWhitelist = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要移除该IP吗？', '提示', {
      type: 'warning'
    })
    const index = whitelist.value.findIndex(w => w.id === row.id)
    if (index !== -1) {
      whitelist.value.splice(index, 1)
      ElMessage.success('已移除')
      updateStats()
    }
  } catch {
    // 用户取消
  }
}

// 添加黑名单
const addBlacklist = () => {
  if (!blacklistIp.value) {
    ElMessage.warning('请输入IP地址')
    return
  }
  
  blacklist.value.push({
    id: blacklist.value.length + 1,
    ip: blacklistIp.value,
    reason: blacklistRemark.value,
    blockTime: new Date().toLocaleString(),
    blockBy: 'admin',
    expireTime: null
  })
  
  blacklistIp.value = ''
  blacklistRemark.value = ''
  ElMessage.success('已添加到黑名单')
  updateStats()
}

// 解封IP
const unblockIp = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要解封该IP吗？', '提示', {
      type: 'warning'
    })
    const index = blacklist.value.findIndex(b => b.id === row.id)
    if (index !== -1) {
      blacklist.value.splice(index, 1)
      ElMessage.success('已解封')
      updateStats()
    }
  } catch {
    // 用户取消
  }
}

// 打开权限对话框
const openPermissionDialog = (permission?: any) => {
  if (permission) {
    currentPermission.value = permission
    Object.assign(permissionForm, {
      ...permission,
      resources: Array.isArray(permission.resources) ? [...permission.resources] : []
    })
  } else {
    currentPermission.value = null
    Object.assign(permissionForm, {
      username: '',
      role: '',
      resources: [],
      ipRestriction: '',
      enabled: true
    })
  }
  permissionDialogVisible.value = true
}

// 保存权限
const savePermission = () => {
  if (currentPermission.value) {
    // 修复：确保正确更新数组以保持响应性
    Object.assign(currentPermission.value, {
      ...permissionForm,
      resources: [...permissionForm.resources]
    })
    ElMessage.success('权限已更新')
  } else {
    permissions.value.push({
      id: permissions.value.length + 1,
      ...permissionForm,
      resources: [...permissionForm.resources]
    })
    ElMessage.success('权限已添加')
  }
  permissionDialogVisible.value = false
}

// 切换权限状态
const togglePermission = (permission: any) => {
  ElMessage.success(`权限已${permission.enabled ? '启用' : '禁用'}`)
}

// 删除权限
const deletePermission = async (permission: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限吗？', '警告', {
      type: 'warning'
    })
    const index = permissions.value.findIndex(p => p.id === permission.id)
    if (index !== -1) {
      permissions.value.splice(index, 1)
      ElMessage.success('权限已删除')
    }
  } catch {
    // 用户取消
  }
}

// 获取类型标签
const getTypeTag = (type: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const tags: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    'IP控制': 'primary',
    '端口控制': 'success',
    '用户控制': 'warning',
    '时间控制': 'info'
  }
  return tags[type] || 'info'
}

// 获取角色标签
const getRoleTag = (role: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const tags: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    'admin': 'danger',
    'auditor': 'warning',
    'operator': 'primary',
    'user': 'info'
  }
  // 如果是旧数据格式，保持兼容性
  const legacyTags: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '管理员': 'danger',
    '安全审计员': 'warning',
    '运维人员': 'primary',
    '普通用户': 'info'
  }
  return tags[role] || legacyTags[role] || 'info'
}

// 获取角色显示名称
const getRoleDisplayName = (role: string): string => {
  const names: Record<string, string> = {
    'admin': '管理员',
    'auditor': '安全审计员',
    'operator': '运维人员',
    'user': '普通用户'
  }
  // 如果是旧数据格式，保持兼容性
  const legacyNames: Record<string, string> = {
    '管理员': '管理员',
    '安全审计员': '安全审计员',
    '运维人员': '运维人员',
    '普通用户': '普通用户'
  }
  return names[role] || legacyNames[role] || role
}

onMounted(() => {
  updateStats()
})
</script>

<style scoped>
.acl-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 3px solid #409eff;
}

.stat-card.active {
  border-left-color: #67c23a;
}

.stat-card.blocked {
  border-left-color: #f56c6c;
}

.tab-header {
  margin-bottom: 20px;
}

:deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: bold;
}

:deep(.el-tabs__item) {
  font-size: 15px;
}
</style>
