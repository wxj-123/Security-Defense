package com.example.securitydefense.dto;

import lombok.Data;
import java.util.List;

/**
 * 角色更新DTO
 */
@Data
public class RoleUpdateDTO {
    
    private Long id;
    
    private String roleName;
    
    private String roleCode;
    
    private String description;
    
    private Integer status;
    
    private List<Long> permissionIds;
}