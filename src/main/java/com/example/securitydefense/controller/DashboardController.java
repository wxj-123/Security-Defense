package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.SecurityEvent;
import com.example.securitydefense.mapper.SecurityEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 仪表盘控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private SecurityEventMapper securityEventMapper;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        log.info("获取仪表盘统计数据");

        try {
            // 统计总事件数
            Long totalEvents = securityEventMapper.selectCount(null);

            // 统计严重事件数
            LambdaQueryWrapper<SecurityEvent> criticalQuery = new LambdaQueryWrapper<>();
            criticalQuery.eq(SecurityEvent::getEventLevel, "critical");
            Long criticalEvents = securityEventMapper.selectCount(criticalQuery);

            // 统计已拦截的攻击（状态为resolved的事件）
            LambdaQueryWrapper<SecurityEvent> resolvedQuery = new LambdaQueryWrapper<>();
            resolvedQuery.eq(SecurityEvent::getStatus, "resolved");
            Long blockedAttacks = securityEventMapper.selectCount(resolvedQuery);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEvents", totalEvents != null ? totalEvents : 0);
            stats.put("criticalEvents", criticalEvents != null ? criticalEvents : 0);
            stats.put("blockedAttacks", blockedAttacks != null ? blockedAttacks : 0);
            stats.put("systemHealth", 98); // 系统健康度暂时保留固定值

            Map<String, Object> networkTraffic = new HashMap<>();
            networkTraffic.put("inbound", 1234); // 暂时保留固定值
            networkTraffic.put("outbound", 567); // 暂时保留固定值
            stats.put("networkTraffic", networkTraffic);

            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            // 返回默认值
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEvents", 0);
            stats.put("criticalEvents", 0);
            stats.put("blockedAttacks", 0);
            stats.put("systemHealth", 98);

            Map<String, Object> networkTraffic = new HashMap<>();
            networkTraffic.put("inbound", 0);
            networkTraffic.put("outbound", 0);
            stats.put("networkTraffic", networkTraffic);

            return ApiResponse.success(stats);
        }
    }

    /**
     * 获取最近安全事件
     */
    @GetMapping("/recent-events")
    public ApiResponse<List<Map<String, Object>>> getRecentEvents(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取最近安全事件，限制数量: {}", limit);

        try {
            // 查询最近的安全事件，按事件时间降序排序
            LambdaQueryWrapper<SecurityEvent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(SecurityEvent::getEventTime);

            // 分页查询
            Page<SecurityEvent> page = new Page<>(1, limit);
            IPage<SecurityEvent> eventPage = securityEventMapper.selectPage(page, queryWrapper);

            // 转换为前端需要的格式
            List<Map<String, Object>> events = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (SecurityEvent event : eventPage.getRecords()) {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", event.getId());
                // 转换事件类型为中文
                eventMap.put("type", convertEventTypeToChinese(event.getEventType()));
                // 转换事件等级为小写
                eventMap.put("level", convertEventLevel(event.getEventLevel()));
                eventMap.put("source", event.getSourceIp());
                eventMap.put("target", event.getTargetIp());
                eventMap.put("description", event.getDescription());
                eventMap.put("timestamp", event.getEventTime() != null ?
                    dateFormat.format(event.getEventTime()) :
                    (event.getCreateTime() != null ? dateFormat.format(event.getCreateTime()) : ""));
                // 转换状态为小写
                eventMap.put("status", convertStatus(event.getStatus()));
                events.add(eventMap);
            }

            log.info("从数据库查询到 {} 条安全事件", events.size());
            return ApiResponse.success(events);

        } catch (Exception e) {
            log.error("获取最近安全事件失败", e);
            // 返回空列表
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 转换事件类型为中文
     */
    private String convertEventTypeToChinese(String eventType) {
        if (eventType == null) return "未知事件";

        switch (eventType) {
            case "PORT_SCAN": return "端口扫描";
            case "BRUTE_FORCE": return "暴力破解";
            case "DDOS": return "DDoS攻击";
            case "MALWARE": return "恶意软件";
            case "SQL_INJECTION": return "SQL注入";
            case "XSS": return "XSS攻击";
            case "PHISHING": return "钓鱼攻击";
            default: return eventType;
        }
    }

    /**
     * 转换事件等级为小写
     */
    private String convertEventLevel(String eventLevel) {
        if (eventLevel == null) return "medium";

        switch (eventLevel) {
            case "CRITICAL": return "critical";
            case "HIGH": return "high";
            case "WARNING": return "medium";
            case "INFO": return "low";
            default: return eventLevel.toLowerCase();
        }
    }

    /**
     * 转换状态为小写
     */
    private String convertStatus(String status) {
        if (status == null) return "pending";

        switch (status) {
            case "PENDING": return "pending";
            case "PROCESSING": return "processing";
            case "RESOLVED": return "resolved";
            case "IGNORED": return "ignored";
            default: return status.toLowerCase();
        }
    }

    /**
     * 获取攻击流量趋势
     */
    @GetMapping("/attack-trend")
    public ApiResponse<Map<String, Object>> getAttackTrend(@RequestParam(defaultValue = "24h") String period) {
        log.info("获取攻击流量趋势，周期: {}", period);
        
        Map<String, Object> trend = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        Random random = new Random();
        int points = period.equals("7d") ? 7 : (period.equals("24h") ? 24 : 12);
        
        for (int i = 0; i < points; i++) {
            labels.add(period.equals("7d") ? "第" + (i+1) + "天" : i + ":00");
            values.add(random.nextInt(100) + 50);
        }
        
        trend.put("labels", labels);
        trend.put("values", values);
        
        return ApiResponse.success(trend);
    }

    /**
     * 获取威胁分布
     */
    @GetMapping("/threat-distribution")
    public ApiResponse<List<Map<String, Object>>> getThreatDistribution() {
        log.info("获取威胁分布");

        try {
            // 查询所有安全事件
            List<SecurityEvent> allEvents = securityEventMapper.selectList(null);

            // 按事件类型统计数量
            Map<String, Long> typeCountMap = new HashMap<>();
            for (SecurityEvent event : allEvents) {
                String eventType = event.getEventType();
                if (eventType != null && !eventType.isEmpty()) {
                    typeCountMap.put(eventType, typeCountMap.getOrDefault(eventType, 0L) + 1);
                }
            }

            // 转换为前端需要的格式，使用中文类型名称
            List<Map<String, Object>> distribution = new ArrayList<>();
            for (Map.Entry<String, Long> entry : typeCountMap.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                // 使用中文类型名称
                item.put("type", convertEventTypeToChinese(entry.getKey()));
                item.put("count", entry.getValue());
                distribution.add(item);
            }

            log.info("威胁分布统计完成，共 {} 种威胁类型", distribution.size());
            return ApiResponse.success(distribution);

        } catch (Exception e) {
            log.error("获取威胁分布失败", e);
            // 返回空列表
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/system-health")
    public ApiResponse<Map<String, Object>> getSystemHealth() {
        log.info("获取系统健康状态");
        
        Map<String, Object> health = new HashMap<>();
        health.put("cpu", 45.2);
        health.put("memory", 62.8);
        health.put("disk", 38.5);
        health.put("network", 23.7);
        health.put("firewall", "正常");
        health.put("ids", "正常");
        health.put("database", "正常");
        
        return ApiResponse.success(health);
    }

    /**
     * 获取网络流量统计
     */
    @GetMapping("/network-traffic")
    public ApiResponse<Map<String, Object>> getNetworkTraffic(@RequestParam String timeRange) {
        log.info("获取网络流量统计，时间范围: {}", timeRange);
        
        Map<String, Object> traffic = new HashMap<>();
        Random random = new Random();
        
        List<String> timestamps = new ArrayList<>();
        List<Double> inbound = new ArrayList<>();
        List<Double> outbound = new ArrayList<>();
        
        for (int i = 0; i < 20; i++) {
            timestamps.add(LocalDateTime.now().minusMinutes(20 - i).format(DateTimeFormatter.ofPattern("HH:mm")));
            inbound.add(random.nextDouble() * 1000 + 500);
            outbound.add(random.nextDouble() * 800 + 300);
        }
        
        traffic.put("timestamps", timestamps);
        traffic.put("inbound", inbound);
        traffic.put("outbound", outbound);
        
        return ApiResponse.success(traffic);
    }

    /**
     * 获取资产地理分布数据
     */
    @GetMapping("/asset-geographic")
    public ApiResponse<Map<String, Object>> getAssetGeographicDistribution() {
        log.info("获取资产地理分布数据");
        
        try {
            // 城市坐标映射
            Map<String, double[]> cityCoordinates = new HashMap<>();
            cityCoordinates.put("北京数据中心", new double[]{116.4074, 39.9042});
            cityCoordinates.put("上海机房", new double[]{121.4737, 31.2304});
            cityCoordinates.put("广州办公室", new double[]{113.2644, 23.1291});
            cityCoordinates.put("深圳研发中心", new double[]{114.0579, 22.5431});
            cityCoordinates.put("成都分部", new double[]{104.0668, 30.5728});
            cityCoordinates.put("武汉分公司", new double[]{114.3055, 30.5928});
            
            String[] assetTypes = {"服务器", "网络设备", "工作站", "数据库", "应用系统"};
            List<String> locations = new ArrayList<>(cityCoordinates.keySet());
            
            // 生成资产列表
            List<Map<String, Object>> assets = new ArrayList<>();
            Random random = new Random();
            
            for (int i = 0; i < 50; i++) {
                String location = locations.get(i % locations.size());
                double[] coords = cityCoordinates.get(location);
                int riskScore = random.nextInt(100);
                
                // 根据风险分数确定风险等级
                String riskLevel;
                if (riskScore >= 80) {
                    riskLevel = "CRITICAL";
                } else if (riskScore >= 60) {
                    riskLevel = "HIGH";
                } else if (riskScore >= 40) {
                    riskLevel = "MEDIUM";
                } else {
                    riskLevel = "LOW";
                }
                
                Map<String, Object> asset = new HashMap<>();
                asset.put("assetId", "asset_" + (i + 1));
                asset.put("assetName", assetTypes[i % assetTypes.length] + "_" + (i + 1));
                asset.put("assetType", assetTypes[i % assetTypes.length]);
                asset.put("riskScore", riskScore);
                asset.put("riskLevel", riskLevel);
                asset.put("location", location);
                asset.put("longitude", coords[0] + (random.nextDouble() - 0.5) * 0.1);
                asset.put("latitude", coords[1] + (random.nextDouble() - 0.5) * 0.1);
                asset.put("ipAddress", "192.168." + (i / 10) + "." + (i % 256));
                asset.put("status", "ACTIVE");
                assets.add(asset);
            }
            
            // 生成位置统计数据
            Map<String, Object> locationStats = new HashMap<>();
            
            for (String location : locations) {
                double[] coords = cityCoordinates.get(location);
                
                // 过滤该位置的资产
                List<Map<String, Object>> locationAssets = new ArrayList<>();
                int highRiskCount = 0;
                double totalRisk = 0;
                
                for (Map<String, Object> asset : assets) {
                    if (location.equals(asset.get("location"))) {
                        locationAssets.add(asset);
                        int risk = (Integer) asset.get("riskScore");
                        totalRisk += risk;
                        if (risk >= 60) {
                            highRiskCount++;
                        }
                    }
                }
                
                double avgRisk = locationAssets.isEmpty() ? 0 : totalRisk / locationAssets.size();
                
                // 生成该位置的前5个资产列表
                List<Map<String, Object>> topAssets = new ArrayList<>();
                int limit = Math.min(5, locationAssets.size());
                for (int i = 0; i < limit; i++) {
                    Map<String, Object> asset = locationAssets.get(i);
                    Map<String, Object> topAsset = new HashMap<>();
                    topAsset.put("name", asset.get("assetName"));
                    topAsset.put("riskScore", asset.get("riskScore"));
                    topAsset.put("value", asset.get("riskScore"));
                    topAssets.add(topAsset);
                }
                
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", location);
                stat.put("value", (int) Math.round(avgRisk));
                stat.put("longitude", coords[0]);
                stat.put("latitude", coords[1]);
                stat.put("assetCount", locationAssets.size());
                stat.put("highRiskCount", highRiskCount);
                stat.put("assets", topAssets);
                
                locationStats.put(location, stat);
            }
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("assets", assets);
            data.put("locationStats", locationStats);
            
            return ApiResponse.success(data);
            
        } catch (Exception e) {
            log.error("获取资产地理分布数据失败", e);
            return ApiResponse.error(500, "获取资产地理分布数据失败");
        }
    }
}
