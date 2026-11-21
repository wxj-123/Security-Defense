package com.example.securitydefense.situation.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;


/**
 * 攻击链模型
 * 对应数据库表：attack_chain
 */
@Data
@TableName("attack_chain")
public class AttackChain {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 攻击链ID
     */
    private String chainId;
    
    /**
     * 攻击阶段
     * 1-侦察 2-武器化 3-初始访问 4-权限提升 5-横向移动 6-数据收集 7-渗透 8-维持访问 9-清除痕迹
     */
    private Integer attackPhase;
    
    /**
     * 攻击阶段名称
     */
    private String phaseName;
    
    /**
     * MITRE ATT&CK技术标签
     */
    private String mitreTechniqueId;
    
    /**
     * 技术描述
     */
    private String techniqueDescription;
    
    /**
     * 源IP
     */
    private String sourceIp;
    
    /**
     * 目标资产
     */
    private String targetAsset;
    
    /**
     * 关联事件ID列表（JSON格式存储）
     */
    private String relatedEventIds;
    
    /**
     * 攻击时间
     */
    private LocalDateTime attackTime;
    
    /**
     * 持续时间（秒）
     */
    private Integer duration;
    
    /**
     * 攻击状态
     * 0-进行中 1-已完成 2-已阻断
     */
    private Integer status;
    
    /**
     * 置信度（0-1）
     */
    private Double confidence;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}