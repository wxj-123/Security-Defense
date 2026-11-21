package com.example.securitydefense.situation.dto;

import lombok.Data;
import java.util.Date;

/**
 * 态势配置数据传输对象
 * 用于态势评估配置信息的传递和展示
 */
@Data
public class SituationConfigDTO {
    
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 风险阈值配置
     */
    private Double lowRiskThreshold; // 低风险阈值
    private Double mediumRiskThreshold; // 中风险阈值
    private Double highRiskThreshold; // 高风险阈值
    
    /**
     * 计算参数配置
     */
    private Boolean realTimeCalculationEnabled; // 是否启用实时计算
    private Integer dataAggregationWindow; // 数据聚合窗口大小(秒)
    private Integer evaluationInterval; // 评估间隔(秒)
    
    /**
     * 权重配置
     */
    private Double threatWeight; // 威胁权重
    private Double vulnerabilityWeight; // 漏洞权重
    private Double assetImportanceWeight; // 资产重要性权重
    
    /**
     * 告警配置
     */
    private Boolean enableAlert; // 是否启用告警
    private Double alertThreshold; // 告警阈值
    
    /**
     * 数据保留配置
     */
    private Integer historyDataRetentionDays; // 历史数据保留天数
    
    /**
     * 配置元数据
     */
    private String configDescription; // 配置描述
    private String status; // 状态
    
    private Date createTime;
    private Date updateTime;
    
    /**
     * 获取当前配置的风险等级描述
     * @param score 评分
     * @return 风险等级描述
     */
    public String getRiskLevelDescription(double score) {
        if (score < lowRiskThreshold) {
            return "低风险(1)";
        } else if (score < mediumRiskThreshold) {
            return "中风险(2)";
        } else if (score < highRiskThreshold) {
            return "高风险(3)";
        } else {
            return "危急风险(4)";
        }
    }
    
    /**
     * 检查是否需要告警
     * @param score 评分
     * @return 是否需要告警
     */
    public boolean shouldAlert(double score) {
        return enableAlert && score >= alertThreshold;
    }
}