package com.example.securitydefense.situation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 攻击链实体类
 * 对应数据库中的attack_chain表
 */
@Data
@TableName("attack_chain")
public class AttackChainEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("chain_id")
    private String chainId;
    
    @TableField("attack_phase")
    private String attackPhase;
    
    @TableField("phase_order")
    private Integer phaseOrder;
    
    @TableField("technique_id")
    private String techniqueId;
    
    @TableField("technique_name")
    private String techniqueName;
    
    @TableField("related_events")
    private String relatedEvents;
    
    @TableField("source_ip")
    private String sourceIp;
    
    @TableField("target_asset_id")
    private String targetAssetId;
    
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    
    private Integer duration;
    
    @TableField("confidence_score")
    private Double confidenceScore;
    
    private String severity;
    
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}