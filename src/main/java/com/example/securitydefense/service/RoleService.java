package com.example.securitydefense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.securitydefense.entity.SysRole;
import com.example.securitydefense.dto.RoleCreateDTO;
import com.example.securitydefense.dto.RoleUpdateDTO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<SysRole> {
    
    /**
     * 创建角色
     */
    boolean createRole(RoleCreateDTO roleCreateDTO, String operator);
    
    /**
     * 更新角色
     */
    boolean updateRole(RoleUpdateDTO roleUpdateDTO, String operator);
    
    /**
     * 删除角色
     */
    boolean deleteRole(Long roleId, String operator);
    
    /**
     * 分页查询角色
     */
    IPage<SysRole> queryRolePage(Integer current, Integer size, String roleName);
    
    /**
     * 获取角色权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
    
    /**
     * 分配角色权限
     */
    boolean assignRolePermissions(Long roleId, List<Long> permissionIds, String operator);
    
    /**
     * 获取所有角色（不带权限）
     */
    List<SysRole> getAllRoles();
}