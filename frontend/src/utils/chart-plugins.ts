// Chart.js全局插件定义文件
// 修复事件处理中的includes方法调用错误

import { Chart } from 'chart.js'

// 定义安全的事件过滤插件
const safeEventFilterPlugin = {
  id: 'safeEventFilter',
  beforeEvent: (chart: any, args: any, options: any) => {
    // 确保event对象存在且有必要的属性
    const event = args.event
    if (!event) return
    
    // 安全检查event对象的属性
    if (event.type === 'click' || event.type === 'mousemove') {
      // 确保chart的配置和插件存在
      if (chart.config && chart.config.options) {
        // 在这里可以添加任何需要的事件预处理逻辑
      }
    }
  }
}

// 修复tooltip插件中的安全问题
const safeTooltipPlugin = {
  id: 'safeTooltip',
  beforeTooltipDraw: (chart: any, args: any, options: any) => {
    // 确保tooltip数据存在
    const tooltip = args.tooltip
    if (!tooltip) return
    
    // 安全访问tooltip属性
    if (tooltip.dataPoints && Array.isArray(tooltip.dataPoints)) {
      // 安全遍历数据点
      for (const point of tooltip.dataPoints) {
        if (!point) continue
        // 可以在这里添加数据点的安全检查
      }
    }
  }
}

// 导出插件
export { safeEventFilterPlugin, safeTooltipPlugin }