package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 资产风险实体类
 * 对应数据库中的asset_risk表
 */
@Data
@TableName("asset_risk")
public class AssetRiskEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("asset_id")
    private String assetId;
    
    @TableField("asset_name")
    private String assetName;
    
    @TableField("asset_type")
    private String assetType;
    
    @TableField("risk_score")
    private Double riskScore;
    
    @TableField("risk_level")
    private String riskLevel;
    
    @TableField("vulnerability_count")
    private Integer vulnerabilityCount;
    
    @TableField("critical_vulnerability_count")
    private Integer criticalVulnerabilityCount;
    
    @TableField("high_vulnerability_count")
    private Integer highVulnerabilityCount;
    
    @TableField("threat_count")
    private Integer threatCount;
    
    @TableField("critical_threat_count")
    private Integer criticalThreatCount;
    
    @TableField("asset_value_score")
    private Double assetValueScore;
    
    @TableField("exposure_score")
    private Double exposureScore;
    
    @TableField("attack_surface")
    private String attackSurface;
    
    @TableField("location")
    private String location;
    
    @TableField("ip_address")
    private String ipAddress;
    
    @TableField("last_scan_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastScanTime;
    
    @TableField("risk_trend")
    private String riskTrend;
    
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}