package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全策略实体类
 * 对应数据库中的security_policy表
 */
@Data
@TableName("security_policy")
public class SecurityPolicy {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("policy_name")
    private String policyName;
    
    @TableField("policy_type")
    private String policyType;
    
    @TableField("description")
    private String description;
    
    @TableField("enabled")
    private Integer enabled;
    
    @TableField("rules")
    private String rules; // JSON格式存储规则
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}