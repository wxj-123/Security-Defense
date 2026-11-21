package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.dto.UserCreateDTO;
import com.example.securitydefense.dto.UserUpdateDTO;
import com.example.securitydefense.dto.UserQueryDTO;
import com.example.securitydefense.service.UserManageService;
import com.example.securitydefense.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/system-manage/user")
public class UserManageController {
    
    @Autowired
    private UserManageService userManageService;
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 创建用户
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        // 获取当前用户名（实际应用中可以从安全上下文中获取）
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = userManageService.createUser(userCreateDTO, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "创建用户失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 更新用户
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = userManageService.updateUser(userUpdateDTO, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "更新用户失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> deleteUser(@PathVariable Long userId) {
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = userManageService.deleteUser(userId, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "删除用户失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> resetPassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPassword = params.get("newPassword").toString();
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = userManageService.resetPassword(userId, newPassword, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "重置密码失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 更新用户状态（启用/禁用）
     */
    @PostMapping("/update-status")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> updateUserStatus(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String operator = "admin"; // 简化处理
        
        try {
            boolean result = userManageService.updateUserStatus(userId, status, operator);
            return result ? ApiResponse.success(true) : ApiResponse.error(500, "更新用户状态失败");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 分页查询用户
     */
    @PostMapping("/page")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<IPage<User>> queryUserPage(@RequestBody UserQueryDTO queryDTO) {
        IPage<User> page = userManageService.queryUserPage(queryDTO);
        return ApiResponse.success(page);
    }
    
    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<User> getUserById(@PathVariable Long userId) {
        User user = userManageService.getById(userId);
        return ApiResponse.success(user);
    }
    
    /**
     * 获取用户角色ID列表
     */
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        List<Long> roleIds = userManageService.getUserRoleIds(userId);
        return ApiResponse.success(roleIds);
    }
    
    /**
     * 分配用户角色
     */
    @PostMapping("/{userId}/assign-roles")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> assignUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        String operator = "admin"; // 简化处理
        
        boolean result = userManageService.assignUserRoles(userId, roleIds, operator);
        return result ? ApiResponse.success(true) : ApiResponse.error(500, "分配用户角色失败");
    }
}