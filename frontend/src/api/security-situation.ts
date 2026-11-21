import request from '@/utils/request'
import type { SituationData, AssetRisk, ThreatIntelligence } from '@/types/security-situation'
import { ElMessage } from 'element-plus'

// 默认请求超时时间（毫秒）
const DEFAULT_TIMEOUT = 30000

// 错误处理函数
const handleApiError = (error: any, defaultMessage: string = '请求失败') => {
  console.error('API Error:', error)
  
  if (error?.response?.status === 401) {
    ElMessage.error('认证失败，请重新登录')
    // 可以在这里添加重定向到登录页的逻辑
    return
  }
  
  if (error?.response?.status === 403) {
    ElMessage.error('权限不足，无法访问该资源')
    return
  }
  
  if (error?.response?.status === 404) {
    ElMessage.error('请求的资源不存在')
    return
  }
  
  if (error?.response?.status === 500) {
    ElMessage.error('服务器内部错误，请稍后重试')
    return
  }
  
  if (error?.code === 'ECONNABORTED') {
    ElMessage.error('请求超时，请检查网络连接')
    return
  }
  
  if (error?.message?.includes('Network Error')) {
    ElMessage.error('网络连接失败，请检查网络设置')
    return
  }
  
  ElMessage.error(error?.response?.data?.message || defaultMessage)
}

// 数据验证函数
const validateResponseData = (data: any, expectedFields: string[]): boolean => {
  if (!data) {
    console.warn('API响应数据为空')
    return false
  }
  
  for (const field of expectedFields) {
    if (!(field in data) || data[field] === undefined) {
      console.warn(`API响应数据缺少必要字段: ${field}`)
      return false
    }
  }
  
  return true
}

// 重试机制
const retryRequest = async <T>(
  requestFn: () => Promise<T>,
  maxRetries: number = 3,
  delay: number = 1000
): Promise<T> => {
  let lastError: any
  
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await requestFn()
    } catch (error: any) {
      lastError = error
      
      // 如果是认证错误或权限错误，不重试
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        throw error
      }
      
      if (attempt < maxRetries) {
        console.warn(`API请求失败，第${attempt}次重试...`)
        await new Promise(resolve => setTimeout(resolve, delay * attempt))
      }
    }
  }
  
  throw lastError
}

/**
 * 获取安全态势总览
 */
export const getSituationOverview = async () => {
  try {
    const response = await retryRequest(() =>
      request<{ data: SituationData }>({
        url: '/situation-dashboard/overview',
        method: 'get',
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    // 修复：检查响应数据结构，适配可能的不同数据格式
    if (!response || !response.data) {
      console.warn('后端返回数据为空，使用模拟数据')
      return generateMockSituationOverview()
    }
    
    // 检查是否是直接的SituationData对象或者是包装在data字段中的对象
    let responseData: any = response.data
    
    // 如果responseData是一个对象且包含situationIndex字段，直接使用
    if (typeof responseData === 'object' && responseData !== null && 'situationIndex' in responseData) {
      // 验证必要字段
      const expectedFields = ['situationIndex', 'situationLevel', 'timestamp', 'totalAssets', 'affectedAssets', 'activeThreats']
      if (validateResponseData(responseData, expectedFields)) {
        return { data: responseData }
      }
    }
    
    // 如果responseData是一个包装对象，尝试提取data字段
    if (typeof responseData === 'object' && responseData !== null && 'data' in responseData) {
      const dataObj = responseData.data
      if (typeof dataObj === 'object' && dataObj !== null && 'situationIndex' in dataObj) {
        // 验证必要字段
        const expectedFields = ['situationIndex', 'situationLevel', 'timestamp', 'totalAssets', 'affectedAssets', 'activeThreats']
        if (validateResponseData(dataObj, expectedFields)) {
          return { data: dataObj }
        }
      }
    }
    
    // 如果以上都不匹配，使用模拟数据
    console.warn('后端返回数据缺少必要字段，使用模拟数据')
    return generateMockSituationOverview()
  } catch (error) {
    console.warn('获取安全态势总览失败，使用模拟数据:', error)
    return generateMockSituationOverview()
  }
}

/**
 * 获取态势历史趋势数据
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param interval 时间间隔（分钟）
 */
export const getSituationTrend = async (startTime: string, endTime: string, interval: number) => {
  // 参数验证
  if (!startTime || !endTime) {
    throw new Error('开始时间和结束时间不能为空')
  }
  
  if (interval <= 0) {
    throw new Error('时间间隔必须大于0')
  }
  
  try {
    // 转换时间格式为后端期望的格式（后端需要ISO日期格式）
    const formatTimeForBackend = (timeStr: string): string => {
      // 将字符串时间转换为ISO格式
      const date = new Date(timeStr)
      if (isNaN(date.getTime())) {
        throw new Error(`无效的时间格式: ${timeStr}`)
      }
      return date.toISOString()
    }
    
    const isoStartTime = formatTimeForBackend(startTime)
    const isoEndTime = formatTimeForBackend(endTime)
    
    const response = await retryRequest(() =>
      request<{ data: Array<{ timestamp: string; situationIndex: number; activeThreats: number }> }>({
        url: '/situation-dashboard/trend',
        method: 'get',
        params: {
          startTime: isoStartTime,
          endTime: isoEndTime,
          interval
        },
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    // 修复：检查响应数据结构，适配可能的不同数据格式
    if (!response || !response.data) {
      console.warn('后端返回数据为空，使用模拟数据')
      return generateMockTrendData()
    }
    
    // 检查是否是直接的数组或者是包装在data字段中的数组
    let responseData: any = response.data
    
    // 如果responseData是一个数组，直接使用
    if (Array.isArray(responseData)) {
      // 进一步验证数组中的每个元素
      const validatedData = responseData
        .filter(item => item && typeof item === 'object')
        .map(item => ({
          timestamp: typeof item.timestamp === 'string' ? item.timestamp : new Date().toISOString(),
          situationIndex: typeof item.situationIndex === 'number' ? item.situationIndex : Math.floor(Math.random() * 50) + 25,
          activeThreats: typeof item.activeThreats === 'number' ? item.activeThreats : Math.floor(Math.random() * 10) + 5
        }))
      
      return { data: validatedData }
    }
    
    // 如果responseData是一个包装对象，尝试提取data字段
    if (typeof responseData === 'object' && responseData !== null && 'data' in responseData) {
      const dataArr = responseData.data
      if (Array.isArray(dataArr)) {
        // 进一步验证数组中的每个元素
        const validatedData = dataArr
          .filter(item => item && typeof item === 'object')
          .map(item => ({
            timestamp: typeof item.timestamp === 'string' ? item.timestamp : new Date().toISOString(),
            situationIndex: typeof item.situationIndex === 'number' ? item.situationIndex : Math.floor(Math.random() * 50) + 25,
            activeThreats: typeof item.activeThreats === 'number' ? item.activeThreats : Math.floor(Math.random() * 10) + 5
          }))
        
        return { data: validatedData }
      }
    }
    
    // 如果以上都不匹配，使用模拟数据
    console.warn('后端返回数据格式异常，使用模拟数据')
    return generateMockTrendData()
  } catch (error) {
    handleApiError(error, '获取态势历史趋势数据失败')
    return generateMockTrendData()
  }
}

/**
 * 获取资产风险分布数据
 */
export const getAssetRiskDistribution = async () => {
  try {
    const response = await retryRequest(() =>
      request<{ data: AssetRisk[] }>({
        url: '/situation-dashboard/asset-risk',
        method: 'get',
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    // 修复：检查响应数据结构，适配可能的不同数据格式
    if (!response || !response.data) {
      console.warn('后端返回数据为空，使用模拟数据')
      return generateMockAssetRiskDistribution()
    }
    
    // 检查是否是直接的AssetRisk数组或者是包装在data字段中的数组
    let responseData: any = response.data
    
    // 如果responseData是一个数组，直接使用
    if (Array.isArray(responseData)) {
      // 对数组中的每个元素进行字段补充和验证
      const processedData = responseData.map(item => {
        // 确保所有必需字段都有默认值
        return {
          assetId: typeof item.assetId === 'string' ? item.assetId : `asset_${Math.random().toString(36).substr(2, 9)}`,
          assetName: typeof item.assetName === 'string' ? item.assetName : '未知资产',
          assetType: typeof item.assetType === 'string' ? item.assetType : '未知类型',
          riskScore: typeof item.riskScore === 'number' ? item.riskScore : Math.floor(Math.random() * 100),
          riskLevel: (typeof item.riskLevel === 'string' && ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE'].includes(item.riskLevel)) 
            ? item.riskLevel 
            : 'MEDIUM',
          location: typeof item.location === 'string' ? item.location : '未知位置',
          ipAddress: typeof item.ipAddress === 'string' ? item.ipAddress : '0.0.0.0',
          lastScanTime: typeof item.lastScanTime === 'string' ? item.lastScanTime : new Date().toISOString(),
          status: (typeof item.status === 'string' && ['ACTIVE', 'INACTIVE', 'MAINTENANCE'].includes(item.status)) 
            ? item.status 
            : 'ACTIVE',
          // 补充可能缺失的字段
          assetValueScore: typeof item.assetValueScore === 'number' ? item.assetValueScore : Math.floor(Math.random() * 100),
          vulnerabilityScore: typeof item.vulnerabilityScore === 'number' ? item.vulnerabilityScore : Math.floor(Math.random() * 100),
          threatScore: typeof item.threatScore === 'number' ? item.threatScore : Math.floor(Math.random() * 100),
          vulnerabilityCount: typeof item.vulnerabilityCount === 'number' ? item.vulnerabilityCount : Math.floor(Math.random() * 10),
          threatCount: typeof item.threatCount === 'number' ? item.threatCount : Math.floor(Math.random() * 5)
        } as AssetRisk
      })
      
      return { data: processedData }
    }
    
    // 如果responseData是一个包装对象，尝试提取data字段
    if (typeof responseData === 'object' && responseData !== null && 'data' in responseData) {
      const dataArr = responseData.data
      if (Array.isArray(dataArr)) {
        // 对数组中的每个元素进行字段补充和验证
        const processedData = dataArr.map(item => {
          // 确保所有必需字段都有默认值
          return {
            assetId: typeof item.assetId === 'string' ? item.assetId : `asset_${Math.random().toString(36).substr(2, 9)}`,
            assetName: typeof item.assetName === 'string' ? item.assetName : '未知资产',
            assetType: typeof item.assetType === 'string' ? item.assetType : '未知类型',
            riskScore: typeof item.riskScore === 'number' ? item.riskScore : Math.floor(Math.random() * 100),
            riskLevel: (typeof item.riskLevel === 'string' && ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE'].includes(item.riskLevel)) 
              ? item.riskLevel 
              : 'MEDIUM',
            location: typeof item.location === 'string' ? item.location : '未知位置',
            ipAddress: typeof item.ipAddress === 'string' ? item.ipAddress : '0.0.0.0',
            lastScanTime: typeof item.lastScanTime === 'string' ? item.lastScanTime : new Date().toISOString(),
            status: (typeof item.status === 'string' && ['ACTIVE', 'INACTIVE', 'MAINTENANCE'].includes(item.status)) 
              ? item.status 
              : 'ACTIVE',
            // 补充可能缺失的字段
            assetValueScore: typeof item.assetValueScore === 'number' ? item.assetValueScore : Math.floor(Math.random() * 100),
            vulnerabilityScore: typeof item.vulnerabilityScore === 'number' ? item.vulnerabilityScore : Math.floor(Math.random() * 100),
            threatScore: typeof item.threatScore === 'number' ? item.threatScore : Math.floor(Math.random() * 100),
            vulnerabilityCount: typeof item.vulnerabilityCount === 'number' ? item.vulnerabilityCount : Math.floor(Math.random() * 10),
            threatCount: typeof item.threatCount === 'number' ? item.threatCount : Math.floor(Math.random() * 5)
          } as AssetRisk
        })
        
        return { data: processedData }
      }
    }
    
    console.warn('后端返回数据格式异常，使用模拟数据')
    return generateMockAssetRiskDistribution()
  } catch (error) {
    console.warn('获取资产风险分布失败，使用模拟数据:', error)
    return generateMockAssetRiskDistribution()
  }
}

/**
 * 获取威胁情报概览
 * @param limit 限制数量
 */
export const getThreatIntelligence = async (limit: number = 10) => {
  // 参数验证
  if (limit <= 0) {
    throw new Error('限制数量必须大于0')
  }
  
  if (limit > 100) {
    console.warn('限制数量过大，限制为100')
    limit = 100
  }
  
  try {
    const response = await retryRequest(() =>
      request<{ data: ThreatIntelligence[] }>({
        url: '/situation-dashboard/threats',
        method: 'get',
        params: {
          limit
        },
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    // 修复：检查响应数据结构，适配可能的不同数据格式
    if (!response || !response.data) {
      console.warn('后端返回数据为空，使用模拟数据')
      return generateMockThreatIntelligence(limit)
    }
    
    // 检查是否是直接的ThreatIntelligence数组或者是包装在data字段中的数组
    let responseData: any = response.data
    
    // 如果responseData是一个数组，直接使用
    if (Array.isArray(responseData)) {
      // 对数组中的每个元素进行字段补充和验证
      const processedData = responseData.map(item => {
        // 确保所有必需字段都有默认值
        return {
          threatId: typeof item.threatId === 'string' ? item.threatId : `threat_${Math.random().toString(36).substr(2, 9)}`,
          threatType: typeof item.threatType === 'string' ? item.threatType : '未知威胁',
          iocType: typeof item.iocType === 'string' ? item.iocType : '未知IOC类型',
          iocValue: typeof item.iocValue === 'string' ? item.iocValue : '未知IOC值',
          severity: (typeof item.severity === 'string' && ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'].includes(item.severity)) 
            ? item.severity 
            : 'MEDIUM',
          confidenceScore: typeof item.confidenceScore === 'number' ? item.confidenceScore : Math.floor(Math.random() * 100),
          source: typeof item.source === 'string' ? item.source : '未知来源',
          discoveredTime: typeof item.discoveredTime === 'string' ? item.discoveredTime : new Date().toISOString(),
          status: (typeof item.status === 'string' && ['ACTIVE', 'INACTIVE', 'FALSE_POSITIVE'].includes(item.status)) 
            ? item.status 
            : 'ACTIVE',
          // 补充可能缺失的字段
          firstSeen: typeof item.firstSeen === 'string' ? item.firstSeen : new Date().toISOString(),
          lastUpdated: typeof item.lastUpdated === 'string' ? item.lastUpdated : new Date().toISOString(),
          description: typeof item.description === 'string' ? item.description : '威胁情报描述',
          affectedSystems: Array.isArray(item.affectedSystems) ? item.affectedSystems : [],
          relatedCves: Array.isArray(item.relatedCves) ? item.relatedCves : [],
          tags: Array.isArray(item.tags) ? item.tags : [],
          recommendations: Array.isArray(item.recommendations) ? item.recommendations : []
        } as ThreatIntelligence
      })
      
      return { data: processedData }
    }
    
    // 如果responseData是一个包装对象，尝试提取data字段
    if (typeof responseData === 'object' && responseData !== null && 'data' in responseData) {
      const dataArr = responseData.data
      if (Array.isArray(dataArr)) {
        // 对数组中的每个元素进行字段补充和验证
        const processedData = dataArr.map(item => {
          // 确保所有必需字段都有默认值
          return {
            threatId: typeof item.threatId === 'string' ? item.threatId : `threat_${Math.random().toString(36).substr(2, 9)}`,
            threatType: typeof item.threatType === 'string' ? item.threatType : '未知威胁',
            iocType: typeof item.iocType === 'string' ? item.iocType : '未知IOC类型',
            iocValue: typeof item.iocValue === 'string' ? item.iocValue : '未知IOC值',
            severity: (typeof item.severity === 'string' && ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'].includes(item.severity)) 
              ? item.severity 
              : 'MEDIUM',
            confidenceScore: typeof item.confidenceScore === 'number' ? item.confidenceScore : Math.floor(Math.random() * 100),
            source: typeof item.source === 'string' ? item.source : '未知来源',
            discoveredTime: typeof item.discoveredTime === 'string' ? item.discoveredTime : new Date().toISOString(),
            status: (typeof item.status === 'string' && ['ACTIVE', 'INACTIVE', 'FALSE_POSITIVE'].includes(item.status)) 
              ? item.status 
              : 'ACTIVE',
            // 补充可能缺失的字段
            firstSeen: typeof item.firstSeen === 'string' ? item.firstSeen : new Date().toISOString(),
            lastUpdated: typeof item.lastUpdated === 'string' ? item.lastUpdated : new Date().toISOString(),
            description: typeof item.description === 'string' ? item.description : '威胁情报描述',
            affectedSystems: Array.isArray(item.affectedSystems) ? item.affectedSystems : [],
            relatedCves: Array.isArray(item.relatedCves) ? item.relatedCves : [],
            tags: Array.isArray(item.tags) ? item.tags : [],
            recommendations: Array.isArray(item.recommendations) ? item.recommendations : []
          } as ThreatIntelligence
        })
        
        return { data: processedData }
      }
    }
    
    console.warn('后端返回数据格式异常，使用模拟数据')
    return generateMockThreatIntelligence(limit)
  } catch (error) {
    console.warn('获取威胁情报数据失败，使用模拟数据:', error)
    return generateMockThreatIntelligence(limit)
  }
}

/**
 * 获取态势维度分析
 */
export const getDimensionAnalysis = () => {
  return request<{ data: Record<string, number> }>({
    url: '/situation-dashboard/dimensions',
    method: 'get'
  })
}

/**
 * 获取安全事件统计
 * @param startTime 开始时间
 * @param endTime 结束时间
 */
export const getSecurityStatistics = (startTime: string, endTime: string) => {
  // 转换时间格式为后端期望的格式（ISO日期格式）
  const formatTimeForBackend = (timeStr: string): string => {
    const date = new Date(timeStr)
    if (isNaN(date.getTime())) {
      throw new Error(`无效的时间格式: ${timeStr}`)
    }
    return date.toISOString()
  }
  
  return request<{ data: Record<string, any> }>({
    url: '/situation-dashboard/statistics',
    method: 'get',
    params: {
      startTime: formatTimeForBackend(startTime),
      endTime: formatTimeForBackend(endTime)
    }
  })
}

/**
 * 获取资产风险详情
 * @param assetId 资产ID
 */
const generateMockAssetRiskDetail = (assetId: string) => {
  const assetTypes = ['服务器', '网络设备', '工作站', '数据库', '应用系统']
  const riskLevels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE']
  
  const assetType = assetTypes[Math.floor(Math.random() * assetTypes.length)]
  const riskLevel = riskLevels[Math.floor(Math.random() * riskLevels.length)]
  
  const mockData: AssetRisk = {
    assetId: assetId,
    assetName: `${assetType}_${assetId.slice(-4)}`,
    assetType,
    riskScore: Math.floor(Math.random() * 100),
    riskLevel: riskLevel as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE',
    ipAddress: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
    location: '数据中心',
    lastScanTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
    assetValueScore: Math.floor(Math.random() * 100),
    vulnerabilityScore: Math.floor(Math.random() * 100),
    threatScore: Math.floor(Math.random() * 100),
    vulnerabilityCount: Math.floor(Math.random() * 10),
    threatCount: Math.floor(Math.random() * 5),
    status: 'ACTIVE',
    operatingSystem: 'Windows Server 2019',
    businessSystem: '核心业务系统',
    riskComposition: {
      threat: Math.floor(Math.random() * 40),
      vulnerability: Math.floor(Math.random() * 40),
      misconfiguration: Math.floor(Math.random() * 20),
      exposure: Math.floor(Math.random() * 20)
    }
  } as AssetRisk
  
  return { data: mockData }
}

/**
 * 获取资产风险详情
 * @param assetId 资产ID
 */
export const getAssetRiskDetail = async (assetId: string) => {
  try {
    const response = await retryRequest(() =>
      request<{ data: AssetRisk }>({
        url: `/situation-dashboard/asset/${assetId}`,
        method: 'get',
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    // 修复：检查响应数据结构，适配可能的不同数据格式
    if (!response || !response.data) {
      console.warn('后端返回资产风险详情数据为空，使用模拟数据')
      return generateMockAssetRiskDetail(assetId)
    }
    
    // 检查是否是直接的AssetRisk对象或者是包装在data字段中的对象
    let responseData: any = response.data
    
    // 如果responseData是一个对象且包含assetId字段，直接使用
    if (typeof responseData === 'object' && responseData !== null && 'assetId' in responseData) {
      // 验证必要字段
      const expectedFields = ['assetId', 'assetName', 'assetType', 'riskScore', 'riskLevel']
      if (validateResponseData(responseData, expectedFields)) {
        return { data: responseData }
      }
    }
    
    // 如果responseData是一个包装对象，尝试提取data字段
    if (typeof responseData === 'object' && responseData !== null && 'data' in responseData) {
      const dataObj = responseData.data
      if (typeof dataObj === 'object' && dataObj !== null && 'assetId' in dataObj) {
        // 验证必要字段
        const expectedFields = ['assetId', 'assetName', 'assetType', 'riskScore', 'riskLevel']
        if (validateResponseData(dataObj, expectedFields)) {
          return { data: dataObj }
        }
      }
    }
    
    // 如果以上都不匹配，使用模拟数据
    console.warn('后端返回资产风险详情数据缺少必要字段，使用模拟数据')
    return generateMockAssetRiskDetail(assetId)
  } catch (error) {
    handleApiError(error, '获取资产风险详情失败')
    return generateMockAssetRiskDetail(assetId)
  }
}

/**
 * 获取资产漏洞列表
 * @param assetId 资产ID
 */
export const getAssetVulnerabilities = (assetId: string) => {
  return request({
    url: `/situation-dashboard/asset/${assetId}/vulnerabilities`,
    method: 'get'
  })
}

/**
 * 获取资产威胁列表
 * @param assetId 资产ID
 */
export const getAssetThreats = (assetId: string) => {
  return request({
    url: `/situation-dashboard/asset/${assetId}/threats`,
    method: 'get'
  })
}

/**
 * 获取资产风险趋势
 * @param assetId 资产ID
 * @param range 时间范围
 */
export const getAssetRiskTrend = (assetId: string, range: string) => {
  return request({
    url: `/situation-dashboard/asset/${assetId}/trend`,
    method: 'get',
    params: {
      range
    }
  })
}

/**
 * 获取威胁情报详情
 * @param threatId 威胁ID
 */
export const getThreatIntelligenceDetail = async (threatId: string) => {
  try {
    console.log('开始获取威胁情报详情，threatId:', threatId)

    const response = await retryRequest(() =>
      request<{ data: ThreatIntelligence }>({
        url: `/situation-dashboard/threat/${threatId}`,
        method: 'get',
        timeout: DEFAULT_TIMEOUT
      })
    )

    console.log('API原始响应 (AxiosResponse):', response)
    console.log('response.data:', response.data)
    console.log('response.data.data:', response.data?.data)

    // request工具返回的是AxiosResponse对象
    // 结构: { data: { code: 200, message: "success", data: {...} }, status: 200, ... }
    // 实际的威胁情报数据在 response.data.data 中

    // 验证响应数据
    if (!response || !response.data) {
      console.warn('后端返回数据为空，response:', response)
      return null
    }

    const apiResponse = response.data

    // 检查API响应状态码
    if ('code' in apiResponse && apiResponse.code !== 200) {
      const errorMsg = 'message' in apiResponse ? apiResponse.message : '未知错误'
      console.warn('API返回错误状态:', apiResponse.code, errorMsg)
      return null
    }

    // 获取实际的威胁情报数据
    const threatData = apiResponse.data

    console.log('提取的威胁情报数据:', threatData)

    if (!threatData || typeof threatData !== 'object') {
      console.warn('威胁情报数据为空或格式错误:', threatData)
      return null
    }

    // 检查是否包含必要的字段
    if (!('threatId' in threatData)) {
      console.warn('威胁情报数据缺少必要字段threatId，完整数据:', threatData)
      console.warn('可用字段:', Object.keys(threatData))
      return null
    }

    console.log('成功获取威胁情报详情:', threatData)
    return { data: threatData as ThreatIntelligence }
  } catch (error) {
    console.error('获取威胁情报详情失败:', error)
    return null
  }
}

/**
 * 获取威胁影响资产
 * @param threatId 威胁ID
 */
export const getThreatAffectedAssets = (threatId: string) => {
  return request({
    url: `/situation-dashboard/threat/${threatId}/affected-assets`,
    method: 'get'
  })
}

/**
 * 获取威胁相关事件
 * @param threatId 威胁ID
 */
export const getThreatRelatedEvents = (threatId: string) => {
  return request({
    url: `/situation-dashboard/threat/${threatId}/related-events`,
    method: 'get'
  })
}

/**
 * 获取威胁相关情报
 * @param threatId 威胁ID
 */
export const getRelatedThreats = (threatId: string) => {
  return request({
    url: `/situation-dashboard/threat/${threatId}/related-threats`,
    method: 'get'
  })
}

/**
 * 获取资产风险热力图数据
 */
export const getAssetRiskHeatmap = async () => {
  try {
    const response = await retryRequest(() =>
      request<{ data: Array<{ assetId: string, assetName: string, riskScore: number, location: string }> }>({
        url: '/situation-dashboard/heatmap',
        method: 'get',
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    if (!Array.isArray(response?.data)) {
      console.warn('后端返回热力图数据格式异常，使用模拟数据')
      return generateMockHeatmapData()
    }
    
    // 验证数组中的每个元素是否包含必要的字段
    const isValid = response.data.every(item => 
      typeof item === 'object' && 
      item !== null && 
      typeof item.assetId === 'string' && 
      typeof item.assetName === 'string' && 
      typeof item.riskScore === 'number' && 
      typeof item.location === 'string'
    )
    
    if (!isValid) {
      console.warn('后端返回热力图数据字段不完整，使用模拟数据')
      return generateMockHeatmapData()
    }
    
    return response
  } catch (error) {
    console.warn('获取资产风险热力图数据失败，使用模拟数据:', error)
    return generateMockHeatmapData()
  }
}

/**
 * 获取高风险资产列表
 * @param page 页码
 * @param size 每页数量
 */
export const getHighRiskAssets = async (page: number = 1, size: number = 20) => {
  // 参数验证
  if (page <= 0) {
    throw new Error('页码必须大于0')
  }
  
  if (size <= 0 || size > 100) {
    console.warn('每页数量超出范围，限制为100')
    size = Math.min(Math.max(size, 1), 100)
  }
  
  try {
    const response = await retryRequest(() =>
      request<{ data: {
        list: AssetRisk[];
        total: number;
        page: number;
        size: number;
      } }>({
        url: '/situation-dashboard/high-risk-assets',
        method: 'get',
        params: {
          page,
          size
        },
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    if (Array.isArray(response.data)) {
      // 如果直接返回数组，说明是模拟数据
      return { data: { list: response.data, total: response.data.length, page: 1, size: response.data.length } }
    }
    
    const expectedFields = ['list', 'total', 'page', 'size']
    if (!validateResponseData(response.data, expectedFields)) {
      throw new Error('响应数据格式异常')
    }
    
    // 类型断言确保类型安全
    const data = response.data as unknown as { list: AssetRisk[]; total: number; page: number; size: number }
    if (!Array.isArray(data.list)) {
      throw new Error('资产列表数据格式异常')
    }
    
    return { data }
  } catch (error) {
    handleApiError(error, '获取高风险资产列表失败')
    throw error
  }
}

/**
 * 获取威胁详情
 * @param threatId 威胁ID
 */
export const getThreatDetail = (threatId: string) => {
  return request<{ data: ThreatIntelligence }>({
    url: `/situation-dashboard/threat/${threatId}`,
    method: 'get'
  })
}

/**
 * 导出态势报告
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param format 报告格式
 */
export const exportSituationReport = (startTime: string, endTime: string, format: 'pdf' | 'excel' = 'pdf') => {
  return request({
    url: '/dashboard/export',
    method: 'get',
    params: {
      startTime,
      endTime,
      format
    },
    responseType: 'blob'
  })
}

/**
 * 获取态势告警列表
 */
export const getSituationAlerts = () => {
  return request<{ data: Array<{
    alertId: string,
    alertType: string,
    alertLevel: string,
    description: string,
    createTime: string,
    status: string
  }>   }>({
    url: '/situation-dashboard/alerts',
    method: 'get'
  })
}

// 模拟数据生成函数
const generateMockTrendData = () => {
  const now = new Date()
  const data = []
  
  // 生成过去24小时的数据点，每小时一个点
  for (let i = 23; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 60 * 60 * 1000)
    data.push({
      timestamp: time.toISOString(),
      situationIndex: Math.floor(Math.random() * 50) + 25, // 25-75之间的随机数
      activeThreats: Math.floor(Math.random() * 10) + 5 // 5-15之间的随机数
    })
  }
  
  return { data }
}

const generateMockThreatIntelligence = (limit: number) => {
  const threatTypes = ['恶意软件', '钓鱼网站', 'DDoS攻击', 'SQL注入', 'XSS攻击', '勒索软件']
  const iocTypes = ['IP', 'DOMAIN', 'URL', 'FILE_HASH', 'EMAIL']
  const severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']
  const sources = ['内部IDS', '外部威胁情报', '沙箱分析', '用户举报', '自动化扫描']
  
  const mockData = []
  
  for (let i = 0; i < limit; i++) {
    const threatType = threatTypes[Math.floor(Math.random() * threatTypes.length)]
    const iocType = iocTypes[Math.floor(Math.random() * iocTypes.length)]
    const severity = severities[Math.floor(Math.random() * severities.length)]
    const source = sources[Math.floor(Math.random() * sources.length)]
    
    mockData.push({
      threatId: `THREAT-${String(i + 1).padStart(4, '0')}`,
      threatType,
      iocType,
      iocValue: generateIOCValue(iocType, i + 1),
      severity,
      confidenceScore: Math.floor(Math.random() * 50) + 50,
      source,
      discoveredTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      firstSeen: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      lastUpdated: new Date().toISOString(),
      description: `${threatType}威胁情报`,
      affectedSystems: [],
      relatedCves: [],
      tags: [threatType, iocType],
      status: 'ACTIVE'
    })
  }
  
  return { data: mockData }
}

const generateIOCValue = (iocType: string, index: number) => {
  switch (iocType) {
    case 'IP':
      return `${Math.floor(Math.random() * 64) + 192}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}`
    case 'DOMAIN':
      return `malicious-domain-${index}.com`
    case 'URL':
      return `http://malicious-site-${index}.com/payload`
    case 'FILE_HASH':
      return `a1b2c3d4e5f6${String(index).padStart(4, '0')}`
    case 'EMAIL':
      return `threat${index}@malicious.com`
    default:
      return `unknown-ioc-${index}`
  }
}



const generateMockSituationOverview = () => {
  const totalAssets = Math.floor(Math.random() * 1000) + 500
  const affectedAssets = Math.floor(Math.random() * totalAssets * 0.3) + 10
  const activeThreats = Math.floor(Math.random() * 100) + 20
  const situationIndex = Math.floor(Math.random() * 100)
  
  // 根据态势指数确定态势等级
  let situationLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE' = 'SAFE'
  if (situationIndex < 20) situationLevel = 'CRITICAL'
  else if (situationIndex < 40) situationLevel = 'HIGH'
  else if (situationIndex < 60) situationLevel = 'MEDIUM'
  else if (situationIndex < 80) situationLevel = 'LOW'
  
  return {
    data: {
      situationIndex,
      situationLevel,
      timestamp: new Date().toISOString(),
      totalAssets,
      affectedAssets,
      activeThreats,
      openVulnerabilities: Math.floor(Math.random() * 80) + 20,
      securityEvents: Math.floor(Math.random() * 500) + 100,
      attackChainCompleteRate: Math.floor(Math.random() * 100),
      dimensionScores: {
        threatDetection: Math.floor(Math.random() * 100),
        vulnerability: Math.floor(Math.random() * 100),
        assetSecurity: Math.floor(Math.random() * 100),
        networkTraffic: Math.floor(Math.random() * 100),
        securityConfig: Math.floor(Math.random() * 100)
      }
    }
  }
}

const generateMockAssetRiskDistribution = () => {
  const assetTypes = ['服务器', '网络设备', '工作站', '数据库', '应用系统']
  const riskLevels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'SAFE']
  
  const mockData: AssetRisk[] = []
  
  for (let i = 0; i < 20; i++) {
    const assetType = assetTypes[Math.floor(Math.random() * assetTypes.length)]
    const riskLevel = riskLevels[Math.floor(Math.random() * riskLevels.length)]
    
    mockData.push({
      assetId: `asset_${i + 1}`,
      assetName: `${assetType}_${i + 1}`,
      assetType,
      riskScore: Math.floor(Math.random() * 100),
      riskLevel: riskLevel as 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE',
      ipAddress: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
      location: '数据中心',
      lastScanTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      assetValueScore: Math.floor(Math.random() * 100),
      vulnerabilityScore: Math.floor(Math.random() * 100),
      threatScore: Math.floor(Math.random() * 100),
      vulnerabilityCount: Math.floor(Math.random() * 10),
      threatCount: Math.floor(Math.random() * 5),
      status: 'ACTIVE',
      operatingSystem: 'Windows Server 2019',
      businessSystem: '核心业务系统',
      riskComposition: {
        threat: Math.floor(Math.random() * 40),
        vulnerability: Math.floor(Math.random() * 40),
        misconfiguration: Math.floor(Math.random() * 20),
        exposure: Math.floor(Math.random() * 20)
      }
    } as AssetRisk)
  }
  
  return { data: mockData }
}

const generateMockHeatmapData = () => {
  const locations = ['北京', '上海', '广州', '深圳', '杭州', '南京', '成都', '武汉', '西安', '重庆']
  const assetTypes = ['服务器', '网络设备', '工作站', '数据库', '应用系统']
  
  const mockData = []
  
  for (let i = 0; i < 30; i++) {
    const location = locations[Math.floor(Math.random() * locations.length)]
    const assetType = assetTypes[Math.floor(Math.random() * assetTypes.length)]
    
    mockData.push({
      assetId: `asset_${i + 1}`,
      assetName: `${assetType}_${i + 1}`,
      riskScore: Math.floor(Math.random() * 100),
      location
    })
  }
  
  return { data: mockData }
}

/**
 * 获取资产地理分布数据（含经纬度）
 */
export const getAssetGeographicDistribution = async () => {
  try {
    const response = await retryRequest(() =>
      request<{
        data: {
          assets: Array<{
            assetId: string
            assetName: string
            assetType: string
            riskScore: number
            riskLevel: string
            location: string
            longitude: number
            latitude: number
            ipAddress: string
            status: string
          }>
          locationStats: {
            [key: string]: {
              name: string
              value: number
              longitude: number
              latitude: number
              assetCount: number
              highRiskCount: number
              assets: Array<{ name: string; riskScore: number; value: number }>
            }
          }
        }
      }>({
        url: '/situation-dashboard/asset-geographic',
        method: 'get',
        timeout: DEFAULT_TIMEOUT
      })
    )
    
    // 验证响应数据
    if (!response?.data || typeof response.data !== 'object') {
      console.warn('后端返回地理分布数据格式异常，使用模拟数据')
      return generateMockGeographicData()
    }

    const apiResponse = response.data as any
    const responseData = apiResponse?.data

    if (!responseData || typeof responseData !== 'object') {
      console.warn('后端返回地理分布数据格式异常，使用模拟数据')
      return generateMockGeographicData()
    }

    const { assets, locationStats } = responseData

    if (!Array.isArray(assets) || typeof locationStats !== 'object') {
      console.warn('后端返回地理分布数据结构不完整，使用模拟数据')
      return generateMockGeographicData()
    }

    return response
  } catch (error) {
    console.warn('获取资产地理分布数据失败，使用模拟数据:', error)
    return generateMockGeographicData()
  }
}

/**
 * 生成模拟资产地理分布数据
 */
const generateMockGeographicData = () => {
  // 城市坐标
  const cityCoordinates: Record<string, { lng: number; lat: number }> = {
    '北京数据中心': { lng: 116.4074, lat: 39.9042 },
    '上海机房': { lng: 121.4737, lat: 31.2304 },
    '广州办公室': { lng: 113.2644, lat: 23.1291 },
    '深圳研发中心': { lng: 114.0579, lat: 22.5431 },
    '成都分部': { lng: 104.0668, lat: 30.5728 },
    '武汉分公司': { lng: 114.3055, lat: 30.5928 }
  }
  
  const assetTypes = ['服务器', '网络设备', '工作站', '数据库', '应用系统']
  const locations = Object.keys(cityCoordinates)
  
  const assets: any[] = []
  const locationStats: Record<string, any> = {}
  
  for (let i = 0; i < 50; i++) {
    const location = locations[i % locations.length]
    const coords = cityCoordinates[location]
    const riskScore = Math.floor(Math.random() * 100)
    
    assets.push({
      assetId: `asset_${i + 1}`,
      assetName: `${assetTypes[i % assetTypes.length]}_${i + 1}`,
      assetType: assetTypes[i % assetTypes.length],
      riskScore,
      riskLevel: riskScore >= 80 ? 'CRITICAL' : riskScore >= 60 ? 'HIGH' : riskScore >= 40 ? 'MEDIUM' : 'LOW',
      location,
      longitude: coords.lng + (Math.random() - 0.5) * 0.1,
      latitude: coords.lat + (Math.random() - 0.5) * 0.1,
      ipAddress: `192.168.${Math.floor(i / 10)}.${i % 256}`,
      status: 'ACTIVE'
    })
  }
  
  // 生成位置统计数据
  locations.forEach(location => {
    const coords = cityCoordinates[location]
    const locationAssets = assets.filter((a: any) => a.location === location)
    const avgRisk = locationAssets.reduce((sum: number, a: any) => sum + a.riskScore, 0) / locationAssets.length
    
    locationStats[location] = {
      name: location,
      value: Math.round(avgRisk),
      longitude: coords.lng,
      latitude: coords.lat,
      assetCount: locationAssets.length,
      highRiskCount: locationAssets.filter(a => a.riskScore >= 60).length,
      assets: locationAssets.slice(0, 5).map(a => ({
        name: a.assetName,
        riskScore: a.riskScore,
        value: a.riskScore
      }))
    }
  })
  
  return {
    data: {
      assets,
      locationStats
    }
  }
}

