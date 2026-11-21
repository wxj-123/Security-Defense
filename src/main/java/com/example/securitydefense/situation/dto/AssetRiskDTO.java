package com.example.securitydefense.situation.dto;

import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * 资产风险数据传输对象
 * 用于资产风险信息的传递和展示
 */
@Data
public class AssetRiskDTO {
    
    /**
     * 基础信息
     */
    private Long id;
    private String assetId;
    private String assetName;
    private String assetType;
    private String businessImportance; // 业务重要性等级(1-3)
    
    /**
     * 风险评估数据
     */
    private Double riskScore; // 风险评分(0-100)
    private String riskLevel; // 风险等级(1-4)
    private String riskTrend; // 风险趋势
    
    /**
     * 漏洞统计
     */
    private Integer vulnerabilityCount; // 漏洞总数
    private Integer highVulnerabilityCount; // 高危漏洞数量
    private Integer mediumVulnerabilityCount; // 中危漏洞数量
    private Integer lowVulnerabilityCount; // 低危漏洞数量
    
    /**
     * 威胁信息
     */
    private Integer threatEventCount; // 威胁事件数量
    private Date lastThreatTime; // 最后威胁时间
    private String mainRiskSource; // 主要风险来源
    
    /**
     * 资产详情
     */
    private String assetIp; // 资产IP地址
    private String assetLocation; // 资产位置
    private String assetTags; // 资产标签
    private String responsiblePerson; // 责任人
    private String securityStatus; // 安全状态
    private Date lastScanTime; // 最后扫描时间
    
    /**
     * 评估指标
     */
    private Double assetValueScore; // 资产价值评分
    private Double exposureScore; // 暴露度评分
    private Map<String, Object> attackSurface; // 攻击面信息
    private String remediationSuggestion; // 修复建议
    
    /**
     * 关联数据
     */
    private Integer relatedSecurityEvents; // 关联安全事件数量
    private Integer relatedAbnormalFlows; // 关联异常流量数量
    
    private Date createTime;
    private Date updateTime;
}