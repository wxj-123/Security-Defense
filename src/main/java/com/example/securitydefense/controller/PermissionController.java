package com.example.securitydefense.controller;

import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.SysPermission;
import com.example.securitydefense.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/system-manage/permission")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<SysPermission>> getPermissionTree() {
        List<SysPermission> tree = permissionService.getPermissionTree();
        return ApiResponse.success(tree);
    }
    
    /**
     * 获取用户权限编码集合
     */
    @GetMapping("/user/{userId}/codes")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Set<String>> getUserPermissions(@PathVariable Long userId) {
        Set<String> permissions = permissionService.getUserPermissions(userId);
        return ApiResponse.success(permissions);
    }
    
    /**
     * 获取用户菜单权限
     */
    @GetMapping("/user/{userId}/menus")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<SysPermission>> getUserMenus(@PathVariable Long userId) {
        List<SysPermission> menus = permissionService.getUserMenus(userId);
        return ApiResponse.success(menus);
    }
    
    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/user/{userId}/check")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> checkUserPermission(
            @PathVariable Long userId, 
            @RequestParam String permissionCode) {
        
        Set<String> permissions = permissionService.getUserPermissions(userId);
        boolean hasPermission = permissions.contains(permissionCode);
        return ApiResponse.success(hasPermission);
    }
}