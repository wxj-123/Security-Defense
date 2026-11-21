package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.SysRole;
import com.example.securitydefense.dto.RoleCreateDTO;
import com.example.securitydefense.dto.RoleUpdateDTO;
import com.example.securitydefense.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/system-manage/role")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 创建角色
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> createRole(@Valid @RequestBody RoleCreateDTO roleCreateDTO) {
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = roleService.createRole(roleCreateDTO, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "创建角色失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 更新角色
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> updateRole(@Valid @RequestBody RoleUpdateDTO roleUpdateDTO) {
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = roleService.updateRole(roleUpdateDTO, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "更新角色失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> deleteRole(@PathVariable Long roleId) {
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = roleService.deleteRole(roleId, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "删除角色失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 分页查询角色
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<IPage<SysRole>> queryRolePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName) {
        
        IPage<SysRole> page = roleService.queryRolePage(current, size, roleName);
        return ApiResponse.success(page);
    }
    
    /**
     * 获取所有角色
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleService.getAllRoles();
        return ApiResponse.success(roles);
    }
    
    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SysRole> getRoleById(@PathVariable Long roleId) {
        SysRole role = roleService.getById(roleId);
        return ApiResponse.success(role);
    }
    
    /**
     * 获取角色权限ID列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        List<Long> permissionIds = roleService.getRolePermissionIds(roleId);
        return ApiResponse.success(permissionIds);
    }
    

    
    /**
     * 分配角色权限
     */
    @PostMapping("/{roleId}/assign-permissions")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> assignRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        String operator = "admin"; // 简化处理
        
        boolean result = roleService.assignRolePermissions(roleId, permissionIds, operator);
        return result ? ApiResponse.success(true) : ApiResponse.error(500, "分配角色权限失败");
    }
}