package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 预测预警实体类
 * 对应数据库中的prediction_alert表
 */
@Data
@TableName("prediction_alert")
public class PredictionAlertEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("alert_id")
    private String alertId;
    
    @TableField("alert_type")
    private String alertType;
    
    @TableField("alert_level")
    private String alertLevel;
    
    @TableField("predicted_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date predictedTime;
    
    @TableField("related_asset_id")
    private String relatedAssetId;
    
    @TableField("related_threat_type")
    private String relatedThreatType;
    
    @TableField("prediction_confidence")
    private Double predictionConfidence;
    
    private String description;
    
    private String recommendation;
    
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}