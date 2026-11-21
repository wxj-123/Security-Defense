package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 系统配置实体类
 * 对应数据库中的system_config表
 */
@Data
@TableName("system_config")
public class SystemConfigEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("config_key")
    private String configKey;
    
    @TableField("config_value")
    private String configValue;
    
    @TableField("config_type")
    private String configType;
    
    private String description;
    
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}