package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.entity.SysPermission;
import com.example.securitydefense.mapper.SysPermissionMapper;
import com.example.securitydefense.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements PermissionService {
    
    @Autowired
    private SysPermissionMapper permissionMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public List<SysPermission> getPermissionTree() {
        // 从缓存获取
        try {
            List<SysPermission> permissions = (List<SysPermission>) redisTemplate.opsForValue().get("permission:tree");
            if (!CollectionUtils.isEmpty(permissions)) {
                return permissions;
            }
        } catch (Exception e) {
            log.warn("Failed to get permission tree from cache, will query from database", e);
        }
        
        // 数据库查询
        List<SysPermission> allPermissions = permissionMapper.selectPermissionTree();
        if (allPermissions == null) {
            allPermissions = new ArrayList<>();
        }
        
        // 构建树形结构
        List<SysPermission> tree = buildPermissionTree(allPermissions, 0L);
        
        // 存入缓存，过期时间30分钟
        try {
            redisTemplate.opsForValue().set("permission:tree", tree, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to save permission tree to cache", e);
        }
        
        return tree;
    }
    
    @Override
    public Set<String> getUserPermissions(Long userId) {
        // 从缓存获取
        try {
            String cacheKey = "user:permissions:" + userId;
            Set<String> permissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (!CollectionUtils.isEmpty(permissions)) {
                return permissions;
            }
        } catch (Exception e) {
            log.warn("Failed to get user permissions from cache, will query from database", e);
        }
        
        // 数据库查询
        List<SysPermission> permissionList = permissionMapper.selectPermissionsByUserId(userId);
        if (permissionList == null) {
            permissionList = new ArrayList<>();
        }
        
        // 转换为权限编码集合
        Set<String> permissionCodes = permissionList.stream()
                .filter(permission -> StringUtils.hasText(permission.getPermissionCode()))
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());
        
        // 存入缓存，过期时间30分钟
        try {
            String cacheKey = "user:permissions:" + userId;
            redisTemplate.opsForValue().set(cacheKey, permissionCodes, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to save user permissions to cache", e);
        }
        
        return permissionCodes;
    }
    
    @Override
    public List<SysPermission> getUserMenus(Long userId) {
        // 从缓存获取
        try {
            String cacheKey = "user:menus:" + userId;
            List<SysPermission> menus = (List<SysPermission>) redisTemplate.opsForValue().get(cacheKey);
            if (!CollectionUtils.isEmpty(menus)) {
                return menus;
            }
        } catch (Exception e) {
            log.warn("Failed to get user menus from cache, will query from database", e);
        }
        
        // 数据库查询
        List<SysPermission> permissionList = permissionMapper.selectPermissionsByUserId(userId);
        if (permissionList == null) {
            permissionList = new ArrayList<>();
        }
        
        // 过滤出菜单权限
        List<SysPermission> menuList = permissionList.stream()
                .filter(permission -> "menu".equals(permission.getResourceType()))
                .collect(Collectors.toList());
        
        // 构建菜单树
        List<SysPermission> menuTree = buildPermissionTree(menuList, 0L);
        
        // 存入缓存，过期时间30分钟
        try {
            String cacheKey = "user:menus:" + userId;
            redisTemplate.opsForValue().set(cacheKey, menuTree, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to save user menus to cache", e);
        }
        
        return menuTree;
    }
    
    /**
     * 构建权限树
     */
    private List<SysPermission> buildPermissionTree(List<SysPermission> permissions, Long parentId) {
        if (CollectionUtils.isEmpty(permissions)) {
            return new ArrayList<>();
        }
        
        // 找出所有子节点
        List<SysPermission> children = permissions.stream()
                .filter(permission -> Objects.equals(permission.getParentId(), parentId))
                .sorted(Comparator.comparing(SysPermission::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        
        // 递归构建子树
        for (SysPermission child : children) {
            List<SysPermission> grandChildren = buildPermissionTree(permissions, child.getId());
            child.setChildren(grandChildren);
        }
        
        return children;
    }
}