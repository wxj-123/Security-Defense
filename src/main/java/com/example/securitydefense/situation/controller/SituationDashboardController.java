package com.example.securitydefense.situation.controller;

import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.situation.evaluation.SituationEvaluator;
import com.example.securitydefense.situation.service.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

/**
 * 安全态势仪表盘控制器
 * 提供可视化展示所需的数据接口
 */
@RestController
@RequestMapping("/api/situation-dashboard")
public class SituationDashboardController {
    
    @Autowired
    private SituationEvaluator situationEvaluator;
    
    @Autowired
    private VisualizationService visualizationService;
    
    /**
     * 获取当前安全态势总览
     * @return 安全态势数据
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getSituationOverview() {
        // 生成模拟态势数据，与前端数据结构匹配
        Map<String, Object> situationData = new HashMap<>();
        
        int overallScore = 75 + (int)(Math.random() * 20);
        situationData.put("overallScore", overallScore);
        situationData.put("situationScore", overallScore / 100.0); // 转换为小数格式
        situationData.put("situationIndex", 65 + (int)(Math.random() * 30));
        situationData.put("situationLevel", "MEDIUM");
        situationData.put("timestamp", new Date());
        situationData.put("totalAssets", 120 + (int)(Math.random() * 50));
        situationData.put("affectedAssets", 15 + (int)(Math.random() * 25));
        situationData.put("affectedAssetCount", 15 + (int)(Math.random() * 25));
        situationData.put("activeThreats", 8 + (int)(Math.random() * 12));
        situationData.put("activeThreatCount", 8 + (int)(Math.random() * 12));
        situationData.put("openVulnerabilities", 45 + (int)(Math.random() * 35));
        situationData.put("highVulnerabilityCount", 45 + (int)(Math.random() * 35));
        situationData.put("securityEvents", 25 + (int)(Math.random() * 20));
        
        // 维度评分
        Map<String, Double> dimensionScores = new HashMap<>();
        dimensionScores.put("threatDetection", 75.0 + (Math.random() * 20.0));
        dimensionScores.put("vulnerability", 70.0 + (Math.random() * 25.0));
        dimensionScores.put("assetSecurity", 65.0 + (Math.random() * 30.0));
        dimensionScores.put("networkTraffic", 80.0 + (Math.random() * 15.0));
        dimensionScores.put("securityConfig", 85.0 + (Math.random() * 10.0));
        situationData.put("dimensionScores", dimensionScores);
        
        return ApiResponse.success(situationData);
    }
    
    /**
     * 获取态势历史趋势数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return 历史态势数据列表
     */
    @GetMapping("/trend")
    public ApiResponse<List<Map<String, Object>>> getSituationTrend(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "60") int interval) {
        // 解析ISO日期字符串
        Date startDateTime;
        Date endDateTime;
        try {
            startDateTime = Date.from(Instant.parse(startTime));
        } catch (Exception e) {
            return ApiResponse.error(400, "无效的开始时间格式: " + startTime);
        }
        
        try {
            endDateTime = Date.from(Instant.parse(endTime));
        } catch (Exception e) {
            return ApiResponse.error(400, "无效的结束时间格式: " + endTime);
        }
        
        // 生成模拟趋势数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        // 生成过去24小时的趋势数据
        long endTimeMs = endDateTime.getTime();
        long startTimeMs = startDateTime.getTime();
        long intervalMs = interval * 60 * 1000L; // 转换为毫秒
        
        for (long time = startTimeMs; time <= endTimeMs; time += intervalMs) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("timestamp", new Date(time));
            dataPoint.put("situationIndex", 65 + (int)(Math.random() * 30));
            dataPoint.put("activeThreats", 5 + (int)(Math.random() * 15));
            trendData.add(dataPoint);
        }
        
        return ApiResponse.success(trendData);
    }
    
    /**
     * 获取资产风险分布
     * @return 资产风险数据
     */
    @GetMapping("/asset-risk")
    public ApiResponse<List<Map<String, Object>>> getAssetRiskDistribution() {
        // 生成模拟资产风险数据
        List<Map<String, Object>> assetRisks = new ArrayList<>();
        
        String[] assetTypes = {"服务器", "数据库", "网络设备", "工作站", "存储设备"};
        String[] locations = {"数据中心A区", "数据中心B区", "办公区", "生产区", "测试区"};
        String[] riskLevels = {"CRITICAL", "HIGH", "MEDIUM", "LOW", "SAFE"};
        
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> asset = new HashMap<>();
            asset.put("assetId", "ASSET-" + String.format("%03d", i));
            asset.put("assetName", assetTypes[i % assetTypes.length] + "-" + String.format("%02d", i));
            asset.put("assetType", assetTypes[i % assetTypes.length]);
            asset.put("riskScore", 10 + (int)(Math.random() * 90));
            asset.put("riskLevel", riskLevels[(int)(Math.random() * riskLevels.length)]);
            asset.put("location", locations[i % locations.length]);
            asset.put("ipAddress", "192.168." + (i / 10 + 1) + "." + (i % 10 + 1));
            asset.put("lastScanTime", new Date().toString());
            asset.put("status", "ACTIVE");
            
            // 添加缺失的字段
            asset.put("assetValueScore", 70 + (int)(Math.random() * 30));
            asset.put("vulnerabilityScore", 60 + (int)(Math.random() * 40));
            asset.put("threatScore", 50 + (int)(Math.random() * 50));
            asset.put("vulnerabilityCount", (int)(Math.random() * 20));
            asset.put("threatCount", (int)(Math.random() * 10));
            
            assetRisks.add(asset);
        }
        
        return ApiResponse.success(assetRisks);
    }
    
    /**
     * 获取威胁情报概览
     * @param limit 限制数量
     * @return 威胁情报数据
     */
    @GetMapping("/threats")
    public ApiResponse<List<Map<String, Object>>> getThreatIntelligence(
            @RequestParam(defaultValue = "10") int limit) {
        // 生成模拟威胁情报数据
        List<Map<String, Object>> threats = new ArrayList<>();
        
        String[] threatTypes = {"恶意软件", "钓鱼网站", "DDoS攻击", "SQL注入", "XSS攻击"};
        String[] iocTypes = {"IP", "DOMAIN", "URL", "FILE_HASH", "EMAIL"};
        String[] severities = {"CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"};
        String[] sources = {"内部IDS", "外部威胁情报", "沙箱分析", "用户举报", "自动化扫描"};
        
        for (int i = 1; i <= limit && i <= 50; i++) {
            Map<String, Object> threat = new HashMap<>();
            threat.put("threatId", "THREAT-" + String.format("%04d", i));
            threat.put("threatType", threatTypes[i % threatTypes.length]);
            threat.put("iocType", iocTypes[i % iocTypes.length]);
            threat.put("iocValue", generateIOCValue(iocTypes[i % iocTypes.length], i));
            threat.put("severity", severities[(int)(Math.random() * severities.length)]);
            threat.put("confidenceScore", 50 + (int)(Math.random() * 50));
            threat.put("source", sources[i % sources.length]);
            threat.put("discoveredTime", new Date(System.currentTimeMillis() - (long)(Math.random() * 7 * 24 * 60 * 60 * 1000)).toString());
            threat.put("status", "ACTIVE");
            
            // 添加缺失的字段
            threat.put("firstSeen", new Date(System.currentTimeMillis() - (long)(Math.random() * 30 * 24 * 60 * 60 * 1000)).toString());
            threat.put("lastUpdated", new Date().toString());
            threat.put("description", threatTypes[i % threatTypes.length] + "威胁情报");
            threat.put("affectedSystems", new ArrayList<String>());
            threat.put("relatedCves", new ArrayList<String>());
            threat.put("tags", Arrays.asList(threatTypes[i % threatTypes.length], iocTypes[i % iocTypes.length]));
            threat.put("recommendations", new ArrayList<String>());
            
            threats.add(threat);
        }
        
        return ApiResponse.success(threats);
    }
    
    /**
     * 生成模拟IOC值
     */
    private String generateIOCValue(String iocType, int index) {
        switch (iocType) {
            case "IP":
                return (192 + (int)(Math.random() * 64)) + "." + (int)(Math.random() * 256) + "." + (int)(Math.random() * 256) + "." + (int)(Math.random() * 256);
            case "DOMAIN":
                return "malicious-domain-" + index + ".com";
            case "URL":
                return "http://malicious-site-" + index + ".com/payload";
            case "FILE_HASH":
                return "a1b2c3d4e5f6" + String.format("%04d", index);
            case "EMAIL":
                return "threat" + index + "@malicious.com";
            default:
                return "unknown-ioc-" + index;
        }
    }
    
    /**
     * 获取态势维度分析
     * @return 各维度态势数据
     */
    @GetMapping("/dimensions")
    public ApiResponse<Map<String, Double>> getDimensionAnalysis() {
        // 生成模拟维度分析数据
        Map<String, Double> dimensions = new HashMap<>();
        dimensions.put("threatDetection", 65.0 + (Math.random() * 30.0));
        dimensions.put("vulnerability", 70.0 + (Math.random() * 25.0));
        dimensions.put("assetSecurity", 60.0 + (Math.random() * 35.0));
        dimensions.put("networkTraffic", 75.0 + (Math.random() * 20.0));
        dimensions.put("securityConfig", 80.0 + (Math.random() * 15.0));
        
        return ApiResponse.success(dimensions);
    }
    
    /**
     * 获取安全事件统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getSecurityStatistics(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        // 解析ISO日期字符串
        Date startDateTime;
        Date endDateTime;
        try {
            startDateTime = Date.from(Instant.parse(startTime));
        } catch (Exception e) {
            return ApiResponse.error(400, "无效的开始时间格式: " + startTime);
        }
        
        try {
            endDateTime = Date.from(Instant.parse(endTime));
        } catch (Exception e) {
            return ApiResponse.error(400, "无效的结束时间格式: " + endTime);
        }
        
        // 由于SituationEvaluator接口没有getSecurityStatistics方法，我们返回空映射
        return ApiResponse.success(new HashMap<>());
    }
    
    /**
     * 获取资产详情
     * @param assetId 资产ID
     * @return 资产详情数据
     */
    @GetMapping("/asset/{assetId}")
    public ApiResponse<Map<String, Object>> getAssetDetail(@PathVariable String assetId) {
        Map<String, Object> assetDetail = visualizationService.getAssetDetail(assetId);
        return ApiResponse.success(assetDetail);
    }
    
    /**
     * 获取资产漏洞列表
     * @param assetId 资产ID
     * @return 漏洞列表
     */
    @GetMapping("/asset/{assetId}/vulnerabilities")
    public ApiResponse<List<Map<String, Object>>> getAssetVulnerabilities(@PathVariable String assetId) {
        List<Map<String, Object>> vulnerabilities = visualizationService.getAssetVulnerabilities(assetId);
        return ApiResponse.success(vulnerabilities);
    }
    
    /**
     * 获取资产威胁列表
     * @param assetId 资产ID
     * @return 威胁列表
     */
    @GetMapping("/asset/{assetId}/threats")
    public ApiResponse<List<Map<String, Object>>> getAssetThreats(@PathVariable String assetId) {
        List<Map<String, Object>> threats = visualizationService.getAssetThreats(assetId);
        return ApiResponse.success(threats);
    }
    
    /**
     * 获取资产风险趋势
     * @param assetId 资产ID
     * @param range 时间范围
     * @return 风险趋势数据
     */
    @GetMapping("/asset/{assetId}/trend")
    public ApiResponse<List<Map<String, Object>>> getAssetRiskTrend(@PathVariable String assetId, @RequestParam String range) {
        List<Map<String, Object>> trendData = visualizationService.getAssetRiskTrend(assetId, range);
        return ApiResponse.success(trendData);
    }
    
    /**
     * 获取威胁情报详情
     * @param threatId 威胁ID
     * @return 威胁情报详情
     */
    @GetMapping("/threat/{threatId}")
    public ApiResponse<Map<String, Object>> getThreatDetail(@PathVariable String threatId) {
        Map<String, Object> threatDetail = visualizationService.getThreatDetail(threatId);

        // 如果数据库中没有找到，生成模拟数据作为备选
        if (threatDetail == null || threatDetail.isEmpty()) {
            threatDetail = generateMockThreatDetail(threatId);
        }

        return ApiResponse.success(threatDetail);
    }

    /**
     * 生成模拟威胁情报详情（当数据库中没有找到时使用）
     * @param threatId 威胁ID
     * @return 模拟威胁情报详情
     */
    private Map<String, Object> generateMockThreatDetail(String threatId) {
        Map<String, Object> detail = new HashMap<>();

        // 根据threatId生成确定性的数据
        int id = extractIdFromThreatId(threatId);

        String[] threatTypes = {"恶意软件", "钓鱼网站", "DDoS攻击", "SQL注入", "XSS攻击", "暴力破解"};
        String[] iocTypes = {"IP", "DOMAIN", "URL", "FILE_HASH", "EMAIL"};
        String[] severities = {"CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"};
        String[] sources = {"内部IDS", "外部威胁情报", "沙箱分析", "用户举报", "自动化扫描"};

        String threatType = threatTypes[id % threatTypes.length];
        String iocType = iocTypes[id % iocTypes.length];
        String severity = severities[id % severities.length];
        String source = sources[id % sources.length];

        // 基本信息
        detail.put("id", (long) id);
        detail.put("threatId", "THREAT-" + String.format("%04d", id));
        detail.put("iocValue", generateMockIOCValue(iocType, id));
        detail.put("iocType", iocType);
        detail.put("threatType", threatType);
        detail.put("severity", severity);
        detail.put("confidenceScore", 70 + (id % 30));
        detail.put("source", source);
        detail.put("description", String.format("检测到%s类型的威胁活动，该威胁已被%s标记为%s。",
            threatType, source, severity));
        detail.put("provider", "安全运营中心");

        // 时间信息
        long daysAgo = 1 + (id % 30);
        java.util.Date firstSeen = new java.util.Date(System.currentTimeMillis() - daysAgo * 24L * 60 * 60 * 1000);
        detail.put("firstSeen", firstSeen.toString());
        detail.put("lastUpdated", new java.util.Date().toString());
        detail.put("discoveredTime", firstSeen.toString());

        // 标签和状态
        detail.put("status", "ACTIVE");
        List<String> tags = new ArrayList<>();
        tags.add(threatType);
        tags.add(iocType);
        detail.put("tags", tags);

        // 相关CVE和影响系统
        detail.put("relatedCves", new ArrayList<String>());
        List<String> affectedSystems = new ArrayList<>();
        affectedSystems.add("服务器集群");
        affectedSystems.add("数据库系统");
        detail.put("affectedSystems", affectedSystems);

        // 附加信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("country", "China");
        additionalInfo.put("asn", "AS" + (10000 + id % 50000));
        additionalInfo.put("attackPattern", "T" + (1000 + id % 2000));
        additionalInfo.put("toolsUsed", threatType);
        additionalInfo.put("historicalActivities", 10 + id % 100);
        detail.put("additionalInfo", additionalInfo);

        // 生成处置建议
        List<Map<String, String>> recommendations = new ArrayList<>();
        Map<String, String> rec1 = new HashMap<>();
        rec1.put("title", "立即阻断威胁");
        rec1.put("detail", "根据威胁特征，立即在防火墙和IPS设备上配置阻断规则");
        recommendations.add(rec1);
        Map<String, String> rec2 = new HashMap<>();
        rec2.put("title", "加强监控");
        rec2.put("detail", "针对该威胁类型增强安全监控和日志分析");
        recommendations.add(rec2);
        Map<String, String> rec3 = new HashMap<>();
        rec3.put("title", "漏洞修复");
        rec3.put("detail", "检查系统是否存在相关漏洞，并及时修复");
        recommendations.add(rec3);
        Map<String, String> rec4 = new HashMap<>();
        rec4.put("title", "通知安全团队");
        rec4.put("detail", "立即通知安全运营团队进行深度调查和响应");
        recommendations.add(rec4);
        detail.put("recommendations", recommendations);

        // 受影响资产（模拟数据）
        List<Map<String, Object>> affectedAssets = new ArrayList<>();
        for (int i = 0; i < (1 + id % 3); i++) {
            Map<String, Object> asset = new HashMap<>();
            asset.put("assetId", "ASSET-" + String.format("%03d", id * 10 + i + 1));
            asset.put("assetName", new String[]{"Web服务器", "数据库服务器", "应用服务器"}[i % 3] + "-" + String.format("%02d", id * 10 + i + 1));
            asset.put("assetType", new String[]{"SERVER", "DATABASE", "APPLICATION"}[i % 3]);
            asset.put("riskScore", 60 + (id * 10 + i) % 40);
            asset.put("riskLevel", (id + i) % 3 == 0 ? "HIGH" : "MEDIUM");
            asset.put("vulnerabilityCount", 1 + (id + i) % 10);
            asset.put("threatCount", 1 + (id + i) % 5);
            asset.put("assetValueScore", 70 + (id + i) % 30);
            asset.put("vulnerabilityScore", 60 + (id + i) % 40);
            asset.put("threatScore", 50 + (id + i) % 50);
            asset.put("status", "ACTIVE");
            affectedAssets.add(asset);
        }
        detail.put("affectedAssets", affectedAssets);

        // 影响事件数（模拟数据）
        List<Map<String, Object>> affectedEvents = new ArrayList<>();
        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", "EVENT-" + String.format("%06d", id * 100 + 1));
        event1.put("type", "检测告警");
        event1.put("count", 50 + id % 200);
        event1.put("description", "系统检测到与该IOC相关的可疑活动");
        affectedEvents.add(event1);
        Map<String, Object> event2 = new HashMap<>();
        event2.put("id", "EVENT-" + String.format("%06d", id * 100 + 2));
        event2.put("type", "访问尝试");
        event2.put("count", 20 + id % 100);
        event2.put("description", "检测到来自该源的访问尝试");
        affectedEvents.add(event2);
        detail.put("affectedEvents", affectedEvents);

        // 活动时间线（模拟数据）
        List<Map<String, Object>> activities = new ArrayList<>();
        java.util.Date now = new java.util.Date();
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("timestamp", now.toString());
        activity1.put("type", "update");
        activity1.put("title", "情报更新");
        activity1.put("description", "威胁情报置信度更新为" + (70 + id % 30));
        activities.add(activity1);
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("timestamp", new java.util.Date(now.getTime() - 2 * 24 * 60 * 60 * 1000L).toString());
        activity2.put("type", "detection");
        activity2.put("title", "新检测");
        activity2.put("description", "在系统日志中发现新的相关活动");
        activities.add(activity2);
        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("timestamp", firstSeen.toString());
        activity3.put("type", "creation");
        activity3.put("title", "情报创建");
        activity3.put("description", "首次发现该威胁指标");
        activities.add(activity3);
        detail.put("activities", activities);

        // 相关威胁（模拟数据）
        List<Map<String, Object>> relatedThreats = new ArrayList<>();
        for (int i = 1; i <= (1 + id % 3); i++) {
            int relatedId = (id + i) % 50 + 1;
            Map<String, Object> threat = new HashMap<>();
            threat.put("id", (long) relatedId);
            threat.put("threatId", "THREAT-" + String.format("%04d", relatedId));
            threat.put("iocValue", generateMockIOCValue(iocTypes[relatedId % iocTypes.length], relatedId));
            threat.put("iocType", iocTypes[relatedId % iocTypes.length]);
            threat.put("threatType", threatTypes[relatedId % threatTypes.length]);
            threat.put("severity", severities[relatedId % severities.length]);
            threat.put("confidenceScore", 70 + relatedId % 30);
            threat.put("lastUpdated", new java.util.Date().toString());
            threat.put("description", "相关威胁情报");
            relatedThreats.add(threat);
        }
        detail.put("relatedThreats", relatedThreats);

        return detail;
    }

    /**
     * 从威胁ID中提取数字ID
     * @param threatId 威胁ID字符串（如"THREAT-0001"）
     * @return 数字ID
     */
    private int extractIdFromThreatId(String threatId) {
        try {
            if (threatId != null && threatId.startsWith("THREAT-")) {
                String numericPart = threatId.substring("THREAT-".length());
                return Integer.parseInt(numericPart);
            }
            return Integer.parseInt(threatId);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * 生成模拟IOC值
     */
    private String generateMockIOCValue(String iocType, int index) {
        switch (iocType) {
            case "IP":
                return (192 + index % 64) + "." + ((index * 7) % 256) + "." + ((index * 13) % 256) + "." + ((index * 17) % 256);
            case "DOMAIN":
                return "malicious-domain-" + index + ".com";
            case "URL":
                return "http://malicious-site-" + index + ".com/payload";
            case "FILE_HASH":
                return "a1b2c3d4e5f6" + String.format("%04d", index);
            case "EMAIL":
                return "threat" + index + "@malicious.com";
            default:
                return "unknown-ioc-" + index;
        }
    }
    
    /**
     * 获取威胁影响资产
     * @param threatId 威胁ID
     * @return 受影响资产列表
     */
    @GetMapping("/threat/{threatId}/affected-assets")
    public ApiResponse<List<Map<String, Object>>> getThreatAffectedAssets(@PathVariable String threatId) {
        List<Map<String, Object>> affectedAssets = visualizationService.getThreatAffectedAssets(threatId);
        return ApiResponse.success(affectedAssets);
    }
    
    /**
     * 获取威胁相关事件
     * @param threatId 威胁ID
     * @return 相关事件列表
     */
    @GetMapping("/threat/{threatId}/related-events")
    public ApiResponse<List<Map<String, Object>>> getThreatRelatedEvents(@PathVariable String threatId) {
        List<Map<String, Object>> relatedEvents = visualizationService.getThreatRelatedEvents(threatId);
        return ApiResponse.success(relatedEvents);
    }
    
    /**
     * 获取威胁相关情报
     * @param threatId 威胁ID
     * @return 相关情报列表
     */
    @GetMapping("/threat/{threatId}/related-threats")
    public ApiResponse<List<Map<String, Object>>> getRelatedThreats(@PathVariable String threatId) {
        List<Map<String, Object>> relatedThreats = visualizationService.getRelatedThreats(threatId);
        return ApiResponse.success(relatedThreats);
    }
    
    /**
     * 获取资产风险热力图数据
     * @return 热力图数据
     */
    @GetMapping("/heatmap")
    public ApiResponse<List<Map<String, Object>>> getAssetRiskHeatmap() {
        // 生成模拟热力图数据
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        String[] locations = {"数据中心A区", "数据中心B区", "办公区", "生产区", "测试区"};
        
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("assetId", "ASSET-" + String.format("%03d", i));
            point.put("assetName", "资产-" + String.format("%03d", i));
            point.put("riskScore", 10 + (int)(Math.random() * 90));
            point.put("location", locations[i % locations.length]);
            heatmapData.add(point);
        }
        
        return ApiResponse.success(heatmapData);
    }
    
    /**
     * 获取高风险资产列表
     * @param page 页码
     * @param size 每页数量
     * @return 高风险资产数据
     */
    @GetMapping("/high-risk-assets")
    public ApiResponse<Map<String, Object>> getHighRiskAssets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // 生成模拟高风险资产数据
        List<Map<String, Object>> highRiskAssets = new ArrayList<>();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, 50); // 假设总共50个高风险资产
        
        for (int i = startIndex; i < endIndex; i++) {
            Map<String, Object> asset = new HashMap<>();
            asset.put("assetId", "HIGH-RISK-" + String.format("%03d", i + 1));
            asset.put("assetName", "高危资产-" + String.format("%03d", i + 1));
            asset.put("assetType", "服务器");
            asset.put("riskScore", 70 + (int)(Math.random() * 30));
            asset.put("riskLevel", "HIGH");
            asset.put("location", "数据中心A区");
            highRiskAssets.add(asset);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", highRiskAssets);
        response.put("total", 50);
        response.put("page", page);
        response.put("size", size);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 获取告警列表
     * @return 告警数据
     */
    @GetMapping("/alerts")
    public ApiResponse<List<Map<String, Object>>> getSituationAlerts() {
        // 生成模拟告警数据
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        String[] alertTypes = {"安全事件", "威胁检测", "异常行为", "系统告警"};
        String[] alertLevels = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
        String[] statuses = {"ACTIVE", "RESOLVED", "INVESTIGATING"};
        
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("alertId", "ALERT-" + String.format("%04d", i));
            alert.put("alertType", alertTypes[i % alertTypes.length]);
            alert.put("alertLevel", alertLevels[(int)(Math.random() * alertLevels.length)]);
            alert.put("description", "系统检测到异常活动，需要立即处理");
            alert.put("createTime", new Date(System.currentTimeMillis() - (long)(Math.random() * 24 * 60 * 60 * 1000)));
            alert.put("status", statuses[i % statuses.length]);
            alerts.add(alert);
        }
        
        return ApiResponse.success(alerts);
    }
    
    /**
     * 获取资产地理分布数据（含经纬度，用于地图显示）
     * @return 资产地理分布数据
     */
    @GetMapping("/asset-geographic")
    public ApiResponse<Map<String, Object>> getAssetGeographicDistribution() {
        // 生成模拟资产地理分布数据
        Map<String, Object> geographicData = new HashMap<>();
        
        // 城市坐标和名称映射
        Map<String, double[]> cityCoordinates = new HashMap<>();
        cityCoordinates.put("北京数据中心", new double[]{116.4074, 39.9042});
        cityCoordinates.put("上海机房", new double[]{121.4737, 31.2304});
        cityCoordinates.put("广州办公室", new double[]{113.2644, 23.1291});
        cityCoordinates.put("深圳研发中心", new double[]{114.0579, 22.5431});
        cityCoordinates.put("成都分部", new double[]{104.0668, 30.5728});
        cityCoordinates.put("武汉分公司", new double[]{114.3055, 30.5928});
        cityCoordinates.put("西安数据中心", new double[]{108.9398, 34.3416});
        cityCoordinates.put("杭州研发中心", new double[]{120.1551, 30.2741});
        cityCoordinates.put("南京分公司", new double[]{118.7969, 32.0603});
        cityCoordinates.put("天津数据中心", new double[]{117.2009, 39.0842});
        
        List<Map<String, Object>> assets = new ArrayList<>();
        String[] assetTypes = {"服务器", "网络设备", "工作站", "数据库", "应用系统"};
        String[] locations = cityCoordinates.keySet().toArray(new String[0]);
        
        for (int i = 1; i <= 50; i++) {
            String location = locations[i % locations.length];
            double[] coords = cityCoordinates.get(location);
            
            Map<String, Object> asset = new HashMap<>();
            asset.put("assetId", "ASSET-" + String.format("%03d", i));
            asset.put("assetName", assetTypes[i % assetTypes.length] + "-" + String.format("%02d", i));
            asset.put("assetType", assetTypes[i % assetTypes.length]);
            asset.put("riskScore", 10 + (int)(Math.random() * 90));
            asset.put("riskLevel", getRiskLevelFromScore(10 + (int)(Math.random() * 90)));
            asset.put("location", location);
            asset.put("longitude", coords[0] + (Math.random() - 0.5) * 0.1); // 添加一点随机偏移
            asset.put("latitude", coords[1] + (Math.random() - 0.5) * 0.1);
            asset.put("ipAddress", "192.168." + (i / 10 + 1) + "." + (i % 10 + 1));
            asset.put("status", "ACTIVE");
            
            assets.add(asset);
        }
        
        // 按地理位置统计风险数据
        Map<String, Object> locationStats = new HashMap<>();
        for (Map.Entry<String, double[]> entry : cityCoordinates.entrySet()) {
            String location = entry.getKey();
            double[] coords = entry.getValue();
            
            // 统计该位置的所有资产
            List<Map<String, Object>> locationAssets = new ArrayList<>();
            int totalRiskScore = 0;
            int highRiskCount = 0;
            
            for (int i = 0; i < 5; i++) {
                int riskScore = 10 + (int)(Math.random() * 90);
                totalRiskScore += riskScore;
                if (riskScore >= 60) highRiskCount++;
                
                Map<String, Object> locAsset = new HashMap<>();
                locAsset.put("name", assetTypes[i % assetTypes.length] + (i + 1));
                locAsset.put("riskScore", riskScore);
                locAsset.put("value", riskScore);
                locationAssets.add(locAsset);
            }
            
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("name", location);
            locationData.put("value", totalRiskScore / 5.0); // 平均风险分数
            locationData.put("longitude", coords[0]);
            locationData.put("latitude", coords[1]);
            locationData.put("assetCount", 5 + (int)(Math.random() * 10));
            locationData.put("highRiskCount", highRiskCount);
            locationData.put("assets", locationAssets);
            
            locationStats.put(location, locationData);
        }
        
        geographicData.put("assets", assets);
        geographicData.put("locationStats", locationStats);
        
        return ApiResponse.success(geographicData);
    }
    
    /**
     * 根据风险分数返回风险等级
     */
    private String getRiskLevelFromScore(int score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "MEDIUM";
        if (score >= 20) return "LOW";
        return "SAFE";
    }
}