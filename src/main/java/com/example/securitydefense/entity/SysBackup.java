package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据备份实体类
 */
@Data
@TableName("sys_backup")
public class SysBackup {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String backupName;
    
    private String backupPath;
    
    private String backupType;
    
    private Long backupSize;
    
    private String backupStatus;
    
    private String backupUser;
    
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}