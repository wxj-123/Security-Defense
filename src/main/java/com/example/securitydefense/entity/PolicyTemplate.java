package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 策略模板实体类
 * 对应数据库中的policy_template表
 */
@Data
@TableName("policy_template")
public class PolicyTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("template_name")
    private String templateName;
    
    @TableField("template_type")
    private String templateType;
    
    @TableField("template_content")
    private String templateContent; // JSON格式存储模板内容
    
    @TableField("description")
    private String description;
    
    @TableField("is_default")
    private Integer isDefault;
    
    @TableField("status")
    private Integer status;
    
    @TableField("deleted")
    private Integer deleted;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}