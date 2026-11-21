package com.example.securitydefense.controller;

import com.example.securitydefense.common.Result;
import com.example.securitydefense.entity.SecurityEvent;
import com.example.securitydefense.service.SecurityEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 安全监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private SecurityEventService securityEventService;

    /**
     * 获取安全事件列表
     */
    @GetMapping("/events")
    public Result<Map<String, Object>> getEvents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level) {
        log.info("获取安全事件列表，页码: {}, 每页数量: {}, 类型: {}, 级别: {}", page, pageSize, type, level);
        
        // 使用数据库数据
        Map<String, Object> result = securityEventService.getSecurityEvents(page, pageSize, type, level);
        
        return Result.success(result);
    }

    /**
     * 获取安全事件详情
     */
    @GetMapping("/events/{id}")
    public Result<Map<String, Object>> getEventDetail(@PathVariable Long id) {
        log.info("获取安全事件详情，ID: {}", id);
        
        // 从数据库获取事件详情
        SecurityEvent event = securityEventService.getById(id);
        if (event == null) {
            return Result.error("事件不存在");
        }
        
        // 将实体转换为Map返回
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("id", event.getId());
        eventMap.put("type", event.getEventType());
        eventMap.put("level", event.getEventLevel());
        eventMap.put("source", event.getSourceIp());
        eventMap.put("target", event.getTargetIp());
        eventMap.put("description", event.getDescription());
        eventMap.put("time", event.getEventTime());
        eventMap.put("status", event.getStatus());
        
        // 如果目标端口存在，添加到返回数据中
        if (event.getTargetPort() != null) {
            eventMap.put("targetPort", event.getTargetPort());
        }
        
        // 如果源端口存在，添加到返回数据中
        if (event.getSourcePort() != null) {
            eventMap.put("sourcePort", event.getSourcePort());
        }
        
        // 如果协议存在，添加到返回数据中
        if (event.getProtocol() != null) {
            eventMap.put("protocol", event.getProtocol());
        }
        
        // 添加默认的payload字段（数据库中可能没有此字段，需要根据实际情况处理）
        eventMap.put("payload", event.getDescription());
        
        return Result.success(eventMap);
    }

    /**
     * 处理安全事件
     */
    @PostMapping("/events/{id}/handle")
    public Result<Void> handleEvent(@PathVariable Long id, @RequestBody Map<String, String> params) {
        log.info("处理安全事件，ID: {}, 操作: {}", id, params.get("action"));
        return Result.success(null);
    }

    /**
     * 获取实时日志流
     */
    @GetMapping("/logs/stream")
    public Result<List<Map<String, Object>>> getLogStream() {
        log.info("获取实时日志流");
        
        List<Map<String, Object>> logs = new ArrayList<>();
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] messages = {
            "用户登录成功",
            "检测到异常访问",
            "防火墙规则更新",
            "系统健康检查完成",
            "发现可疑流量"
        };
        
        for (int i = 0; i < 20; i++) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", i + 1);
            log.put("timestamp", LocalDateTime.now().minusSeconds(i * 3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.put("level", levels[i % levels.length]);
            log.put("message", messages[i % messages.length]);
            log.put("source", "192.168.1." + (100 + i % 50));
            logs.add(log);
        }
        
        return Result.success(logs);
    }

    /**
     * 获取网络流量监控数据
     */
    @GetMapping("/network-flow")
    public Result<Map<String, Object>> getNetworkFlow(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("获取网络流量监控数据");
        
        Map<String, Object> flow = new HashMap<>();
        Random random = new Random();
        
        List<Map<String, Object>> connections = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Map<String, Object> conn = new HashMap<>();
            conn.put("sourceIp", "192.168.1." + (10 + i));
            conn.put("destIp", "10.0.0." + (20 + i));
            conn.put("protocol", i % 3 == 0 ? "TCP" : (i % 3 == 1 ? "UDP" : "HTTP"));
            conn.put("port", 8080 + i);
            conn.put("bytes", random.nextInt(100000) + 10000);
            conn.put("packets", random.nextInt(1000) + 100);
            conn.put("status", i % 5 == 0 ? "异常" : "正常");
            connections.add(conn);
        }
        
        flow.put("connections", connections);
        flow.put("totalBytes", random.nextInt(10000000) + 1000000);
        flow.put("totalPackets", random.nextInt(100000) + 10000);
        
        return Result.success(flow);
    }

    /**
     * 获取异常行为检测结果
     */
    @GetMapping("/anomaly-detection")
    public Result<Map<String, Object>> getAnomalyDetection() {
        log.info("获取异常行为检测结果");
        
        Map<String, Object> detection = new HashMap<>();
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        String[] types = {"频繁访问", "异常时段访问", "大流量传输", "端口扫描", "暴力破解"};
        
        for (int i = 0; i < 8; i++) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("id", i + 1);
            anomaly.put("type", types[i % types.length]);
            anomaly.put("source", "192.168.1." + (50 + i));
            anomaly.put("confidence", 75 + new Random().nextInt(20));
            anomaly.put("time", LocalDateTime.now().minusMinutes(i * 10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            anomaly.put("severity", i % 3 == 0 ? "高" : (i % 3 == 1 ? "中" : "低"));
            anomalies.add(anomaly);
        }
        
        detection.put("anomalies", anomalies);
        detection.put("total", anomalies.size());
        detection.put("highSeverity", 2);
        detection.put("mediumSeverity", 3);
        detection.put("lowSeverity", 3);
        
        return Result.success(detection);
    }
}
