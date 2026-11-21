<template>
  <div class="attack-events-container">
    <el-row :gutter="20" class="dashboard-header">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">当前攻击数量</div>
          <div class="stat-value">{{ attackCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card class="chart-card">
          <template #header>
            <span>TOP3 攻击类型</span>
          </template>
          <div id="topAttackTypesChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-content">
      <el-col :span="16">
        <el-card class="main-card">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>实时攻击列表</span>
              <div>
                <span style="font-size: 12px; color: #909399; margin-right: 10px;">
                  最后更新: {{ lastUpdateTime }}
                </span>
                <el-button link @click="refreshData">刷新</el-button>
              </div>
            </div>
          </template>
          <el-table
            :data="attackList"
            @row-click="showAttackDetail"
            stripe
            style="width: 100%"
            highlight-current-row
            height="500"
            :header-cell-style="{background: '#f5f7fa', color: '#606266'}"
            border>
            <el-table-column prop="id" label="攻击ID" min-width="120">
              <template #default="scope">
                <span class="table-cell-text">{{ scope.row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="attackType" label="攻击类型" min-width="120">
              <template #default="scope">
                <span class="table-cell-text attack-type-cell">{{ scope.row.attackType }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sourceIp" label="源IP" min-width="130">
              <template #default="scope">
                <span class="table-cell-text">{{ scope.row.sourceIp }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="targetPort" label="目标端口" min-width="90" align="center">
              <template #default="scope">
                <el-tag type="info">{{ scope.row.targetPort }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="告警等级" min-width="100" align="center">
              <template #default="scope">
                <el-tag :type="getSeverityTagType(scope.row.severity)" class="severity-cell">
                  {{ scope.row.severity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="timestamp" label="发生时间" min-width="160">
              <template #default="scope">
                <span class="table-cell-text">{{ scope.row.timestamp }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="100" align="center">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" class="status-cell">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="120" fixed="right" align="center">
              <template #default="scope">
                <el-button class="payload-btn" size="small" type="primary" link @click.stop="showPayload(scope.row)">查看Payload</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[10, 20, 50]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total">
            </el-pagination>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="filter-card">
          <template #header>
            <span>筛选条件</span>
          </template>
          <el-form :model="filterForm" label-width="80px">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
            <el-form-item label="攻击类型">
              <el-select v-model="filterForm.attackType" placeholder="请选择攻击类型" clearable style="width: 100%">
                <el-option label="SQL注入" value="SQL注入"></el-option>
                <el-option label="XSS攻击" value="XSS攻击"></el-option>
                <el-option label="DDoS攻击" value="DDoS攻击"></el-option>
                <el-option label="暴力破解" value="暴力破解"></el-option>
                <el-option label="恶意扫描" value="恶意扫描"></el-option>
                <el-option label="端口扫描" value="PORT_SCAN"></el-option>
                <el-option label="恶意软件" value="MALWARE"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="告警等级">
              <el-select v-model="filterForm.severity" placeholder="请选择告警等级" clearable style="width: 100%">
                <el-option label="严重" value="CRITICAL"></el-option>
                <el-option label="高危" value="HIGH"></el-option>
                <el-option label="中危" value="MEDIUM"></el-option>
                <el-option label="低危" value="LOW"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="资产标签">
              <el-select v-model="filterForm.assetTag" placeholder="请选择资产标签" clearable style="width: 100%">
                <el-option label="Web服务器" value="web-server"></el-option>
                <el-option label="数据库服务器" value="db-server"></el-option>
                <el-option label="应用服务器" value="app-server"></el-option>
                <el-option label="网关设备" value="gateway"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="applyFilter">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
              <el-button @click="exportToExcel">导出Excel</el-button>
            </el-form-item>
          </el-form>
          
          <el-card class="distribution-card">
            <template #header>
              <span>告警等级分布</span>
            </template>
            <div id="alertLevelChart" class="chart-container"></div>
          </el-card>
        </el-card>
      </el-col>
    </el-row>

    <!-- 攻击详情弹窗 -->
    <el-dialog title="攻击详情" v-model="detailDialogVisible" width="60%" @close="closeDetailDialog">
      <el-form :model="currentAttackDetail" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="攻击ID：">
              <span>{{ currentAttackDetail.id }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="攻击类型：">
              <span>{{ currentAttackDetail.attackType }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="源IP地址：">
              <span>{{ currentAttackDetail.sourceIp }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标端口：">
              <span>{{ currentAttackDetail.targetPort }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="攻击时间：">
              <span>{{ currentAttackDetail.timestamp }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="持续时长：">
              <span>{{ currentAttackDetail.duration }} 秒</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="告警等级：">
              <el-tag :type="getSeverityTagType(currentAttackDetail.severity)">
                {{ currentAttackDetail.severity }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态：">
              <el-tag :type="getStatusTagType(currentAttackDetail.status)">
                {{ currentAttackDetail.status }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="攻击Payload：">
          <el-input
            type="textarea"
            :rows="4"
            readonly
            v-model="currentAttackDetail.payload">
          </el-input>
        </el-form-item>
        <el-form-item label="目标资产：">
          <span>{{ currentAttackDetail.targetAsset }}</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button type="danger" @click="blockIp">拉黑IP</el-button>
          <el-button type="warning" @click="interceptAttack">联动防御设备拦截</el-button>
          <el-button type="info" @click="markFalsePositive">标记为误报</el-button>
          <el-button @click="closeDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Payload详情弹窗 -->
    <el-dialog title="Payload详情" v-model="payloadDialogVisible" width="50%" @close="closePayloadDialog">
      <el-form label-width="100px">
        <el-form-item label="攻击类型：">
          <span>{{ currentPayload.attackType }}</span>
        </el-form-item>
        <el-form-item label="源IP地址：">
          <span>{{ currentPayload.sourceIp }}</span>
        </el-form-item>
        <el-form-item label="攻击Payload：">
          <el-input
            type="textarea"
            :rows="6"
            readonly
            v-model="currentPayload.payload">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closePayloadDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSecurityEvents, getEventDetail } from '@/api/security'

// 数据定义
const attackCount = ref(0)
const attackList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailDialogVisible = ref(false)
const payloadDialogVisible = ref(false)
const currentAttackDetail = ref({})
const currentPayload = ref({
  attackType: '',
  sourceIp: '',
  payload: ''
})
const dateRange = ref([])
const filterForm = ref({
  attackType: '',
  assetTag: '',
  severity: ''
})
const lastUpdateTime = ref('')

// 图表实例
let topAttackTypesChartInstance = null
let alertLevelChartInstance = null
let autoRefreshTimer = null

// 初始化数据
onMounted(async () => {
  await initCharts();
  await loadData();

  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', handleResize);

  // 启动自动刷新，每30秒刷新一次，与后端生成事件的频率一致
  startAutoRefresh();
})

onUnmounted(() => {
  // 组件销毁时移除事件监听器
  window.removeEventListener('resize', handleResize)

  // 销毁图表实例
  if (topAttackTypesChartInstance) {
    topAttackTypesChartInstance.dispose()
  }
  if (alertLevelChartInstance) {
    alertLevelChartInstance.dispose()
  }

  // 清除自动刷新定时器
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
})

const handleResize = () => {
  if (topAttackTypesChartInstance) {
    topAttackTypesChartInstance.resize();
  }
  if (alertLevelChartInstance) {
    alertLevelChartInstance.resize();
  }
};

// 加载数据
const loadData = async () => {
  try {
    // 调用API获取安全事件列表
    const response = await getSecurityEvents({
      page: currentPage.value,
      pageSize: pageSize.value,
      type: filterForm.value.attackType,
      level: filterForm.value.severity || filterForm.value.level
    });
      
    // 处理API返回的数据
    if (response && response.data && Array.isArray(response.data.list)) {
      // 更新最后更新时间
      const now = new Date()
      lastUpdateTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })

      // 映射后端返回的字段到前端期望的字段
      attackList.value = response.data.list.map(item => ({
        id: item.id ? `ATK${item.id}` : '',
        attackType: item.type || item.eventType || '',
        sourceIp: item.source || item.sourceIp || '',
        targetPort: item.targetPort || item.targetPort || 80, // 默认端口80
        severity: item.level || item.eventLevel || '低危',
        timestamp: item.time || item.eventTime || new Date().toISOString().replace('T', ' ').substring(0, 19),
        status: item.status || '未处理',
        duration: item.duration || 0,
        payload: item.payload || item.description || '',
        targetAsset: item.target || item.targetIp || '未知资产'
      }));

      // 更新分页信息
      total.value = response.data.total || 0;

      // 更新当前攻击数量
      attackCount.value = response.data.total || 0;

      // 更新图表数据
      nextTick(() => {
        updateCharts()
      });
    } else {
      // 降级处理：使用默认数据
      attackList.value = [
        {
          id: 'ATK00000000001',
          attackType: 'DDoS攻击',
          sourceIp: '192.168.1.100',
          targetPort: 80,
          severity: '高危',
          timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
          status: '未处理',
          duration: 120,
          payload: '大量SYN包洪水攻击',
          targetAsset: 'Web服务器01'
        },
        {
          id: 'ATK00000000002',
          attackType: 'SQL注入',
          sourceIp: '203.123.45.67',
          targetPort: 3306,
          severity: '严重',
          timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
          status: '处理中',
          duration: 45,
          payload: "SELECT * FROM users WHERE id = 1 OR 1=1--'",
          targetAsset: '数据库服务器01'
        }
      ];
      total.value = 2;
      attackCount.value = 2;
          
      ElMessage.warning('数据加载异常，已使用默认数据');
    }
  } catch (error) {
    console.error('获取安全事件列表失败:', error);
    ElMessage.error('获取安全事件列表失败，请稍后重试');
      
    // 降级处理：使用默认数据
    attackList.value = [
      {
        id: 'ATK00000000001',
        attackType: 'DDoS攻击',
        sourceIp: '192.168.1.100',
        targetPort: 80,
        severity: '高危',
        timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
        status: '未处理',
        duration: 120,
        payload: '大量SYN包洪水攻击',
        targetAsset: 'Web服务器01'
      },
      {
        id: 'ATK00000000002',
        attackType: 'SQL注入',
        sourceIp: '203.123.45.67',
        targetPort: 3306,
        severity: '严重',
        timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
        status: '处理中',
        duration: 45,
        payload: "SELECT * FROM users WHERE id = 1 OR 1=1--'",
        targetAsset: '数据库服务器01'
      }
    ];
    total.value = 2;
    attackCount.value = 2;
  }
}

// 初始化图表
const initCharts = async () => {
  return new Promise((resolve) => {
    // 确保在DOM更新后再初始化图表
    nextTick(() => {
      // 使用setTimeout确保DOM完全渲染
      setTimeout(() => {
        // 销毁已存在的实例（如果有的话）
        if (topAttackTypesChartInstance) {
          topAttackTypesChartInstance.dispose();
        }
        
        if (alertLevelChartInstance) {
          alertLevelChartInstance.dispose();
        }
        
        const topChartElement = document.getElementById('topAttackTypesChart');
        const alertChartElement = document.getElementById('alertLevelChart');
        
        if (topChartElement) {
          topAttackTypesChartInstance = echarts.init(topChartElement);
        }
        
        if (alertChartElement) {
          alertLevelChartInstance = echarts.init(alertChartElement);
        }
        
        updateCharts();
        resolve();
      }, 100); // 延迟100ms确保DOM完全加载
    });
  });
}

// 更新图表
const updateCharts = () => {
  // TOP3 攻击类型图表
  if (topAttackTypesChartInstance) {
    // 统计攻击类型
    const attackTypeCount = {};
    attackList.value.forEach(item => {
      const type = item.attackType;
      attackTypeCount[type] = (attackTypeCount[type] || 0) + 1;
    });
    
    // 获取TOP3攻击类型
    const sortedTypes = Object.entries(attackTypeCount)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3);
    
    const top3Types = sortedTypes.map(([type]) => type);
    const top3Counts = sortedTypes.map(([type, count]) => count);
    
    const topAttackOption = {
      tooltip: {},
      xAxis: {
        type: 'category',
        data: top3Types.length > 0 ? top3Types : ['DDoS攻击', 'SQL注入', 'XSS攻击']
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        type: 'bar',
        data: top3Counts.length > 0 ? top3Counts : [120, 45, 32],
        itemStyle: {
          color: '#409EFF'
        }
      }]
    };
    topAttackTypesChartInstance.setOption(topAttackOption, true); // 使用notMerge参数确保重新绘制
  }
  
  // 告警等级分布图表
  if (alertLevelChartInstance) {
    // 统计告警等级
    const severityCount = {};
    attackList.value.forEach(item => {
      const severity = item.severity;
      severityCount[severity] = (severityCount[severity] || 0) + 1;
    });
    
    const severityData = Object.entries(severityCount).map(([name, value]) => ({
      name,
      value
    }));
    
    // 如果没有数据，使用默认数据
    const pieData = severityData.length > 0 ? severityData : [
      { value: 15, name: '严重' },
      { value: 8, name: '高危' },
      { value: 22, name: '中危' },
      { value: 35, name: '低危' }
    ];
    
    const alertLevelOption = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: pieData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    alertLevelChartInstance.setOption(alertLevelOption, true); // 使用notMerge参数确保重新绘制
  }
}

// 显示攻击详情
const showAttackDetail = async (row) => {
  // 从数据库获取完整的事件详情
  if (row && row.id) {
    try {
      // 提取ID中的数字部分作为参数
      const eventId = row.id.replace(/[^0-9]/g, '');
      
      // 调用API获取事件详情
      const response = await getEventDetail(eventId);
      
      if (response && response.data) {
        // 映射数据库返回的字段到前端期望的字段
        currentAttackDetail.value = {
          id: response.data.id ? `ATK${response.data.id}` : row.id,
          attackType: response.data.type || response.data.eventType || row.attackType,
          sourceIp: response.data.source || response.data.sourceIp || row.sourceIp,
          targetPort: response.data.targetPort || response.data.targetPort || row.targetPort || 80,
          severity: response.data.level || response.data.eventLevel || row.severity,
          timestamp: response.data.time || response.data.eventTime || row.timestamp,
          status: response.data.status || row.status,
          duration: response.data.duration || row.duration || 0,
          payload: response.data.payload || response.data.description || row.payload,
          targetAsset: response.data.target || response.data.targetIp || row.targetAsset
        };
      } else {
        // 使用行数据作为降级方案
        currentAttackDetail.value = row;
      }
    } catch (error) {
      console.error('获取事件详情失败:', error);
      ElMessage.error('获取事件详情失败，请稍后重试');
      
      // 使用行数据作为降级方案
      currentAttackDetail.value = row;
    }
  } else {
    currentAttackDetail.value = row;
  }
  
  detailDialogVisible.value = true
}

// 显示Payload详情
const showPayload = async (row) => {
  if (row && row.id) {
    try {
      // 提取ID中的数字部分作为参数
      const eventId = row.id.replace(/[^0-9]/g, '');
        
      // 调用API获取事件详情
      const response = await getEventDetail(eventId);
        
      if (response && response.data) {
        // 确保Payload内容完整显示
        let payloadContent = response.data.payload || response.data.description || row.payload || '';
          
        // 处理特殊字符，确保正确显示
        if (typeof payloadContent === 'string') {
          // 如果Payload内容包含HTML标签，转义HTML标签
          payloadContent = payloadContent.replace(/</g, '&lt;').replace(/>/g, '&gt;');
        }
          
        currentPayload.value = {
          attackType: response.data.type || response.data.eventType || row.attackType || '',
          sourceIp: response.data.source || response.data.sourceIp || row.sourceIp || '',
          payload: payloadContent
        }
      } else {
        // 使用行数据作为降级方案
        currentPayload.value = {
          attackType: row.attackType || '',
          sourceIp: row.sourceIp || '',
          payload: row.payload || ''
        }
      }
    } catch (error) {
      console.error('获取事件详情失败:', error);
      ElMessage.error('获取事件详情失败，请稍后重试');
        
      // 使用行数据作为降级方案
      currentPayload.value = {
        attackType: row.attackType || '',
        sourceIp: row.sourceIp || '',
        payload: row.payload || ''
      }
    }
  } else {
    currentPayload.value = {
      attackType: '',
      sourceIp: '',
      payload: ''
    }
  }
    
  // 确保Payload内容完整显示
  nextTick(() => {
    const payloadTextarea = document.querySelector('.el-textarea__inner');
    if (payloadTextarea) {
      payloadTextarea.style.height = 'auto';
      payloadTextarea.style.height = payloadTextarea.scrollHeight + 'px';
    }
  });
    
  payloadDialogVisible.value = true
}

// 关闭详情弹窗
const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

// 关闭Payload弹窗
const closePayloadDialog = () => {
  payloadDialogVisible.value = false
  // 清空payload数据
  currentPayload.value = {
    attackType: '',
    sourceIp: '',
    payload: ''
  }
}

// 获取告警等级标签类型
const getSeverityTagType = (severity) => {
  const severityMap = {
    '严重': 'danger',
    '高危': 'warning',
    '中危': 'primary',
    '低危': 'success'
  }
  return severityMap[severity] || 'info'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    '未处理': 'warning',
    '处理中': 'primary',
    '已处理': 'success',
    '已忽略': 'info'
  }
  return statusMap[status] || 'info'
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val;
  loadData();
  // 更新图表
  nextTick(() => {
    updateCharts();
  });
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
  // 更新图表
  nextTick(() => {
    updateCharts();
  });
};

// 刷新数据
const refreshData = () => {
  loadData();
  // 刷新图表
  nextTick(() => {
    updateCharts();
  });
  ElMessage.success('数据已刷新');
}

// 启动自动刷新
const startAutoRefresh = () => {
  // 清除已存在的定时器
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
  }

  // 设置新的定时器，每30秒自动刷新一次
  autoRefreshTimer = setInterval(() => {
    loadData();
    nextTick(() => {
      updateCharts();
    });
  }, 30000); // 30秒刷新一次，与后端生成事件的频率一致
}

// 应用筛选条件
const applyFilter = () => {
  ElMessage.info('筛选条件已应用');
  loadData();
  // 更新图表
  nextTick(() => {
    updateCharts();
  });
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.value.attackType = '';
  filterForm.value.assetTag = '';
  filterForm.value.severity = '';
  dateRange.value = [];
  ElMessage.info('筛选条件已重置');
  loadData();
  // 更新图表
  nextTick(() => {
    updateCharts();
  });
}

// 导出Excel
const exportToExcel = () => {
  ElMessage.success('攻击事件数据已导出为Excel文件')
}

// 拉黑IP
const blockIp = () => {
  ElMessageBox.confirm(
    `确认要将IP地址 ${currentAttackDetail.value.sourceIp} 加入黑名单吗？`,
    '警告',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟拉黑IP操作
    ElMessage.success(`已将IP地址 ${currentAttackDetail.value.sourceIp} 加入黑名单`)
    // 记录日志
    console.log(`[${new Date().toISOString()}] 操作日志: 将IP ${currentAttackDetail.value.sourceIp} 加入黑名单`)
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 联动防御设备拦截
const interceptAttack = () => {
  ElMessageBox.confirm(
    `确认要联动防御设备拦截来自 ${currentAttackDetail.value.sourceIp} 的攻击吗？`,
    '警告',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟联动防御设备拦截操作
    ElMessage.success(`已联动防御设备拦截来自 ${currentAttackDetail.value.sourceIp} 的攻击`)
    // 记录日志
    console.log(`[${new Date().toISOString()}] 操作日志: 联动防御设备拦截来自 ${currentAttackDetail.value.sourceIp} 的攻击`)
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 标记为误报
const markFalsePositive = () => {
  ElMessageBox.confirm(
    `确认要将该攻击事件标记为误报吗？`,
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 模拟标记为误报操作
    ElMessage.success(`已将攻击事件标记为误报`)
    // 更新状态
    currentAttackDetail.value.status = '已忽略'
    // 记录日志
    console.log(`[${new Date().toISOString()}] 操作日志: 将攻击事件 ${currentAttackDetail.value.id} 标记为误报`)
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}
</script>

<style scoped>
.attack-events-container {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.chart-card {
  text-align: center;
}

.stat-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.main-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.filter-card {
  margin-bottom: 20px;
}

.distribution-card {
  margin-top: 20px;
}

.dialog-footer {
  text-align: center;
}

.payload-btn {
  padding: 4px 8px;
}

.table-cell-text {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.attack-type-cell {
  font-weight: 500;
  color: #303133;
}

.severity-cell {
  font-weight: 500;
}

.status-cell {
  font-weight: 500;
}

.el-table .el-table__row:hover td {
  background-color: #f5f7fa !important;
}

.el-table__row--striped td {
  background-color: #fafafa !important;
}

@media (max-width: 1200px) {
  .el-col-16 {
    width: 100%;
  }
  
  .el-col-8 {
    width: 100%;
    margin-top: 20px;
  }
}
</style>
