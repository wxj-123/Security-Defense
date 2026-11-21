package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 防火墙规则实体类
 */
@Data
@TableName("firewall_rule")
public class FirewallRule {
    
    @TableId("id")
    private Long id;
    
    @TableField("rule_name")
    private String ruleName;
    
    @TableField("rule_type")
    private String ruleType;
    
    @TableField("source_ip")
    private String sourceIp;
    
    @TableField("target_ip")
    private String targetIp;
    
    @TableField("source_port")
    private String sourcePort;
    
    @TableField("target_port")
    private String targetPort;
    
    @TableField("protocol")
    private String protocol;
    
    @TableField("direction")
    private String direction;
    
    @TableField("priority")
    private Integer priority;
    
    @TableField("status")
    private Integer status;
    
    @TableField("description")
    private String description;
    
    @TableField("deleted")
    private Integer deleted;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}