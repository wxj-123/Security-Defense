package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 态势数据实体类
 * 对应数据库中的situation_data表
 */
@Data
@TableName("situation_data")
public class SituationDataEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    
    @TableField("situation_index")
    private Double situationIndex;
    
    @TableField("situation_level")
    private String situationLevel;
    
    @TableField("total_assets")
    private Integer totalAssets;
    
    @TableField("critical_assets")
    private Integer criticalAssets;
    
    @TableField("high_threats")
    private Integer highThreats;
    
    @TableField("critical_vulnerabilities")
    private Integer criticalVulnerabilities;
    
    @TableField("threat_score")
    private Double threatScore;
    
    @TableField("vulnerability_score")
    private Double vulnerabilityScore;
    
    @TableField("asset_value_score")
    private Double assetValueScore;
    
    @TableField("risk_distribution")
    private String riskDistribution;
    
    @TableField("trend_indicator")
    private String trendIndicator;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}