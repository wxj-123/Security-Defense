package com.example.securitydefense.situation.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 威胁情报模型
 * 对应数据库表：threat_intelligence
 */
@Data
@TableName("threat_intelligence")
public class ThreatIntelligence {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * IOC值（IP、域名、哈希等）
     */
    private String iocValue;
    
    /**
     * IOC类型
     * IP、DOMAIN、URL、HASH、EMAIL等
     */
    private String iocType;
    
    /**
     * 威胁类型
     */
    private String threatType;
    
    /**
     * 威胁等级
     * 1-低风险 2-中风险 3-高风险 4-严重风险
     */
    private Integer threatLevel;
    
    /**
     * 置信度（0-1）
     */
    private Double confidence;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 首次发现时间
     */
    private LocalDateTime firstSeen;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 过期时间
     */
    private LocalDateTime expirationTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}