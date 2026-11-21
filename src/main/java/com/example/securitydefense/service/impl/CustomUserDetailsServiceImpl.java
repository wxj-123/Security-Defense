package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.entity.SysUserRole;
import com.example.securitydefense.entity.SysRole;
import com.example.securitydefense.entity.SysRolePermission;
import com.example.securitydefense.entity.SysPermission;
import com.example.securitydefense.mapper.UserMapper;
import com.example.securitydefense.mapper.SysUserRoleMapper;
import com.example.securitydefense.mapper.SysRoleMapper;
import com.example.securitydefense.mapper.SysRolePermissionMapper;
import com.example.securitydefense.mapper.SysPermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务实现类
 * 用于替代Spring Security默认的UserDetailsServiceAutoConfiguration
 */
@Slf4j
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private SysPermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 获取用户角色
        List<SysRole> roles = getUserRoles(user.getId());
        
        // 获取用户权限
        List<SysPermission> permissions = getUserPermissions(roles);
        
        // 构建权限列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色权限
        for (SysRole role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
        }
        
        // 添加具体权限
        for (SysPermission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
        }

        // 返回UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != null && user.getStatus() == 0)
                .build();
    }
    
    /**
     * 获取用户角色列表
     */
    private List<SysRole> getUserRoles(Long userId) {
        List<SysRole> roles = new ArrayList<>();
        
        // 查询用户角色关联
        QueryWrapper<SysUserRole> userRoleQuery = new QueryWrapper<>();
        userRoleQuery.eq("user_id", userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        
        if (!CollectionUtils.isEmpty(userRoles)) {
            // 获取角色ID列表
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            
            // 查询角色信息
            QueryWrapper<SysRole> roleQuery = new QueryWrapper<>();
            roleQuery.in("id", roleIds);
            roleQuery.eq("status", 1); // 只查询启用的角色
            roles = roleMapper.selectList(roleQuery);
        }
        
        return roles;
    }
    
    /**
     * 获取用户权限列表
     */
    private List<SysPermission> getUserPermissions(List<SysRole> roles) {
        List<SysPermission> permissions = new ArrayList<>();
        
        if (!CollectionUtils.isEmpty(roles)) {
            // 获取角色ID列表
            List<Long> roleIds = roles.stream()
                    .map(SysRole::getId)
                    .collect(Collectors.toList());
            
            // 查询角色权限关联
            QueryWrapper<SysRolePermission> rolePermissionQuery = new QueryWrapper<>();
            rolePermissionQuery.in("role_id", roleIds);
            List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermissionQuery);
            
            if (!CollectionUtils.isEmpty(rolePermissions)) {
                // 获取权限ID列表
                List<Long> permissionIds = rolePermissions.stream()
                        .map(SysRolePermission::getPermissionId)
                        .collect(Collectors.toList());
                
                // 查询权限信息
                QueryWrapper<SysPermission> permissionQuery = new QueryWrapper<>();
                permissionQuery.in("id", permissionIds);
                permissionQuery.eq("status", 1); // 只查询启用的权限
                permissions = permissionMapper.selectList(permissionQuery);
            }
        }
        
        return permissions;
    }
}