package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private String username;
    
    private String operation;
    
    private String method;
    
    private String params;
    
    private String ip;
    
    private String location;
    
    private Integer status;
    
    @TableField("error_msg")
    private String errorMsg;
    
    @TableField("execution_time")
    private Integer executionTime;
    
    @TableField("operation_user")
    private String operationUser;
    
    @TableField("operation_module")
    private String operationModule;
    
    @TableField("operation_type")
    private String operationType;
    
    @TableField("operation_content")
    private String operationContent;
    
    @TableField("operation_ip")
    private String operationIp;
    
    @TableField("operation_result")
    private String operationResult;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}