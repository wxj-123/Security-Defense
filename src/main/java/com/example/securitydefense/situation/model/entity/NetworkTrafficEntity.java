package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 网络流量数据实体类
 * 对应数据库中的network_traffic表
 */
@Data
@TableName("network_traffic")
public class NetworkTrafficEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    
    @TableField("source_ip")
    private String sourceIp;
    
    @TableField("destination_ip")
    private String destinationIp;
    
    @TableField("source_port")
    private Integer sourcePort;
    
    @TableField("destination_port")
    private Integer destinationPort;
    
    private String protocol;
    
    @TableField("bytes_sent")
    private Long bytesSent;
    
    @TableField("bytes_received")
    private Long bytesReceived;
    
    @TableField("packet_count")
    private Integer packetCount;
    
    private Integer duration;
    
    @TableField("flow_type")
    private String flowType;
    
    private String status;
    
    @TableField("risk_score")
    private Double riskScore;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}