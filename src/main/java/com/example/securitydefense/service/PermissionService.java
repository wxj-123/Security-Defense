package com.example.securitydefense.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.securitydefense.entity.SysPermission;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<SysPermission> {
    
    /**
     * 获取权限树形结构
     */
    List<SysPermission> getPermissionTree();
    
    /**
     * 获取用户权限编码集合
     */
    Set<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户菜单权限
     */
    List<SysPermission> getUserMenus(Long userId);
}