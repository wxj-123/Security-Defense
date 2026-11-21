package com.example.securitydefense.situation.dto;

import lombok.Data;
import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * 态势评估数据传输对象
 * 用于在控制器、服务层和数据访问层之间传递态势评估相关数据
 */
@Data
public class SituationEvaluationDTO {
    
    /**
     * 态势数据基础信息
     */
    private Long id;
    private Date timestamp;
    private Double situationIndex; // 态势指数
    private String situationLevel; // 态势等级
    
    /**
     * 评分数据
     */
    private Double assetScore; // 资产评分
    private Double threatScore; // 威胁评分
    private Double vulnerabilityScore; // 漏洞评分
    
    /**
     * 趋势分析
     */
    private String riskTrend; // 风险趋势
    private String trendIndicator; // 趋势指标
    
    /**
     * 详细统计
     */
    private Integer totalAssets; // 涉事资产数量
    private Integer criticalAssets; // 危急资产数量
    private Integer highThreats; // 高危威胁数量
    private Integer criticalVulnerabilities; // 危急漏洞数量
    private Integer securityEventCount; // 安全事件数量
    private Integer abnormalBehaviorCount; // 异常行为数量
    
    /**
     * 分类信息
     */
    private Map<String, Integer> mainThreatTypes; // 主要威胁类型分布
    private Map<String, Integer> mainAffectedAssetTypes; // 主要受影响资产类型分布
    private Map<String, Double> riskDistribution; // 风险分布
    
    /**
     * 关联数据
     */
    private List<AssetRiskDTO> topRiskAssets; // 高风险资产列表
    private List<NetFlowDTO> abnormalFlows; // 异常流量列表
    
    /**
     * 配置相关
     */
    private SituationConfigDTO evaluationConfig; // 评估配置
    
    /**
     * 备注信息
     */
    private String remark;
    private Date createTime;
    private Date updateTime;
    
    /**
     * 转换态势数据为DTO
     * @param situationData 态势数据实体
     * @return 态势评估DTO
     */
    public static SituationEvaluationDTO fromEntity(Object situationData) {
        // 这里将在服务层实现具体的转换逻辑
        return new SituationEvaluationDTO();
    }
}