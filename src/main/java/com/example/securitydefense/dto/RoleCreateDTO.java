package com.example.securitydefense.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色创建DTO
 */
@Data
public class RoleCreateDTO {
    
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    
    private String description;
    
    private List<Long> permissionIds;
}