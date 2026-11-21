package com.example.securitydefense.situation.config;

import lombok.Data;

/**
 * 数据采集配置属性
 */
@Data
public class CollectionProperties {
    
    /**
     * 数据采集延迟要求（秒）
     */
    private int collectionDelay = 5;
    
    /**
     * 批处理大小
     */
    private int batchSize = 1000;
    
    /**
     * 是否启用网络流量采集
     */
    private boolean enableNetflowCollection = true;
    
    /**
     * 是否启用安全日志采集
     */
    private boolean enableSecurityLogCollection = true;
    
    /**
     * 是否启用系统日志采集
     */
    private boolean enableSystemLogCollection = true;
    
    /**
     * 是否启用威胁情报采集
     */
    private boolean enableThreatIntelligence = true;
    
    /**
     * 威胁情报更新频率（小时）
     */
    private int threatIntelUpdateInterval = 1;
}