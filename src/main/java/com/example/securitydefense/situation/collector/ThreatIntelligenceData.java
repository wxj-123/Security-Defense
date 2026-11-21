package com.example.securitydefense.situation.collector;

import lombok.Data;

/**
 * 威胁情报数据模型
 */
@Data
public class ThreatIntelligenceData {
    
    /**
     * IOC类型（IP/DOMAIN/URL/HASH/EMAIL等）
     */
    private String iocType;
    
    /**
     * IOC值
     */
    private String iocValue;
    
    /**
     * 威胁类型（MALWARE/PHISHING/C2/SPAM/RANSOMWARE等）
     */
    private String threatType;
    
    /**
     * 威胁等级（HIGH/MEDIUM/LOW）
     */
    private String threatLevel;
    
    /**
     * 置信度分数（0-100）
     */
    private Integer confidenceScore;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 首次发现时间
     */
    private Long firstSeen;
    
    /**
     * 最后一次发现时间
     */
    private Long lastSeen;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 参考链接
     */
    private String referenceLink;
    
    /**
     * 附加信息
     */
    private String additionalInfo;
}