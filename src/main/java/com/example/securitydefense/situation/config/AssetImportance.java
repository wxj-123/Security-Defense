package com.example.securitydefense.situation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 资产重要性配置属性
 */
@Data
@ConfigurationProperties(prefix = "situation.evaluation.asset-importance")
public class AssetImportance {
    
    /**
     * 核心业务资产乘数（默认2.0）
     */
    private double coreBusinessMultiplier = 2.0;
    
    /**
     * 业务资产乘数（默认1.5）
     */
    private double businessMultiplier = 1.5;
    
    /**
     * 非业务资产乘数（默认1.0）
     */
    private double nonBusinessMultiplier = 1.0;
    
    /**
     * 核心业务关键字数组
     */
    private String[] coreBusinessKeywords = {"核心", "关键", "主要", "重要", "Core", "Critical", "Main", "Important"};
}