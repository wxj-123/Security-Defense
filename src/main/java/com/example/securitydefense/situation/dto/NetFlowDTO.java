package com.example.securitydefense.situation.dto;

import lombok.Data;
import java.util.Date;

/**
 * 网络流量数据传输对象
 * 用于网络流量信息的传递和展示
 */
@Data
public class NetFlowDTO {
    
    /**
     * 基础信息
     */
    private Long id;
    private String flowId;
    private Date timestamp;
    
    /**
     * 网络连接信息
     */
    private String sourceIp;
    private Integer sourcePort;
    private String destinationIp;
    private Integer destinationPort;
    private String protocol;
    private String applicationProtocol;
    
    /**
     * 流量统计
     */
    private Long bytesSent; // 发送字节数
    private Long bytesReceived; // 接收字节数
    private Integer packetsSent; // 发送包数
    private Integer packetsReceived; // 接收包数
    private Integer duration; // 持续时间(秒)
    
    /**
     * 风险评估
     */
    private Double riskScore; // 风险评分
    private Boolean anomalyDetected; // 是否检测到异常
    private String anomalyType; // 异常类型
    
    /**
     * 地理位置信息
     */
    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    
    /**
     * 流量属性
     */
    private String flowType; // 流量类型
    private String flowStatus; // 流量状态
    private String encryptionStatus; // 加密状态
    
    /**
     * 关联信息
     */
    private String relatedAssetId; // 关联资产ID
    private String relatedAssetName; // 关联资产名称
    private Integer relatedSecurityEvents; // 关联安全事件数量
    
    private Date createTime;
    private Date updateTime;
}