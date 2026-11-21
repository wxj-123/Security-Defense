package com.example.securitydefense.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 简单健康检查 - 快速响应
     */
    @GetMapping
    public Map<String, Object> simpleHealthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("application", "Security-Defense");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 详细健康检查 - 包含数据库和Redis检查
     */
    @GetMapping("/check")
    public Map<String, Object> detailedHealthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("application", "Security-Defense");
        result.put("timestamp", System.currentTimeMillis());
        
        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            result.put("database", "Connected");
            result.put("databaseUrl", connection.getMetaData().getURL());
        } catch (Exception e) {
            result.put("database", "Error: " + e.getMessage());
            result.put("status", "DEGRADED");
        }
        
        // 检查Redis连接
        try {
            redisTemplate.opsForValue().set("health:check", "OK");
            String value = (String) redisTemplate.opsForValue().get("health:check");
            result.put("redis", "Connected - " + value);
        } catch (Exception e) {
            result.put("redis", "Error: " + e.getMessage());
            result.put("status", "DEGRADED");
        }
        
        return result;
    }
}
