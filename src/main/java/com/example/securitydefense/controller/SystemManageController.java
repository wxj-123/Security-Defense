package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.SysConfig;
import com.example.securitydefense.dto.ConfigUpdateDTO;
import com.example.securitydefense.service.SystemService;
import com.example.securitydefense.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 */
@RestController
@RequestMapping("/api/system-manage")
public class SystemManageController {
    
    @Autowired
    private SystemService systemService;
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 获取所有配置
     */
    @GetMapping("/config/list")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<SysConfig>> listConfig() {
        List<SysConfig> configs = systemService.list();
        return ApiResponse.success(configs);
    }
    
    /**
     * 根据类型获取配置
     */
    @GetMapping("/config/type/{configType}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<SysConfig>> getConfigByType(@PathVariable String configType) {
        List<SysConfig> configs = systemService.getConfigByType(configType);
        return ApiResponse.success(configs);
    }
    
    /**
     * 根据键获取配置值
     */
    @GetMapping("/config/key/{configKey}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<String> getConfigByKey(@PathVariable String configKey) {
        String value = systemService.getConfigValue(configKey);
        return ApiResponse.success(value);
    }
    
    /**
     * 更新配置
     */
    @PostMapping("/config/update")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Boolean> updateConfig(@Valid @RequestBody ConfigUpdateDTO configUpdateDTO) {
        // 获取当前用户名（实际应用中可以从安全上下文中获取）
        String operator = "admin"; // 简化处理
        
        boolean result = systemService.updateConfig(configUpdateDTO, operator);
        return result ? ApiResponse.success(true) : ApiResponse.error(500, "更新配置失败");
    }
    
    /**
     * 测试数据库连接
     */
    @GetMapping("/connection/test/database")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Map<String, Object>> testDatabaseConnection() {
        boolean result = systemService.testDatabaseConnection();
        Map<String, Object> dbConnectionResult = new HashMap<>();
        dbConnectionResult.put("database", result ? "连接成功" : "连接失败");
        dbConnectionResult.put("status", result);
        return ApiResponse.success(dbConnectionResult);
    }
    
    /**
     * 测试Redis连接
     */
    @GetMapping("/connection/test/redis")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Map<String, Object>> testRedisConnection() {
        boolean result = systemService.testRedisConnection();
        Map<String, Object> redisConnectionResult = new HashMap<>();
        redisConnectionResult.put("redis", result ? "连接成功" : "连接失败");
        redisConnectionResult.put("status", result);
        return ApiResponse.success(redisConnectionResult);
    }
    
    /**
     * 系统信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Map<String, Object>> getSystemInfo() {
        // 获取系统信息
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        // 创建系统信息Map
        Map<String, Object> systemInfo = new HashMap<>();
        
        // JVM信息
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("maxMemory", maxMemory);
        jvmInfo.put("totalMemory", totalMemory);
        jvmInfo.put("usedMemory", usedMemory);
        jvmInfo.put("freeMemory", freeMemory);
        systemInfo.put("jvm", jvmInfo);
        
        // 操作系统信息
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", System.getProperty("os.name"));
        osInfo.put("version", System.getProperty("os.version"));
        osInfo.put("arch", System.getProperty("os.arch"));
        systemInfo.put("os", osInfo);
        
        // Java信息
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        javaInfo.put("home", System.getProperty("java.home"));
        systemInfo.put("java", javaInfo);
        
        return ApiResponse.success(systemInfo);
    }
}