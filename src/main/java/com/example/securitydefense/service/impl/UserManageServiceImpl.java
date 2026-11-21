package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.entity.SysUserRole;
import com.example.securitydefense.dto.UserCreateDTO;
import com.example.securitydefense.dto.UserUpdateDTO;
import com.example.securitydefense.dto.UserQueryDTO;
import com.example.securitydefense.mapper.UserMapper;
import com.example.securitydefense.mapper.SysUserRoleMapper;
import com.example.securitydefense.service.UserManageService;
import com.example.securitydefense.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理服务实现类
 */
@Slf4j
@Service
public class UserManageServiceImpl extends ServiceImpl<UserMapper, User> implements UserManageService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Override
    @Transactional
    public boolean createUser(UserCreateDTO userCreateDTO, String operator) {
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userCreateDTO.getUsername());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(userCreateDTO, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setStatus(1); // 默认启用
        
        // 保存用户
        int result = userMapper.insert(user);
        
        if (result > 0) {
            // 分配角色
            if (!CollectionUtils.isEmpty(userCreateDTO.getRoleIds())) {
                assignUserRoles(user.getId(), userCreateDTO.getRoleIds(), operator);
            }
            
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "用户管理",
                "新增",
                String.format("创建用户 [%s]，部门 [%s]", user.getUsername(), userCreateDTO.getDepartment()),
                "成功",
                null
            );
            
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean updateUser(UserUpdateDTO userUpdateDTO, String operator) {
        User user = getById(userUpdateDTO.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(userUpdateDTO, user, "id", "username", "password");
        user.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(user);
        
        if (result) {
            // 更新角色
            if (userUpdateDTO.getRoleIds() != null) {
                assignUserRoles(user.getId(), userUpdateDTO.getRoleIds(), operator);
            }
            
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "用户管理",
                "编辑",
                String.format("更新用户 [%s] 信息", user.getUsername()),
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long userId, String operator) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 逻辑删除用户
        user.setDeleted(1);
        boolean result = updateById(user);
        
        if (result) {
            // 删除用户角色关联
            userRoleMapper.deleteByUserId(userId);
            
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "用户管理",
                "删除",
                String.format("删除用户 [%s]", user.getUsername()),
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    public boolean resetPassword(Long userId, String newPassword, String operator) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(user);
        
        if (result) {
            // 记录操作日志
            operationLogService.logOperation(
                operator,
                "用户管理",
                "重置密码",
                String.format("重置用户 [%s] 密码", user.getUsername()),
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    public boolean updateUserStatus(Long userId, Integer status, String operator) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(user);
        
        if (result) {
            // 记录操作日志
            String statusText = status == 1 ? "启用" : "禁用";
            operationLogService.logOperation(
                operator,
                "用户管理",
                statusText,
                String.format("%s用户 [%s]", statusText, user.getUsername()),
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    public IPage<User> queryUserPage(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        return userMapper.selectUserPage(page, queryDTO);
    }
    
    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean assignUserRoles(Long userId, List<Long> roleIds, String operator) {
        if (userId == null) {
            return false;
        }
        
        // 先删除原有角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 添加新的角色关联
        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
        
        return true;
    }
}