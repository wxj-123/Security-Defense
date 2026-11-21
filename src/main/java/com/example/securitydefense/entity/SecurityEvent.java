package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 安全事件实体类
 * 对应数据库中的security_event表
 */
@Data
@TableName("security_event")
public class SecurityEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("event_type")
    private String eventType;

    @TableField("event_level")
    private String eventLevel;

    @TableField("source_ip")
    private String sourceIp;

    @TableField("target_ip")
    private String targetIp;

    @TableField("source_port")
    private Integer sourcePort;

    @TableField("target_port")
    private Integer targetPort;

    @TableField("protocol")
    private String protocol;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("handled_by")
    private Long handledBy;

    @TableField("handled_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handledTime;

    @TableField("event_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}