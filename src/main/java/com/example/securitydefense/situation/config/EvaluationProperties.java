package com.example.securitydefense.situation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 态势评估配置属性
 */
@Data
@ConfigurationProperties(prefix = "situation.evaluation")
public class EvaluationProperties {
    
    /**
     * 资产重要性配置
     */
    private AssetImportance assetImportance = new AssetImportance();
    
    /**
     * 权重配置
     */
    private Weights weights = new Weights();
    
    /**
     * 资产权重（默认40%）
     */
    private double assetWeight = 0.4;
    
    /**
     * 威胁权重（默认35%）
     */
    private double threatWeight = 0.35;
    
    /**
     * 漏洞权重（默认25%）
     */
    private double vulnerabilityWeight = 0.25;
    
    /**
     * 态势计算频率（分钟）
     */
    private int calculateInterval = 5;
    
    /**
     * 历史数据保留天数
     */
    private int historyDays = 30;
    
    /**
     * 态势指数阈值 - 低风险
     */
    private double lowRiskThreshold = 30.0;
    
    /**
     * 态势指数阈值 - 中风险
     */
    private double mediumRiskThreshold = 60.0;
    
    /**
     * 态势指数阈值 - 高风险
     */
    private double highRiskThreshold = 80.0;
    
    /**
     * 是否启用实时计算
     */
    private boolean realTimeCalculationEnabled = true;
    
    /**
     * 数据聚合窗口大小（秒）
     */
    private int aggregationWindowSeconds = 60;
}