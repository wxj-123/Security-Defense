import { http } from '@/utils/request'
import type { SecurityEvent, PageQuery, PageResult } from '@/types'

// 获取安全事件列表
export const getSecurityEvents = (params: PageQuery) => {
  return http.get<PageResult<SecurityEvent>>('/security/events', { params })
}

// 获取安全事件详情
export const getEventDetail = (eventId: string) => {
  try {
    // 对事件ID进行编码，避免特殊字符导致的问题
    const encodedEventId = encodeURIComponent(eventId);
    
    // 添加必要的请求头
    return http.get<SecurityEvent>(`/security/events/${encodedEventId}`, {
      headers: {
        'Content-Type': 'application/json',
        // 如果需要认证，可以取消注释下面这行
        // 'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      timeout: 5000
    });
  } catch (error) {
    console.error('获取事件详情失败:', error);
    throw new Error('获取事件详情失败，请检查网络或参数');
  }
}

// 处理安全事件
export const handleEvent = (id: number, action: string) => {
  return http.post(`/security/events/${id}/handle`, { action })
}

// 获取实时日志流
export const getLogStream = () => {
  return http.get('/security/logs/stream')
}

// 获取网络流量监控数据
export const getNetworkFlowMonitor = (params: any) => {
  return http.get('/security/network-flow', { params })
}

// 获取异常行为检测结果
export const getAnomalyDetection = () => {
  return http.get('/security/anomaly-detection')
}
