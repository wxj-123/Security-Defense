<template>
  <div class="firewall-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>防火墙规则管理</span>
          <el-button type="primary" size="small" @click="handleAdd">添加规则</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="规则名称" width="150" />
        <el-table-column prop="action" label="动作" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceIp" label="源IP" width="150" />
        <el-table-column prop="targetIp" label="目标IP" width="150" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="protocol" label="协议" width="100" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggle(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @current-change="loadRules"
        style="margin-top: 20px"
      />
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="动作">
          <el-select v-model="formData.action">
            <el-option label="允许" value="allow" />
            <el-option label="拒绝" value="deny" />
            <el-option label="丢弃" value="drop" />
          </el-select>
        </el-form-item>
        <el-form-item label="源IP">
          <el-input v-model="formData.sourceIp" />
        </el-form-item>
        <el-form-item label="目标IP">
          <el-input v-model="formData.targetIp" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="formData.port" />
        </el-form-item>
        <el-form-item label="协议">
          <el-select v-model="formData.protocol">
            <el-option label="TCP" value="tcp" />
            <el-option label="UDP" value="udp" />
            <el-option label="ICMP" value="icmp" />
            <el-option label="ANY" value="any" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="formData.priority" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FirewallRule } from '@/types'
import { getFirewallRules, addFirewallRule, updateFirewallRule, deleteFirewallRule, toggleFirewallRule } from '@/api/policy'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加规则')
const tableData = ref<FirewallRule[]>([])

const formData = reactive<Partial<FirewallRule>>({
  name: '',
  action: 'allow',
  sourceIp: '',
  targetIp: '',
  port: '',
  protocol: 'tcp',
  priority: 50
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const loadRules = async () => {
  loading.value = true
  try {
    const res = await getFirewallRules({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加规则'
  Object.assign(formData, {
    name: '',
    action: 'allow',
    sourceIp: '',
    targetIp: '',
    port: '',
    protocol: 'tcp',
    priority: 50
  })
  dialogVisible.value = true
}

const handleEdit = (row: FirewallRule) => {
  dialogTitle.value = '编辑规则'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await updateFirewallRule(formData.id, formData)
    } else {
      await addFirewallRule(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleToggle = async (row: FirewallRule) => {
  try {
    await toggleFirewallRule(row.id, row.enabled)
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
    row.enabled = !row.enabled
  }
}

const handleDelete = async (row: FirewallRule) => {
  try {
    await ElMessageBox.confirm('确定要删除此规则吗？', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const response = await deleteFirewallRule(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadRules()
    } else {
      ElMessage.error('删除失败: ' + response.message)
    }
  } catch (error: any) {
    // Element Plus 的 MessageBox 在用户取消时会抛出 'cancel' 字符串
    if (error === 'cancel') {
      // 用户取消操作，不显示错误消息
      return
    }
    // 其他错误情况
    ElMessage.error('删除失败: ' + (error.message || '未知错误'))
  }
}

const getActionType = (action: string) => {
  const types: Record<string, any> = {
    allow: 'success',
    deny: 'danger',
    drop: 'warning'
  }
  return types[action] || 'info'
}

const getActionText = (action: string) => {
  const texts: Record<string, string> = {
    allow: '允许',
    deny: '拒绝',
    drop: '丢弃'
  }
  return texts[action] || action
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.firewall-container {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
