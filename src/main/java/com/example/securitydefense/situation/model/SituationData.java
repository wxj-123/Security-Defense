package com.example.securitydefense.situation.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 态势数据模型
 * 对应数据库表：situation_data
 */
@Data
@TableName("situation_data")
public class SituationData {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 评估ID
     */
    private String evaluationId;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 评估器名称
     */
    private String evaluatorName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 态势指数（0-100分）
     */
    private Double situationIndex;
    
    /**
     * 态势得分
     */
    private Double situationScore;
    
    /**
     * 态势等级
     */
    private String situationLevel;
    
    /**
     * 威胁等级
     * 1-低风险 2-中风险 3-高风险 4-严重风险
     */
    private Integer threatLevel;
    
    /**
     * 受影响资产数量
     */
    private Integer affectedAssetCount;
    
    /**
     * 活跃威胁数量
     */
    private Integer activeThreatCount;
    
    /**
     * 高危漏洞数量
     */
    private Integer highVulnerabilityCount;
    
    /**
     * 资产评分（0-100）
     */
    private Double assetScore;
    
    /**
     * 威胁评分（0-100）
     */
    private Double threatScore;
    
    /**
     * 漏洞评分（0-100）
     */
    private Double vulnerabilityScore;
    
    /**
     * 风险趋势
     * 1-上升 2-稳定 3-下降
     */
    private Integer riskTrend;
    
    /**
     * 主要威胁类型
     */
    private String primaryThreatType;
    
    /**
     * 主要受影响资产类型
     */
    private String primaryAffectedAssetType;
    
    /**
     * 安全事件数量
     */
    private Integer securityEventsCount;
    
    /**
     * 异常行为数量
     */
    private Integer anomalyCount;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 威胁类型分布
     */
    private Map<String, Integer> threatTypeDistribution;
    
    /**
     * 资产风险分布
     */
    private Map<String, Integer> assetRiskDistribution;
    
    /**
     * 主要威胁列表
     */
    private List<ThreatSummary> majorThreats;
    
    /**
     * 风险资产列表
     */
    private List<AssetRiskSummary> riskyAssets;
    
    /**
     * 维度得分
     */
    private DimensionScores dimensionScores;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记
     */
    @TableField(select = false)
    private Integer deleted;
    
    // 维度评分内部类
    @Data
    public static class DimensionScores {
        private Double assetScore;
        private Double threatScore;
        private Double vulnerabilityScore;
        private Double assetWeight;
        private Double threatWeight;
        private Double vulnerabilityWeight;
    }
    
    // 资产风险摘要内部类
    @Data
    public static class AssetRiskSummary {
        private String assetId;
        private String assetName;
        private String assetType;
        private Double riskScore;
        private String riskLevel;
        private Integer threatCount;
        private Integer vulnerabilityCount;
    }
    
    // 威胁摘要内部类
    @Data
    public static class ThreatSummary {
        private String threatType;
        private String threatName;
        private String severity;
        private Integer count;
        private Double riskScore;
    }
}