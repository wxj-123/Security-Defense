package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 安全日志实体类
 * 对应数据库中的security_log表
 */
@Data
@TableName("security_log")
public class SecurityLogEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    
    @TableField("source_system")
    private String sourceSystem;
    
    @TableField("log_type")
    private String logType;
    
    @TableField("event_id")
    private String eventId;
    
    private String severity;
    
    private String message;
    
    @TableField("source_ip")
    private String sourceIp;
    
    @TableField("user_id")
    private String userId;
    
    @TableField("asset_id")
    private String assetId;
    
    private String action;
    
    private String result;
    
    @TableField("risk_score")
    private Double riskScore;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}