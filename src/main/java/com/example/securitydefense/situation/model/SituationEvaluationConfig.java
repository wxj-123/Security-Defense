package com.example.securitydefense.situation.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;

/**
 * 态势评估配置实体类
 * 对应数据库表 situation_evaluation_config
 */
@Data
@TableName("situation_evaluation_config")
public class SituationEvaluationConfig {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 低风险阈值
     */
    private Double lowRiskThreshold = 30.0;
    
    /**
     * 中风险阈值
     */
    private Double mediumRiskThreshold = 60.0;
    
    /**
     * 高风险阈值
     */
    private Double highRiskThreshold = 80.0;
    
    /**
     * 是否启用实时计算
     */
    private Boolean realTimeCalculationEnabled = true;
    
    /**
     * 数据聚合窗口大小(秒)
     */
    private Integer dataAggregationWindow = 60;
    
    /**
     * 威胁权重
     */
    private Double threatWeight = 0.4;
    
    /**
     * 漏洞权重
     */
    private Double vulnerabilityWeight = 0.3;
    
    /**
     * 资产重要性权重
     */
    private Double assetImportanceWeight = 0.3;
    
    /**
     * 评估间隔(秒)
     */
    private Integer evaluationInterval = 60;
    
    /**
     * 历史数据保留天数
     */
    private Integer historyDataRetentionDays = 30;
    
    /**
     * 是否启用告警
     */
    private Boolean enableAlert = true;
    
    /**
     * 告警阈值
     */
    private Double alertThreshold = 70.0;
    
    /**
     * 配置描述
     */
    private String configDescription;
    
    /**
     * 状态
     */
    private String status = "ACTIVE";
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}