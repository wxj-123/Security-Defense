package com.example.securitydefense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.dto.UserCreateDTO;
import com.example.securitydefense.dto.UserUpdateDTO;
import com.example.securitydefense.dto.UserQueryDTO;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface UserManageService extends IService<User> {
    
    /**
     * 创建用户
     */
    boolean createUser(UserCreateDTO userCreateDTO, String operator);
    
    /**
     * 更新用户
     */
    boolean updateUser(UserUpdateDTO userUpdateDTO, String operator);
    
    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long userId, String operator);
    
    /**
     * 重置密码
     */
    boolean resetPassword(Long userId, String newPassword, String operator);
    
    /**
     * 启用/禁用用户
     */
    boolean updateUserStatus(Long userId, Integer status, String operator);
    
    /**
     * 分页查询用户
     */
    IPage<User> queryUserPage(UserQueryDTO queryDTO);
    
    /**
     * 获取用户角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);
    
    /**
     * 分配用户角色
     */
    boolean assignUserRoles(Long userId, List<Long> roleIds, String operator);
}