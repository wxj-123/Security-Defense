package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 关联分析结果实体类
 * 对应数据库中的correlation_result表
 */
@Data
@TableName("correlation_result")
public class CorrelationResultEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("correlation_id")
    private String correlationId;
    
    @TableField("event_ids")
    private String eventIds;
    
    @TableField("correlation_type")
    private String correlationType;
    
    @TableField("related_asset_id")
    private String relatedAssetId;
    
    @TableField("related_threat_id")
    private String relatedThreatId;
    
    @TableField("confidence_score")
    private Double confidenceScore;
    
    private String severity;
    
    private String summary;
    
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}