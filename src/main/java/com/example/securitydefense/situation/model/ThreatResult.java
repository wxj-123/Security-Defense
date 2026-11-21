package com.example.securitydefense.situation.model;

import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * 威胁检测结果模型
 */
@Data
public class ThreatResult {
    
    /**
     * 检测ID
     */
    private String detectionId;
    
    /**
     * 检测时间戳
     */
    private Long timestamp;
    
    /**
     * 威胁类型
     */
    private String threatType;
    
    /**
     * 威胁级别（HIGH/MEDIUM/LOW）
     */
    private String threatLevel;
    
    /**
     * 源IP地址
     */
    private String sourceIp;
    
    /**
     * 目标IP地址
     */
    private String destIp;
    
    /**
     * 相关资产ID
     */
    private String assetId;
    
    /**
     * 目标资产ID
     */
    private String targetAssetId;
    
    /**
     * 攻击阶段
     */
    private String attackPhase;
    
    /**
     * 检测到的异常行为描述
     */
    private String description;
    
    /**
     * 检测分数（0-100）
     */
    private Double detectionScore;
    
    /**
     * 置信度分数（0-100）
     */
    private Double confidenceScore;
    
    /**
     * 检测时间
     */
    private Date detectedTime;
    
    /**
     * 检测规则ID
     */
    private String ruleId;
    
    /**
     * 检测引擎名称
     */
    private String detectorName;
    
    /**
     * 原始数据引用
     */
    private String rawDataReference;
    
    /**
     * 威胁情报关联ID
     */
    private String threatIntelligenceId;
    
    /**
     * 建议响应措施
     */
    private String recommendedAction;
    
    /**
     * 附加信息
     */
    private Map<String, String> additionalInfo;
}