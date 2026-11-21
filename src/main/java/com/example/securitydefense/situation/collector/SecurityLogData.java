package com.example.securitydefense.situation.collector;

import lombok.Data;

/**
 * 安全设备日志数据模型
 */
@Data
public class SecurityLogData {
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 设备类型（Firewall/WAF/IDS/IPS等）
     */
    private String deviceType;
    
    /**
     * 日志级别（INFO/WARN/ERROR/CRITICAL等）
     */
    private String logLevel;
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件类型（BLOCK/ALERT/DROP/ALLOW等）
     */
    private String eventType;
    
    /**
     * 源IP地址
     */
    private String sourceIp;
    
    /**
     * 目标IP地址
     */
    private String destIp;
    
    /**
     * 源端口
     */
    private Integer sourcePort;
    
    /**
     * 目标端口
     */
    private Integer destPort;
    
    /**
     * 协议类型
     */
    private String protocol;
    
    /**
     * 日志消息内容
     */
    private String message;
    
    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 规则ID
     */
    private String ruleId;
    
    /**
     * 威胁情报关联
     */
    private String threatIntelligenceId;
    
    /**
     * 处理结果
     */
    private String actionResult;
}