package com.example.securitydefense.situation.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产风险模型
 * 对应数据库表：asset_risk
 */
@Data
@TableName("asset_risk")
public class AssetRisk {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资产ID
     */
    private String assetId;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 资产类型
     */
    private String assetType;
    
    /**
     * 资产IP地址
     */
    private String ipAddress;
    
    /**
     * 资产位置
     */
    private String location;
    
    /**
     * 资产标签（JSON格式）
     */
    private String assetTags;
    
    /**
     * 业务重要性等级
     * 1-一般 2-重要 3-核心
     */
    private Integer businessImportance;
    
    /**
     * 风险值（0-100）
     */
    private Double riskScore;
    
    /**
     * 漏洞总数
     */
    private Integer vulnerabilityCount;
    
    /**
     * 高危漏洞数量
     */
    private Integer highVulnerabilityCount;
    
    /**
     * 中危漏洞数量
     */
    private Integer mediumVulnerabilityCount;
    
    /**
     * 低危漏洞数量
     */
    private Integer lowVulnerabilityCount;
    
    /**
     * 威胁事件数量
     */
    private Integer threatEventCount;
    
    /**
     * 最近威胁时间
     */
    private LocalDateTime lastThreatTime;
    
    /**
     * 风险等级
     * 1-低风险 2-中风险 3-高风险 4-严重风险
     */
    private Integer riskLevel;
    
    /**
     * 风险趋势
     * 1-上升 2-稳定 3-下降
     */
    private Integer riskTrend;
    
    /**
     * 主要风险来源
     */
    private String primaryRiskSource;
    
    /**
     * 修复建议（JSON格式）
     */
    private String remediationSuggestions;
    
    /**
     * 责任人
     */
    private String responsiblePerson;
    
    /**
     * 安全状态
     * 1-正常 2-警告 3-异常 4-离线
     */
    private Integer securityStatus;
    
    /**
     * 上次扫描时间
     */
    private LocalDateTime lastScanTime;
    
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