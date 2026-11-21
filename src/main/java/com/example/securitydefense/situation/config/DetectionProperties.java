package com.example.securitydefense.situation.config;

import lombok.Data;

/**
 * 威胁检测配置属性
 */
@Data
public class DetectionProperties {
    
    /**
     * 异常检测阈值
     */
    private double anomalyThreshold = 0.85;
    
    /**
     * IOC匹配频率（秒）
     */
    private int iocMatchInterval = 60;
    
    /**
     * 攻击链分析窗口（分钟）
     */
    private int attackChainWindow = 30;
    
    /**
     * 是否启用机器学习检测
     */
    private boolean enableMachineLearning = true;
    
    /**
     * 模型更新频率（小时）
     */
    private int modelUpdateInterval = 24;
    
    /**
     * 资产威胁评分过期时间（天）
     */
    private int assetThreatScoreExpiry = 7;
}