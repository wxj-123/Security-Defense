package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.entity.SysRole;
import com.example.securitydefense.entity.SysRolePermission;
import com.example.securitydefense.dto.RoleCreateDTO;
import com.example.securitydefense.dto.RoleUpdateDTO;
import com.example.securitydefense.mapper.SysRoleMapper;
import com.example.securitydefense.mapper.SysRolePermissionMapper;
import com.example.securitydefense.service.RoleService;
import com.example.securitydefense.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Override
    @Transactional
    public boolean createRole(RoleCreateDTO roleCreateDTO, String operator) {
        // 检查角色名称是否已存在
        if (roleMapper.checkRoleNameExists(roleCreateDTO.getRoleName(), null) > 0) {
            throw new RuntimeException("角色名称已存在");
        }
        
        // 检查角色编码是否已存在
        if (roleMapper.checkRoleCodeExists(roleCreateDTO.getRoleCode(), null) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        
        // 创建角色对象
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleCreateDTO, role);
        role.setStatus(1); // 默认启用
        
        // 保存角色
        int result = roleMapper.insert(role);
        
        if (result > 0) {
            // 分配权限
            if (!CollectionUtils.isEmpty(roleCreateDTO.getPermissionIds())) {
                assignRolePermissions(role.getId(), roleCreateDTO.getPermissionIds(), operator);
            }
            
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "角色管理",
                "新增",
                String.format("创建角色 [%s]，编码 [%s]", role.getRoleName(), role.getRoleCode()),
                "成功",
                null
            );
            
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean updateRole(RoleUpdateDTO roleUpdateDTO, String operator) {
        SysRole role = getById(roleUpdateDTO.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        // 检查角色名称是否已存在（排除当前角色）
        if (roleMapper.checkRoleNameExists(roleUpdateDTO.getRoleName(), roleUpdateDTO.getId()) > 0) {
            throw new RuntimeException("角色名称已存在");
        }
        
        // 检查角色编码是否已存在（排除当前角色）
        if (roleMapper.checkRoleCodeExists(roleUpdateDTO.getRoleCode(), roleUpdateDTO.getId()) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        
        // 更新角色信息
        BeanUtils.copyProperties(roleUpdateDTO, role, "id", "createTime");
        role.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(role);
        
        if (result) {
            // 更新权限
            if (roleUpdateDTO.getPermissionIds() != null) {
                assignRolePermissions(role.getId(), roleUpdateDTO.getPermissionIds(), operator);
            }
            
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "角色管理",
                "编辑",
                String.format("更新角色 [%s] 信息", role.getRoleName()),
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean deleteRole(Long roleId, String operator) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        // 系统管理员角色不允许删除
        if ("admin".equals(role.getRoleCode())) {
            throw new RuntimeException("系统管理员角色不允许删除");
        }
        
        // 使用MyBatis-Plus的removeById方法进行逻辑删除
        // @TableLogic注解会自动处理deleted字段的更新
        boolean result = removeById(roleId);
        
        if (result) {
            // 删除角色权限关联
            rolePermissionMapper.deleteByRoleId(roleId);
            
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "角色管理",
                "删除",
                String.format("删除角色 [%s]", role.getRoleName()),
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    public IPage<SysRole> queryRolePage(Integer current, Integer size, String roleName) {
        Page<SysRole> page = new Page<>(current, size);
        return roleMapper.selectRolePage(page, roleName);
    }
    
    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public boolean assignRolePermissions(Long roleId, List<Long> permissionIds, String operator) {
        if (roleId == null) {
            return false;
        }
        
        // 先删除原有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 添加新的权限关联
        if (!CollectionUtils.isEmpty(permissionIds)) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                rolePermissionMapper.insert(rolePermission);
            }
        }
        
        return true;
    }
    
    @Override
    public List<SysRole> getAllRoles() {
        return roleMapper.selectList(null);
    }
}