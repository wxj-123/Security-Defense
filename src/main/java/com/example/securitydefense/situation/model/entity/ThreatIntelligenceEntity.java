package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 威胁情报实体类
 * 对应数据库中的threat_intelligence表
 */
@Data
@TableName("threat_intelligence")
public class ThreatIntelligenceEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("ioc_value")
    private String iocValue;
    
    @TableField("ioc_type")
    private String iocType;
    
    @TableField("threat_type")
    private String threatType;
    
    @TableField("threat_level")
    private String threatLevel;
    
    @TableField("confidence_score")
    private Double confidenceScore;
    
    private String source;
    
    @TableField("first_seen")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstSeen;
    
    @TableField("last_seen")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSeen;
    
    private String description;
    
    @TableField("related_cve")
    private String relatedCve;
    
    @TableField("location_info")
    private String locationInfo;
    
    private String tags;
    
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}