/**
 * 安全态势数据类型
 */
export interface SituationData {
  /** 态势指数 */
  situationIndex: number
  /** 态势等级 */
  situationLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE'
  /** 时间戳 */
  timestamp: string
  /** 总资产数 */
  totalAssets: number
  /** 受影响资产数 */
  affectedAssets: number
  /** 活跃威胁数 */
  activeThreats: number
  /** 未修复漏洞数 */
  openVulnerabilities: number
  /** 安全事件数 */
  securityEvents: number
  /** 攻击链完整率 */
  attackChainCompleteRate: number
  /** 维度评分 */
  dimensionScores: {
    threatDetection?: number
    vulnerability?: number
    assetSecurity?: number
    networkTraffic?: number
    securityConfig?: number
    [key: string]: number | undefined
  }
  /** 威胁摘要 */
  threatSummary?: {
    totalThreats: number
    bySeverity: {
      CRITICAL: number
      HIGH: number
      MEDIUM: number
      LOW: number
      INFO: number
    }
    byType: Record<string, number>
    recentThreats: ThreatInfo[]
  }
  /** 资产风险摘要 */
  assetRiskSummary?: {
    totalAssets: number
    byRiskLevel: {
      CRITICAL: number
      HIGH: number
      MEDIUM: number
      LOW: number
      SAFE: number
    }
    byAssetType: Record<string, number>
    highRiskAssets: AssetInfo[]
  }
}

/**
 * 威胁信息类型
 */
export interface ThreatInfo {
  threatId: string
  threatType: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
  iocValue: string
  iocType: string
  discoveredTime: string
  confidenceScore: number
  description: string
}

/**
 * 威胁情报详情
 */
export interface ThreatIntelligence extends ThreatInfo {
  firstSeen?: string
  provider?: string
  additionalInfo?: Record<string, any>
  recommendations?: Array<{
    title: string
    detail: string
  }>
}

/**
 * 相关威胁
 */
export interface RelatedThreat extends ThreatInfo {
  // 相关威胁继承自ThreatInfo
}

/**
 * 活动时间线
 */
export interface Activity {
  timestamp: string
  type: string
  title: string
  description: string
}

/**
 * 资产信息类型
 */
export interface AssetInfo {
  assetId: string
  assetName: string
  assetType: string
  riskScore: number
  riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE'
  vulnerabilityCount: number
  threatCount: number
}

/**
 * 资产风险类型
 */
export interface AssetRisk {
  /** 资产ID */
  assetId: string
  /** 资产名称 */
  assetName: string
  /** 资产类型 */
  assetType: string
  /** 风险评分 */
  riskScore: number
  /** 风险等级 */
  riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'SAFE'
  /** 资产位置 */
  location?: string
  /** 资产IP */
  ipAddress?: string
  /** 操作系统 */
  operatingSystem?: string
  /** 所属业务系统 */
  businessSystem?: string
  /** 资产价值评分 */
  assetValueScore: number
  /** 脆弱性评分 */
  vulnerabilityScore: number
  /** 威胁评分 */
  threatScore: number
  /** 未修复漏洞数 */
  vulnerabilityCount: number
  /** 安全威胁数 */
  threatCount: number
  /** 最近一次扫描时间 */
  lastScanTime?: string
  /** 资产状态 */
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
  /** 风险组成 */
  riskComposition?: {
    threat: number
    vulnerability: number
    misconfiguration: number
    exposure: number
  }
  /** 威胁信息 */
  threats?: ThreatInfo[]
  /** 漏洞信息 */
  vulnerabilities?: VulnerabilityInfo[]
}

/**
 * 资产风险详情
 */
export interface AssetRiskDetail extends AssetRisk {
  // 已经包含在AssetRisk中的字段不再重复定义
}

/**
 * 漏洞信息类型
 */
export interface VulnerabilityInfo {
  vulnId: string
  cveId?: string
  vulnerabilityType: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  cvssScore: number
  description: string
  discoveredTime: string
  patchedTime?: string
  status: 'OPEN' | 'PATCHED' | 'REMEDIATED'
  remediation?: string
}

/**
 * 威胁情报类型
 */
export interface ThreatIntelligence {
  /** 威胁ID */
  threatId: string
  /** IOC值 */
  iocValue: string
  /** IOC类型 */
  iocType: string
  /** 威胁类型 */
  threatType: string
  /** 严重程度 */
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
  /** 置信度分数 */
  confidenceScore: number
  /** 来源 */
  source: string
  /** 发现时间 */
  discoveredTime: string
  /** 首次发现时间 */
  firstSeen: string
  /** 最后更新时间 */
  lastUpdated: string
  /** 描述 */
  description: string
  /** 影响范围 */
  affectedSystems: string[]
  /** 相关CVE */
  relatedCves: string[]
  /** 威胁标签 */
  tags: string[]
  /** 处理建议 */
  recommendations: string[]
  /** 状态 */
  status: 'ACTIVE' | 'INACTIVE' | 'FALSE_POSITIVE'
}

/**
 * 攻击链类型
 */
export interface AttackChain {
  chainId: string
  attackPhase: string
  startTime: string
  endTime?: string
  status: string
  relatedAssets: string[]
  relatedEvents: string[]
  severity: string
  confidenceScore: number
}

/**
 * 安全事件类型
 */
export interface SecurityEvent {
  eventId: string
  eventType: string
  sourceIp: string
  destinationIp: string
  timestamp: string
  severity: string
  status: string
  description: string
  sourceSystem: string
}

/**
 * 关联分析结果类型
 */
export interface CorrelationResult {
  correlationId: string
  eventIds: string[]
  correlationType: string
  relatedAssetId?: string
  relatedThreatId?: string
  confidenceScore: number
  severity: string
  summary: string
  startTime: string
  endTime?: string
  status: string
}

/**
 * 预测预警类型
 */
export interface PredictionAlert {
  alertId: string
  alertType: string
  alertLevel: string
  predictedTime: string
  relatedAssetId?: string
  relatedThreatType?: string
  predictionConfidence: number
  description: string
  recommendation: string
  status: string
}

/**
 * 安全统计数据类型
 */
export interface SecurityStatistics {
  /** 威胁统计 */
  threatStats: {
    total: number
    byType: Record<string, number>
    bySeverity: Record<string, number>
    trend: Array<{ time: string, count: number }>
  }
  /** 漏洞统计 */
  vulnerabilityStats: {
    total: number
    bySeverity: Record<string, number>
    byType: Record<string, number>
    unpatchedCount: number
  }
  /** 资产统计 */
  assetStats: {
    total: number
    byType: Record<string, number>
    byRiskLevel: Record<string, number>
    affectedCount: number
  }
  /** 事件统计 */
  eventStats: {
    total: number
    byType: Record<string, number>
    bySeverity: Record<string, number>
    recentEvents: Array<{ eventId: string, eventType: string, timestamp: string, severity: string }>
  }
}

/**
 * 热力图数据类型
 */
export interface HeatmapData {
  assetId: string
  assetName: string
  riskScore: number
  location: string
  coordinates?: {
    x: number
    y: number
  }
}