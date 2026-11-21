package com.example.securitydefense.situation.collector;

import lombok.Data;

/**
 * 系统安全日志数据模型
 */
@Data
public class SystemLogData {
    
    /**
     * 系统类型（Windows/Linux等）
     */
    private String systemType;
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * 事件源
     */
    private String eventSource;
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 日志级别
     */
    private String logLevel;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 源IP地址
     */
    private String sourceIp;
    
    /**
     * 进程名称
     */
    private String processName;
    
    /**
     * 日志消息内容
     */
    private String message;
    
    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 状态码
     */
    private String statusCode;
    
    /**
     * 详细信息
     */
    private String details;
    
    /**
     * 严重程度
     */
    private Integer severity;
}