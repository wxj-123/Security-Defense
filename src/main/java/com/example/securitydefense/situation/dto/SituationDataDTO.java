package com.example.securitydefense.situation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 态势数据DTO
 */
@Data
public class SituationDataDTO {
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 态势指数（0-100分）
     */
    private Double situationIndex;
    
    /**
     * 威胁等级
     */
    private Integer threatLevel;
    
    /**
     * 受影响资产数量
     */
    private Integer affectedAssets;
    
    /**
     * 活跃威胁数量
     */
    private Integer activeThreats;
    
    /**
     * 高危漏洞数量
     */
    private Integer highVulnerabilities;
    
    /**
     * 态势等级描述
     */
    private String situationLevelDesc;
}