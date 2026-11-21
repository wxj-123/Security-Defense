package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.SysOperationLog;
import com.example.securitydefense.dto.OperationLogQueryDTO;
import com.example.securitydefense.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system-manage/operation-log")
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 分页查询操作日志
     */
    @PostMapping("/page")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<IPage<SysOperationLog>> queryOperationLogPage(@Valid @RequestBody OperationLogQueryDTO queryDTO) {
        IPage<SysOperationLog> page = operationLogService.queryOperationLogPage(queryDTO);
        return ApiResponse.success(page);
    }
    
    /**
     * 根据ID获取日志详情
     */
    @GetMapping("/{logId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<SysOperationLog> getLogById(@PathVariable Long logId) {
        SysOperationLog log = operationLogService.getById(logId);
        return ApiResponse.success(log);
    }
    
    /**
     * 清理过期日志
     */
    @PostMapping("/clean-expired")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<String> cleanExpiredLogs(@RequestBody Map<String, Object> params) {
        Integer retentionDays = params.containsKey("retentionDays") ? 
            (Integer) params.get("retentionDays") : 180;
        
        operationLogService.cleanExpiredLogs(retentionDays);
        return ApiResponse.success("清理操作已提交，系统将在后台处理");
    }
}