package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.SysBackup;
import com.example.securitydefense.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据备份控制器
 */
@RestController
@RequestMapping("/api/system-manage/backup")
public class BackupController {
    
    @Autowired
    private BackupService backupService;
    
    /**
     * 创建备份
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> createBackup(@RequestBody Map<String, String> params) {
        String backupName = params.get("backupName");
        String backupType = params.get("backupType");
        String description = params.get("description");
        String operator = "admin"; // 简化处理
        
        if (backupName == null || backupName.trim().isEmpty()) {
            backupName = "system_backup_" + System.currentTimeMillis();
        }
        
        if (backupType == null || backupType.trim().isEmpty()) {
            backupType = "manual";
        }
        
        boolean result = backupService.createBackup(backupName, backupType, operator, description);
        return result ? ApiResponse.success(true) : ApiResponse.error(500, "创建备份失败");
    }
    
    /**
     * 分页查询备份列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<IPage<SysBackup>> queryBackupPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        IPage<SysBackup> page = backupService.queryBackupPage(current, size);
        return ApiResponse.success(page);
    }
    
    /**
     * 恢复备份
     */
    @PostMapping("/{backupId}/restore")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> restoreBackup(@PathVariable Long backupId) {
        String operator = "admin"; // 简化处理
        
        boolean result = backupService.restoreBackup(backupId, operator);
        return result ? ApiResponse.success(true) : ApiResponse.error(500, "恢复备份失败");
    }
    
    /**
     * 删除备份
     */
    @DeleteMapping("/{backupId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> deleteBackup(@PathVariable Long backupId) {
        String operator = "admin"; // 简化处理
        
        boolean result = backupService.deleteBackup(backupId, operator);
        return result ? ApiResponse.success(true) : ApiResponse.error(500, "删除备份失败");
    }
    
    /**
     * 清理过期备份
     */
    @PostMapping("/clean-expired")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<String> cleanExpiredBackups(@RequestBody Map<String, Object> params) {
        Integer retentionDays = params.containsKey("retentionDays") ? 
            (Integer) params.get("retentionDays") : 30;
        
        backupService.cleanExpiredBackups(retentionDays);
        return ApiResponse.success("清理操作已提交，系统将在后台处理");
    }
}