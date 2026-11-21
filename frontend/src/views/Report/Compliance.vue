<template>
  <div class="compliance-container">
    <!-- 合规性概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <el-icon :size="40" color="#409eff"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalStandards }}</div>
              <div class="stat-label">合规标准数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9f0;">
              <el-icon :size="40" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.compliant }}</div>
              <div class="stat-label">合规项</div>
              <div class="stat-rate" style="color: #67c23a;">{{ overview.compliantRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0;">
              <el-icon :size="40" color="#f56c6c"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.nonCompliant }}</div>
              <div class="stat-label">不合规项</div>
              <div class="stat-rate" style="color: #f56c6c;">{{ overview.nonCompliantRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef5e7;">
              <el-icon :size="40" color="#e6a23c"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.warning }}</div>
              <div class="stat-label">待改进项</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 合规性标准选择 -->
    <el-card class="standard-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>合规性标准</span>
          <el-button type="primary" size="small" @click="generateReport">
            <el-icon><DocumentCopy /></el-icon>
            生成报告
          </el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeStandard" @tab-change="handleStandardChange">
        <el-tab-pane label="等级保护2.0" name="djbh">
          <div class="standard-content">
            <div class="compliance-score">
              <el-progress 
                type="dashboard" 
                :percentage="standards.djbh.score"
                :width="180"
                :color="getScoreColor(standards.djbh.score)"
              >
                <template #default="{ percentage }">
                  <span class="percentage-value">{{ percentage }}</span>
                  <span class="percentage-label">合规分数</span>
                </template>
              </el-progress>
            </div>
            <div class="compliance-details">
              <h3>等级保护2.0 三级</h3>
              <p>根据《信息安全技术 网络安全等级保护基本要求》（GB/T 22239-2019）进行评估</p>
              
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">安全物理环境：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">95分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">安全通信网络：</span>
                      <el-tag type="warning" size="small">待改进</el-tag>
                      <span class="detail-score">78分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">安全区域边界：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">88分</span>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">安全计算环境：</span>
                      <el-tag type="danger" size="small">不合规</el-tag>
                      <span class="detail-score">65分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">安全管理中心：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">90分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">安全管理制度：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">92分</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="ISO 27001" name="iso27001">
          <div class="standard-content">
            <div class="compliance-score">
              <el-progress 
                type="dashboard" 
                :percentage="standards.iso27001.score"
                :width="180"
                :color="getScoreColor(standards.iso27001.score)"
              >
                <template #default="{ percentage }">
                  <span class="percentage-value">{{ percentage }}</span>
                  <span class="percentage-label">合规分数</span>
                </template>
              </el-progress>
            </div>
            <div class="compliance-details">
              <h3>ISO/IEC 27001:2013 信息安全管理体系</h3>
              <p>国际标准化组织制定的信息安全管理体系标准</p>
              
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">A.5 信息安全方针：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">92分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">A.6 信息安全组织：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">87分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">A.7 人力资源安全：</span>
                      <el-tag type="warning" size="small">待改进</el-tag>
                      <span class="detail-score">75分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">A.8 资产管理：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">90分</span>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">A.9 访问控制：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">88分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">A.10 密码学：</span>
                      <el-tag type="danger" size="small">不合规</el-tag>
                      <span class="detail-score">68分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">A.11 物理和环境安全：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">93分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">A.12 运行安全：</span>
                      <el-tag type="warning" size="small">待改进</el-tag>
                      <span class="detail-score">79分</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="PCI DSS" name="pcidss">
          <div class="standard-content">
            <div class="compliance-score">
              <el-progress 
                type="dashboard" 
                :percentage="standards.pcidss.score"
                :width="180"
                :color="getScoreColor(standards.pcidss.score)"
              >
                <template #default="{ percentage }">
                  <span class="percentage-value">{{ percentage }}</span>
                  <span class="percentage-label">合规分数</span>
                </template>
              </el-progress>
            </div>
            <div class="compliance-details">
              <h3>PCI DSS 支付卡行业数据安全标准</h3>
              <p>保护持卡人数据的全球性安全标准</p>
              
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">1. 防火墙配置：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">90分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">2. 默认密码管理：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">85分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">3. 存储数据保护：</span>
                      <el-tag type="danger" size="small">不合规</el-tag>
                      <span class="detail-score">62分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">4. 传输加密：</span>
                      <el-tag type="warning" size="small">待改进</el-tag>
                      <span class="detail-score">76分</span>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">5. 防病毒软件：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">88分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">6. 安全系统开发：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">82分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">7. 访问控制：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">87分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">8. 唯一ID分配：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">91分</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="GDPR" name="gdpr">
          <div class="standard-content">
            <div class="compliance-score">
              <el-progress 
                type="dashboard" 
                :percentage="standards.gdpr.score"
                :width="180"
                :color="getScoreColor(standards.gdpr.score)"
              >
                <template #default="{ percentage }">
                  <span class="percentage-value">{{ percentage }}</span>
                  <span class="percentage-label">合规分数</span>
                </template>
              </el-progress>
            </div>
            <div class="compliance-details">
              <h3>GDPR 通用数据保护条例</h3>
              <p>欧盟数据保护和隐私法规</p>
              
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">数据处理合法性：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">89分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">数据主体权利：</span>
                      <el-tag type="warning" size="small">待改进</el-tag>
                      <span class="detail-score">74分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">数据安全措施：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">86分</span>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never" class="detail-card">
                    <div class="detail-item">
                      <span class="detail-label">数据泄露通知：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">91分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">数据影响评估：</span>
                      <el-tag type="danger" size="small">不合规</el-tag>
                      <span class="detail-score">66分</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">数据保护官：</span>
                      <el-tag type="success" size="small">合规</el-tag>
                      <span class="detail-score">88分</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 合规性趋势 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>合规性趋势分析</span>
      </template>
      <div ref="trendChartRef" style="width: 100%; height: 350px;"></div>
    </el-card>

    <!-- 不合规项详情 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>不合规项及整改建议</span>
      </template>
      <el-table :data="nonCompliantItems" border stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding: 20px;">
              <h4>整改建议：</h4>
              <ul style="line-height: 2;">
                <li v-for="(suggestion, index) in row.suggestions" :key="index">
                  {{ suggestion }}
                </li>
              </ul>
              <el-divider />
              <div style="display: flex; gap: 10px;">
                <el-button type="primary" size="small" @click="createTask(row)">
                  创建整改任务
                </el-button>
                <el-button size="small" @click="ignoreItem(row)">
                  忽略此项
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="standard" label="合规标准" width="150" />
        <el-table-column prop="category" label="类别" width="180" />
        <el-table-column prop="item" label="检查项" show-overflow-tooltip />
        <el-table-column prop="currentStatus" label="当前状态" width="200" />
        <el-table-column prop="risk" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.risk)" size="small">{{ row.risk }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="整改期限" width="120" />
      </el-table>
    </el-card>

    <!-- 生成报告对话框 -->
    <el-dialog v-model="reportDialogVisible" title="生成合规性报告" width="600px">
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="报告名称">
          <el-input v-model="reportForm.name" placeholder="请输入报告名称" />
        </el-form-item>
        <el-form-item label="合规标准">
          <el-select v-model="reportForm.standard" placeholder="请选择" style="width: 100%;">
            <el-option label="等级保护2.0" value="djbh" />
            <el-option label="ISO 27001" value="iso27001" />
            <el-option label="PCI DSS" value="pcidss" />
            <el-option label="GDPR" value="gdpr" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="reportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="报告格式">
          <el-radio-group v-model="reportForm.format">
            <el-radio :value="'pdf'">PDF</el-radio>
            <el-radio :value="'word'">Word</el-radio>
            <el-radio :value="'excel'">Excel</el-radio>
            <el-radio :value="'html'">HTML</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="包含内容">
          <el-checkbox-group v-model="reportForm.includes">
            <el-checkbox value="summary" label="总体概览" />
            <el-checkbox value="details" label="详细评估" />
            <el-checkbox value="nonCompliant" label="不合规项" />
            <el-checkbox value="suggestions" label="整改建议" />
            <el-checkbox value="trend" label="趋势分析" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmGenerate">
          <el-icon><Download /></el-icon>
          生成报告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  CircleCheck,
  CircleClose,
  Warning,
  DocumentCopy,
  Download
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const activeStandard = ref('djbh')
const reportDialogVisible = ref(false)
const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let resizeHandler: (() => void) | null = null

// 概览数据
const overview = reactive({
  totalStandards: 156,
  compliant: 128,
  nonCompliant: 18,
  warning: 10,
  compliantRate: 82,
  nonCompliantRate: 12
})

// 各标准数据
const standards = reactive({
  djbh: { score: 84 },
  iso27001: { score: 81 },
  pcidss: { score: 79 },
  gdpr: { score: 83 }
})

// 报告表单
const reportForm = reactive({
  name: '',
  standard: 'djbh',
  dateRange: [],
  format: 'pdf',
  includes: ['summary', 'details', 'nonCompliant', 'suggestions']
})

// 不合规项
interface NonCompliantItem {
  standard: string;
  category: string;
  item: string;
  currentStatus: string;
  risk: '高' | '中' | '低';
  deadline: string;
  suggestions: string[];
}

const nonCompliantItems = ref<NonCompliantItem[]>([
  {
    standard: '等级保护2.0',
    category: '安全计算环境',
    item: '身份鉴别机制不完善',
    currentStatus: '缺少双因素认证',
    risk: '高',
    deadline: '30天',
    suggestions: [
      '部署双因素认证系统（如短信验证码、动态令牌）',
      '强制要求管理员账户启用双因素认证',
      '定期审计身份鉴别日志'
    ]
  },
  {
    standard: 'ISO 27001',
    category: 'A.10 密码学',
    item: '密钥管理策略缺失',
    currentStatus: '未建立密钥生命周期管理',
    risk: '高',
    deadline: '45天',
    suggestions: [
      '制定密钥管理策略和流程',
      '部署密钥管理系统（KMS）',
      '实施密钥定期轮换机制'
    ]
  },
  {
    standard: 'PCI DSS',
    category: '3. 存储数据保护',
    item: '持卡人数据加密不足',
    currentStatus: '部分敏感数据未加密存储',
    risk: '高',
    deadline: '15天',
    suggestions: [
      '对所有持卡人数据实施强加密',
      '使用经过验证的加密算法（AES-256）',
      '加密密钥与数据分离存储'
    ]
  },
  {
    standard: 'GDPR',
    category: '数据影响评估',
    item: '缺少数据保护影响评估',
    currentStatus: '未对高风险数据处理进行DPIA',
    risk: '中',
    deadline: '60天',
    suggestions: [
      '识别需要进行DPIA的数据处理活动',
      '建立DPIA流程和模板',
      '定期更新和审查DPIA结果'
    ]
  },
  {
    standard: '等级保护2.0',
    category: '安全通信网络',
    item: '网络流量监控不足',
    currentStatus: '缺少全网流量分析工具',
    risk: '中',
    deadline: '45天',
    suggestions: [
      '部署网络流量分析系统',
      '配置异常流量告警规则',
      '定期分析网络流量报告'
    ]
  }
])

// 获取分数颜色
const getScoreColor = (score: number): string => {
  if (score >= 85) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 获取风险类型
const getRiskType = (risk: '高' | '中' | '低'): 'danger' | 'warning' | 'success' | 'info' | 'primary' => {
  const map: Record<'高' | '中' | '低', 'danger' | 'warning' | 'success' | 'info' | 'primary'> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'success'
  }
  return map[risk] || 'info'
}

// 标准切换
const handleStandardChange = (): void => {
  // 可以加载对应标准的详细数据
}

// 生成报告
const generateReport = (): void => {
  reportDialogVisible.value = true
}

// 确认生成
const confirmGenerate = async (): Promise<void> => {
  if (!reportForm.name) {
    ElMessage.warning('请输入报告名称')
    return
  }
  
  try {
    ElMessage.success('报告生成中，请稍候...')
    
    // 模拟生成报告数据
    const reportData = {
      name: reportForm.name,
      standard: reportForm.standard,
      dateRange: reportForm.dateRange,
      format: reportForm.format,
      includes: reportForm.includes,
      generatedAt: new Date().toISOString()
    }
    
    // 生成并下载报告
    await generateAndDownloadReport(reportData)
    
    reportDialogVisible.value = false
    ElMessage.success('报告已生成并下载')
  } catch (error) {
    console.error('生成报告失败:', error)
    ElMessage.error('报告生成失败: ' + (error instanceof Error ? error.message : '未知错误'))
  }
}

// 定义报告数据接口
interface ReportData {
  name: string;
  standard: string;
  dateRange: any[];
  format: string;
  includes: string[];
  generatedAt: string;
}

// 生成并下载报告
const generateAndDownloadReport = async (reportData: ReportData) => {
  try {
    // 根据格式生成不同类型的报告内容
    let content = ''
    let mimeType = ''
    let fileExtension = ''
    
    switch (reportData.format) {
      case 'pdf':
        content = generatePDFContent(reportData)
        mimeType = 'application/pdf'
        fileExtension = 'pdf'
        break
      case 'word':
        content = generateWordContent(reportData)
        mimeType = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        fileExtension = 'docx'
        break
      case 'excel':
        content = generateExcelContent(reportData)
        mimeType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        fileExtension = 'xlsx'
        break
      case 'html':
        content = generateHTMLContent(reportData)
        mimeType = 'text/html'
        fileExtension = 'html'
        break
      default:
        content = generatePDFContent(reportData)
        mimeType = 'application/pdf'
        fileExtension = 'pdf'
    }
    
    // 创建并下载文件
    const blob = new Blob([content], { type: mimeType })
    const fileName = `${reportData.name}.${fileExtension}`
    
    if (window.navigator && (window.navigator as any).msSaveOrOpenBlob) {
      // IE/Edge
      (window.navigator as any).msSaveOrOpenBlob(blob, fileName)
    } else {
      // 其他浏览器
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      
      link.href = url
      link.download = fileName
      link.style.display = 'none'
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 清理URL对象
      setTimeout(() => URL.revokeObjectURL(url), 100)
    }
  } catch (error) {
    throw new Error('生成报告内容失败: ' + (error instanceof Error ? error.message : '未知错误'))
  }
}

// 生成PDF内容
const generatePDFContent = (reportData: ReportData): string => {
  // 简化的PDF内容生成，实际项目中可能需要使用专门的库如 jsPDF
  return `
    <html>
      <head>
        <title>${reportData.name}</title>
        <meta charset="UTF-8">
      </head>
      <body>
        <h1>${reportData.name}</h1>
        <p>生成时间: ${new Date().toLocaleString()}</p>
        <p>合规标准: ${getStandardName(reportData.standard)}</p>
        <p>格式: ${reportData.format}</p>
        <p>包含内容: ${reportData.includes.join(', ')}</p>
        <hr>
        <p>这是合规性报告的PDF版本内容</p>
      </body>
    </html>
  `
}

// 生成Word内容
const generateWordContent = (reportData: ReportData): string => {
  // 简化的Word内容生成
  return `
    <html xmlns:o='urn:schemas-microsoft-com:office:office' 
          xmlns:w='urn:schemas-microsoft-com:office:word' 
          xmlns='http://www.w3.org/TR/REC-html40'>
      <head>
        <title>${reportData.name}</title>
        <meta charset="UTF-8">
      </head>
      <body>
        <h1>${reportData.name}</h1>
        <p>生成时间: ${new Date().toLocaleString()}</p>
        <p>合规标准: ${getStandardName(reportData.standard)}</p>
        <p>格式: ${reportData.format}</p>
        <p>包含内容: ${reportData.includes.join(', ')}</p>
        <hr>
        <p>这是合规性报告的Word版本内容</p>
      </body>
    </html>
  `
}

// 生成Excel内容
const generateExcelContent = (reportData: ReportData): string => {
  // 简化的Excel内容生成
  return `
    <table border="1">
      <tr>
        <th>报告名称</th>
        <th>生成时间</th>
        <th>合规标准</th>
        <th>格式</th>
        <th>包含内容</th>
      </tr>
      <tr>
        <td>${reportData.name}</td>
        <td>${new Date().toLocaleString()}</td>
        <td>${getStandardName(reportData.standard)}</td>
        <td>${reportData.format}</td>
        <td>${reportData.includes.join(', ')}</td>
      </tr>
    </table>
  `
}

// 生成HTML内容
const generateHTMLContent = (reportData: ReportData): string => {
  // 简化的HTML内容生成
  return `
    <!DOCTYPE html>
    <html>
      <head>
        <title>${reportData.name}</title>
        <meta charset="UTF-8">
        <style>
          body { font-family: Arial, sans-serif; }
          h1 { color: #333; }
          table { border-collapse: collapse; width: 100%; }
          th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
          th { background-color: #f2f2f2; }
        </style>
      </head>
      <body>
        <h1>${reportData.name}</h1>
        <p>生成时间: ${new Date().toLocaleString()}</p>
        <p>合规标准: ${getStandardName(reportData.standard)}</p>
        <p>格式: ${reportData.format}</p>
        <p>包含内容: ${reportData.includes.join(', ')}</p>
        <hr>
        <p>这是合规性报告的HTML版本内容</p>
      </body>
    </html>
  `
}

// 获取标准名称
const getStandardName = (standard: string): string => {
  const standardMap: Record<string, string> = {
    'djbh': '等级保护2.0',
    'iso27001': 'ISO 27001',
    'pcidss': 'PCI DSS',
    'gdpr': 'GDPR'
  }
  return standardMap[standard] || standard
}

// 创建整改任务
const createTask = (row: NonCompliantItem) => {
  ElMessage.success(`已创建整改任务：${row.item}`)
}

// 忽略项目
const ignoreItem = async (row: NonCompliantItem) => {
  try {
    await ElMessageBox.confirm('确定要忽略此不合规项吗？', '提示', {
      type: 'warning'
    } as const)
    ElMessage.success('已忽略')
  } catch {
    // 用户取消
  }
}

// 初始化图表
const initChart = (): void => {
  // 销毁旧的图表实例（如果存在）
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  // 生成数据时创建副本，避免响应式数据循环引用
  const months: string[] = []
  const djbhData: number[] = []
  const iso27001Data: number[] = []
  const pcidssData: number[] = []
  const gdprData: number[] = []
  
  for (let i = 5; i >= 0; i--) {
    const date = new Date()
    date.setMonth(date.getMonth() - i)
    months.push(`${date.getMonth() + 1}月`)
    djbhData.push(Math.floor(Math.random() * 10) + 78)
    iso27001Data.push(Math.floor(Math.random() * 10) + 75)
    pcidssData.push(Math.floor(Math.random() * 10) + 73)
    gdprData.push(Math.floor(Math.random() * 10) + 77)
  }
  
  // 创建数据副本，避免循环引用
  const monthsCopy = [...months]
  const djbhDataCopy = [...djbhData]
  const iso27001DataCopy = [...iso27001Data]
  const pcidssDataCopy = [...pcidssData]
  const gdprDataCopy = [...gdprData]
  
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['等级保护2.0', 'ISO 27001', 'PCI DSS', 'GDPR']
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: monthsCopy // 使用副本
    },
    yAxis: {
      type: 'value',
      name: '合规分数',
      min: 60,
      max: 100
    },
    series: [
      {
        name: '等级保护2.0',
        type: 'line',
        smooth: true,
        data: djbhDataCopy, // 使用副本
        itemStyle: { color: '#409eff' }
      },
      {
        name: 'ISO 27001',
        type: 'line',
        smooth: true,
        data: iso27001DataCopy, // 使用副本
        itemStyle: { color: '#67c23a' }
      },
      {
        name: 'PCI DSS',
        type: 'line',
        smooth: true,
        data: pcidssDataCopy, // 使用副本
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: 'GDPR',
        type: 'line',
        smooth: true,
        data: gdprDataCopy, // 使用副本
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  
  trendChart?.setOption(option, true) // 使用 notMerge 参数避免合并旧配置
  
  // 使用防抖函数处理窗口大小调整
  const handleResize = (): void => {
    if (trendChart) {
      trendChart.resize()
    }
  }
  
  // 保存resize处理函数引用
  resizeHandler = handleResize
  
  // 添加窗口大小调整事件监听器
  window.addEventListener('resize', resizeHandler)
}

onMounted((): void => {
  initChart()
})

onUnmounted((): void => {
  // 清理resize事件监听器
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  
  // 销毁图表实例
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})
</script>

<style scoped>
.compliance-container {
  padding: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.stat-card {
  border-left: 4px solid #409eff;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.stat-rate {
  font-size: 12px;
  margin-top: 3px;
  font-weight: 500;
}

.standard-card {
  margin-bottom: 20px;
}

.standard-content {
  display: flex;
  gap: 30px;
  padding: 20px 0;
}

.compliance-score {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 200px;
}

.percentage-value {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.percentage-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.compliance-details {
  flex: 1;
}

.compliance-details h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.compliance-details p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.detail-card {
  background: #f5f7fa;
  border: none;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.detail-score {
  margin-left: 10px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-progress__text) {
  font-size: 12px !important;
}

:deep(.el-checkbox) {
  margin-right: 20px;
}
</style>
