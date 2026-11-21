package com.example.securitydefense.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体类
 */
@Data
@TableName("sys_permission")
public class SysPermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String permissionName;
    
    private String permissionCode;
    
    private String resourceType;
    
    private String resourceUrl;
    
    private Long parentId;
    
    private Integer sort;
    
    private String icon;
    
    private Integer status;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 子权限列表
    @TableField(exist = false)
    private List<SysPermission> children;
}