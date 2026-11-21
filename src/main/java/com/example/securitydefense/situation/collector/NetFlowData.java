package com.example.securitydefense.situation.collector;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 网络流量数据模型
 * 用于收集和存储网络流量数据
 */
@Data
@TableName("net_flow_data")
public class NetFlowData {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 源IP地址
     */
    private String sourceIp;
    
    /**
     * 目标IP地址
     */
    private String destIp;
    
    /**
     * 手动添加setter方法以确保Lombok生成问题
     */
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
    
    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }
    
    public void setPacketCount(Long packetCount) {
        this.packetCount = packetCount;
    }
    
    public void setByteCount(Long byteCount) {
        this.byteCount = byteCount;
    }
    
    public void setDuration(Double duration) {
        this.duration = duration;
    }
    
    public void setTimestamp(java.time.LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSourceIp() {
        return this.sourceIp;
    }
    
    public String getDestIp() {
        return this.destIp;
    }
    
    public Long getPacketCount() {
        return this.packetCount;
    }
    
    public Long getByteCount() {
        return this.byteCount;
    }
    
    public Double getDuration() {
        return this.duration;
    }
    
    public java.time.LocalDateTime getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * 源端口
     */
    private Integer sourcePort;
    
    /**
     * 目标端口
     */
    private Integer destPort;
    
    /**
     * 协议类型（TCP/UDP/ICMP等）
     */
    private String protocol;
    
    /**
     * 数据包数量
     */
    private Long packetCount;
    
    /**
     * 字节数量
     */
    private Long byteCount;
    
    /**
     * 流量持续时间（秒）
     */
    private Double duration;
    
    /**
     * 数据包类型（请求/响应）
     */
    private String packetType;
    
    /**
     * 应用层协议
     */
    private String applicationProtocol;
    
    /**
     * 流量状态（正常/可疑/异常）
     */
    private String flowStatus;
    
    /**
     * 流量分类（业务/管理/异常）
     */
    private String flowCategory;
    
    /**
     * 相关资产ID
     */
    private String assetId;
    
    /**
     * 检测到的威胁标志
     */
    private Boolean threatDetected;
    
    /**
     * 检测到的威胁类型
     */
    private String detectedThreatType;
    
    /**
     * 流量收集设备
     */
    private String collectorDevice;
    
    /**
     * 流量收集接口
     */
    private String collectorInterface;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记
     */
    @TableField(select = false)
    private Integer deleted;
}
