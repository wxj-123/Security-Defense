import axios from 'axios'
import { http, request } from '@/utils/request'
import type { AxiosResponse } from 'axios'

// 获取安全事件统计报表
export const getEventStatistics = (params: any) => {
  return http.get('/report/event-statistics', { params })
}

// 获取威胁情报分析
export const getThreatIntelligence = (params: any) => {
  return http.get('/report/threat-intelligence', { params })
}

// 获取合规性报告
export const getComplianceReport = (params: any) => {
  return http.get('/report/compliance', { params })
}

// 生成自定义报表
export const generateCustomReport = (data: any) => {
  return http.post('/report/custom', data)
}

// 导出报表
export const exportReport = (reportId: number, format: 'pdf' | 'excel' | 'csv') => {
  return request.get(`/report/${reportId}/export`, {
    params: { format },
    responseType: 'blob'
  })
}

// 导出事件统计数据
export const exportEventStatistics = (data: any, format: 'pdf' | 'excel' | 'csv' = 'excel'): Promise<AxiosResponse<Blob>> => {
  return axios.post('/api/report/event-statistics/export', data, {
    params: { format },
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 获取报表列表
export const getReportList = (params: any) => {
  return http.get('/report/list', { params })
}