<template>
  <div class="custom-report-container">
    <el-row :gutter="20">
      <!-- 左侧：报表配置 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>报表配置</span>
              <el-button link @click="resetConfig">重置</el-button>
            </div>
          </template>
          
          <el-form :model="reportConfig" label-width="100px" size="default">
            <el-form-item label="报表名称">
              <el-input v-model="reportConfig.name" placeholder="请输入报表名称" />
            </el-form-item>
            
            <el-form-item label="报表类型">
              <el-select v-model="reportConfig.type" placeholder="请选择" style="width: 100%;">
                <el-option label="安全事件报表" value="security" />
                <el-option label="策略执行报表" value="policy" />
                <el-option label="威胁情报报表" value="threat" />
                <el-option label="合规性报表" value="compliance" />
                <el-option label="综合分析报表" value="comprehensive" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="reportConfig.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%;"
              />
            </el-form-item>
            
            <el-divider>数据源选择</el-divider>
            
            <el-form-item label="事件源">
              <el-checkbox-group v-model="reportConfig.eventSources">
                <el-checkbox value="security" label="安全事件" />
                <el-checkbox value="firewall" label="防火墙" />
                <el-checkbox value="ids" label="入侵检测" />
                <el-checkbox value="antivirus" label="杀毒软件" />
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="事件等级">
              <el-checkbox-group v-model="reportConfig.eventLevels">
                <el-checkbox value="high" label="高危" />
                <el-checkbox value="medium" label="中危" />
                <el-checkbox value="low" label="低危" />
              </el-checkbox-group>
            </el-form-item>
            
            <el-divider>图表选择</el-divider>
            
            <el-form-item label="图表类型">
              <el-checkbox-group v-model="reportConfig.charts">
                <el-checkbox value="trend" label="趋势图" />
                <el-checkbox value="pie" label="饼图" />
                <el-checkbox value="bar" label="柱状图" />
                <el-checkbox value="line" label="折线图" />
                <el-checkbox value="table" label="数据表" />
              </el-checkbox-group>
            </el-form-item>
            
            <el-divider>报表选项</el-divider>
            
            <el-form-item label="输出格式">
              <el-radio-group v-model="reportConfig.format">
                <el-radio :value="'pdf'">PDF</el-radio>
                <el-radio :value="'excel'">Excel</el-radio>
                <el-radio :value="'html'">HTML</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="自动生成">
              <el-switch v-model="reportConfig.autoGenerate" />
              <span style="margin-left: 10px; font-size: 12px; color: #909399;">
                定时生成报表
              </span>
            </el-form-item>
            
            <el-form-item v-if="reportConfig.autoGenerate" label="生成周期">
              <el-select v-model="reportConfig.schedule" style="width: 100%;">
                <el-option label="每日" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" style="width: 100%;" @click="previewReport">
                <el-icon><View /></el-icon>
                预览报表
              </el-button>
            </el-form-item>
            
            <el-form-item>
              <el-button type="success" style="width: 100%;" @click="generateReport">
                <el-icon><DocumentCopy /></el-icon>
                生成报表
              </el-button>
            </el-form-item>
            
            <el-form-item>
              <el-button style="width: 100%;" @click="saveTemplate">
                <el-icon><FolderAdd /></el-icon>
                保存为模板
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 报表模板 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>我的模板</span>
          </template>
          <div class="template-list">
            <div 
              v-for="template in templates" 
              :key="template.id" 
              class="template-item"
              @click="loadTemplate(template)"
            >
              <div class="template-info">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-desc">{{ template.type }}</div>
              </div>
              <el-button 
                type="danger" 
                size="small" 
                link 
                @click.stop="deleteTemplate(template)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-empty v-if="templates.length === 0" description="暂无模板" :image-size="80" />
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：报表预览 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>报表预览</span>
              <div>
                <el-button size="small" @click="refreshPreview">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
                <el-button type="primary" size="small" @click="downloadReport">
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>
              </div>
            </div>
          </template>
          
          <div v-if="previewData" class="report-preview">
            <!-- 报表头部 -->
            <div class="report-header">
              <h1>{{ reportConfig.name || '自定义报表' }}</h1>
              <div class="report-meta">
                <span>生成时间：{{ new Date().toLocaleString() }}</span>
                <span style="margin-left: 20px;">报表类型：{{ getTypeLabel(reportConfig.type) }}</span>
              </div>
            </div>
            
            <!-- 概述卡片 -->
            <el-row :gutter="20" class="summary-row">
              <el-col :span="6">
                <div class="summary-card">
                  <div class="summary-value">{{ previewData.totalEvents }}</div>
                  <div class="summary-label">总事件数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-card">
                  <div class="summary-value" style="color: #f56c6c;">{{ previewData.highRisk }}</div>
                  <div class="summary-label">高危事件</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-card">
                  <div class="summary-value" style="color: #67c23a;">{{ previewData.handled }}</div>
                  <div class="summary-label">已处理</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-card">
                  <div class="summary-value" style="color: #e6a23c;">{{ previewData.pending }}</div>
                  <div class="summary-label">待处理</div>
                </div>
              </el-col>
            </el-row>
            
            <!-- 图表区域 -->
            <div v-if="reportConfig.charts.includes('trend')" class="chart-section">
              <h3>事件趋势分析</h3>
              <div ref="trendChartRef" style="width: 100%; height: 300px;"></div>
            </div>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <div v-if="reportConfig.charts.includes('pie')" class="chart-section">
                  <h3>事件类型分布</h3>
                  <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
                </div>
              </el-col>
              <el-col :span="12">
                <div v-if="reportConfig.charts.includes('bar')" class="chart-section">
                  <h3>事件等级统计</h3>
                  <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
                </div>
              </el-col>
            </el-row>
            
            <!-- 数据表格 -->
            <div v-if="reportConfig.charts.includes('table')" class="chart-section">
              <h3>详细数据</h3>
              <el-table :data="previewData.tableData" border stripe>
                <el-table-column prop="time" label="时间" width="180" />
                <el-table-column prop="type" label="事件类型" width="150" />
                <el-table-column prop="level" label="等级" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getLevelType(row.level)" size="small">{{ row.level }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="source" label="来源" width="150" />
                <el-table-column prop="description" label="描述" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === '已处理' ? 'success' : 'warning'" size="small">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 报表脚注 -->
            <div class="report-footer">
              <el-divider />
              <p style="text-align: center; color: #909399; font-size: 12px;">
                本报表由网络安全防御系统自动生成 | 生成时间：{{ new Date().toLocaleString() }}
              </p>
            </div>
          </div>
          
          <el-empty v-else description="请配置报表参数并点击预览" :image-size="200" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  DocumentCopy,
  FolderAdd,
  Delete,
  Refresh,
  Download
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { generateCustomReport, exportReport } from '@/api/report'

// 报表配置
const reportConfig = reactive({
  name: '',
  type: 'security',
  dateRange: [],
  eventSources: ['security', 'firewall'],
  eventLevels: ['high', 'medium', 'low'],
  charts: ['trend', 'pie', 'bar', 'table'],
  format: 'pdf',
  autoGenerate: false,
  schedule: 'weekly'
})

// 预览数据
const previewData = ref<any>(null)

// 模板列表
const templates = ref([
  {
    id: 1,
    name: '每周安全报表',
    type: '安全事件报表',
    config: {
      type: 'security',
      eventSources: ['security', 'firewall', 'ids'],
      eventLevels: ['high', 'medium'],
      charts: ['trend', 'pie', 'table']
    }
  },
  {
    id: 2,
    name: '月度合规报告',
    type: '合规性报表',
    config: {
      type: 'compliance',
      charts: ['bar', 'table']
    }
  }
])

// 图表引用
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

// 获取类型标签
const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    security: '安全事件报表',
    policy: '策略执行报表',
    threat: '威胁情报报表',
    compliance: '合规性报表',
    comprehensive: '综合分析报表'
  }
  return map[type] || '自定义报表'
}

// 获取等级类型
const getLevelType = (level: string): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const map: Record<string, 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '高危': 'danger',
    '中危': 'warning',
    '低危': 'success'
  }
  return map[level] || 'info'
}

// 重置配置
const resetConfig = () => {
  reportConfig.name = ''
  reportConfig.type = 'security'
  reportConfig.dateRange = []
  reportConfig.eventSources = ['security', 'firewall']
  reportConfig.eventLevels = ['high', 'medium', 'low']
  reportConfig.charts = ['trend', 'pie', 'bar', 'table']
  reportConfig.format = 'pdf'
  reportConfig.autoGenerate = false
  reportConfig.schedule = 'weekly'
  previewData.value = null
  ElMessage.success('已重置配置')
}

// 预览报表
const previewReport = async () => {
  if (!reportConfig.name) {
    ElMessage.warning('请输入报表名称')
    return
  }
  
  // 生成模拟数据
  previewData.value = {
    totalEvents: 647,
    highRisk: 89,
    handled: 589,
    pending: 58,
    tableData: [
      {
        time: '2025-01-15 14:30',
        type: 'SQL注入',
        level: '高危',
        source: '防火墙',
        description: '检测到SQL注入攻击尝试',
        status: '已处理'
      },
      {
        time: '2025-01-15 13:20',
        type: 'XSS攻击',
        level: '中危',
        source: 'IDS',
        description: '发现跨站脚本攻击',
        status: '已处理'
      },
      {
        time: '2025-01-15 12:15',
        type: 'DDoS攻击',
        level: '高危',
        source: '防火墙',
        description: '大量异常流量',
        status: '待处理'
      },
      {
        time: '2025-01-15 11:00',
        type: '暴力破解',
        level: '中危',
        source: '安全事件',
        description: '多次登录失败尝试',
        status: '已处理'
      },
      {
        time: '2025-01-15 10:30',
        type: '恶意扫描',
        level: '低危',
        source: 'IDS',
        description: '端口扫描行为',
        status: '已处理'
      }
    ]
  }
  
  ElMessage.success('报表预览生成成功')
  
  // 等待DOM更新后初始化图表
  await nextTick()
  initCharts()
}

// 初始化图表
const initCharts = () => {
  // 趋势图
  if (reportConfig.charts.includes('trend') && trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    
    const dates = []
    const counts = []
    for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      dates.push(date.toLocaleDateString())
      counts.push(Math.floor(Math.random() * 100) + 50)
    }
    
    const trendOption: EChartsOption = {
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: dates,
        boundaryGap: false
      },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'line',
          smooth: true,
          data: counts,
          itemStyle: { color: '#409eff' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ])
          }
        }
      ]
    }
    trendChart.setOption(trendOption)
  }
  
  // 饼图
  if (reportConfig.charts.includes('pie') && pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    
    const pieOption: EChartsOption = {
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { value: 156, name: 'SQL注入' },
            { value: 89, name: 'XSS攻击' },
            { value: 45, name: 'DDoS攻击' },
            { value: 234, name: '暴力破解' },
            { value: 123, name: '恶意扫描' }
          ]
        }
      ]
    }
    pieChart.setOption(pieOption)
  }
  
  // 柱状图
  if (reportConfig.charts.includes('bar') && barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    
    const barOption: EChartsOption = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['高危', '中危', '低危'] },
      xAxis: {
        type: 'category',
        data: ['SQL注入', 'XSS攻击', 'DDoS', '暴力破解', '恶意扫描']
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '高危',
          type: 'bar',
          data: [34, 12, 23, 15, 5],
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '中危',
          type: 'bar',
          data: [78, 45, 18, 89, 56],
          itemStyle: { color: '#e6a23c' }
        },
        {
          name: '低危',
          type: 'bar',
          data: [44, 32, 4, 130, 62],
          itemStyle: { color: '#67c23a' }
        }
      ]
    }
    barChart.setOption(barOption)
  }
}

// 生成报表
const generateReport = async () => {
  if (!previewData.value) {
    ElMessage.warning('请先预览报表')
    return
  }
  
  try {
    ElMessage.info(`正在生成 ${reportConfig.format.toUpperCase()} 报表...`)
    
    // 调用后端API生成报表
    const response = await generateCustomReport({
      ...reportConfig,
      previewData: previewData.value
    })
    
    if (response.code === 200) {
      ElMessage.success('报表生成成功')
      
      // 自动触发下载
      await downloadGeneratedReport(response.data.id)
    } else {
      ElMessage.error('报表生成失败: ' + response.message)
    }
  } catch (error: any) {
    ElMessage.error('报表生成失败: ' + (error.message || '未知错误'))
  }
}

// 下载生成的报表
const downloadGeneratedReport = async (reportId: number) => {
  try {
    // 构造包含报表数据的URL
    const reportData = JSON.stringify({
      config: reportConfig,
      preview: previewData.value
    });
    
    // 对数据进行URL编码
    const encodedData = encodeURIComponent(reportData);
    const url = `/api/report/${reportId}/export?format=${reportConfig.format}&reportData=${encodedData}`;
    
    // 使用window.open方式触发文件下载
    const link = document.createElement('a');
    link.href = url;
    link.download = `report_${reportId}.${reportConfig.format}`;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('报表下载成功');
  } catch (error: any) {
    ElMessage.error('报表下载失败: ' + (error.message || '未知错误'));
  }
}

// 保存模板
const saveTemplate = async () => {
  if (!reportConfig.name) {
    ElMessage.warning('请输入报表名称')
    return
  }
  
  try {
    const { value } = await ElMessageBox.prompt('请输入模板名称', '保存模板', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: reportConfig.name
    })
    
    templates.value.push({
      id: templates.value.length + 1,
      name: value,
      type: getTypeLabel(reportConfig.type),
      config: { ...reportConfig }
    })
    
    ElMessage.success('模板保存成功')
  } catch {
    // 用户取消
  }
}

// 加载模板
const loadTemplate = (template: any) => {
  Object.assign(reportConfig, template.config)
  ElMessage.success(`已加载模板：${template.name}`)
}

// 删除模板
const deleteTemplate = async (template: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除模板「${template.name}」吗？`, '提示', {
      type: 'warning'
    })
    
    const index = templates.value.findIndex(t => t.id === template.id)
    if (index !== -1) {
      templates.value.splice(index, 1)
      ElMessage.success('模板已删除')
    }
  } catch {
    // 用户取消
  }
}

// 刷新预览
const refreshPreview = () => {
  if (previewData.value) {
    previewReport()
  } else {
    ElMessage.info('请先配置并预览报表')
  }
}

// 下载报表
const downloadReport = () => {
  if (!previewData.value) {
    ElMessage.warning('请先预览报表')
    return
  }
  generateReport()
}

// 监听图表选项变化
watch(() => reportConfig.charts, () => {
  if (previewData.value) {
    nextTick(() => {
      initCharts()
    })
  }
}, { deep: true })
</script>

<style scoped>
.custom-report-container {
  padding: 20px;
}

.template-list {
  max-height: 300px;
  overflow-y: auto;
}

.template-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.template-info {
  flex: 1;
}

.template-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.template-desc {
  font-size: 12px;
  color: #909399;
}

.report-preview {
  padding: 20px;
  background: #fff;
}

.report-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #409eff;
}

.report-header h1 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 28px;
}

.report-meta {
  font-size: 14px;
  color: #909399;
}

.summary-row {
  margin-bottom: 30px;
}

.summary-card {
  padding: 20px;
  text-align: center;
  background: #f5f7fa;
  border-radius: 8px;
}

.summary-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
}

.chart-section {
  margin-bottom: 30px;
}

.chart-section h3 {
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
  font-size: 16px;
}

.report-footer {
  margin-top: 40px;
}

:deep(.el-checkbox) {
  display: block;
  margin: 10px 0;
}

:deep(.el-radio) {
  display: block;
  margin: 10px 0;
}
</style>
