package com.example.securitydefense.situation.dto;

import lombok.Data;

/**
 * 威胁地图DTO
 */
@Data
public class ThreatMapDTO {
    
    /**
     * 攻击源IP
     */
    private String sourceIp;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 攻击次数
     */
    private Integer attackCount;
    
    /**
     * 攻击类型
     */
    private String attackType;
    
    /**
     * 威胁等级
     */
    private Integer threatLevel;
    
    /**
     * 国家/地区
     */
    private String country;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 最近攻击时间（时间戳格式）
     */
    private Long lastAttackTime;
}