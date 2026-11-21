package com.example.securitydefense.situation.service;

import com.example.securitydefense.situation.model.entity.SituationDataEntity;
import com.example.securitydefense.situation.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可视化服务类
 * 提供态势可视化所需的数据处理和转换功能
 */
@Service
public class VisualizationService {
    
    @Autowired
    private ThreatIntelligenceService threatIntelligenceService;
    
    /**
     * 生成态势仪表盘数据
     * @param situationData 当前态势数据
     * @param assetRisks 资产风险数据
     * @param threats 威胁数据
     * @return 仪表盘数据
     */
    public Map<String, Object> generateDashboardData(
            SituationDataEntity situationData,
            List<AssetRiskEntity> assetRisks,
            List<ThreatIntelligenceEntity> threats) {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // 基本态势信息
        dashboardData.put("situationIndex", situationData.getSituationIndex());
        dashboardData.put("situationLevel", situationData.getSituationLevel());
        dashboardData.put("timestamp", situationData.getTimestamp());
        
        // 资产风险统计
        dashboardData.put("assetRiskStats", generateAssetRiskStats(assetRisks));
        
        // 威胁统计
        dashboardData.put("threatStats", generateThreatStats(threats));
        
        // 维度分数
        Map<String, Object> dimensionScores = new HashMap<>();
        dimensionScores.put("threatScore", situationData.getThreatScore());
        dimensionScores.put("vulnerabilityScore", situationData.getVulnerabilityScore());
        dimensionScores.put("assetValueScore", situationData.getAssetValueScore());
        dashboardData.put("dimensionScores", dimensionScores);
        
        // 关键指标
        dashboardData.put("keyMetrics", generateKeyMetrics(situationData));
        
        return dashboardData;
    }
    
    /**
     * 生成资产风险统计数据
     * @param assetRisks 资产风险列表
     * @return 资产风险统计
     */
    private Map<String, Object> generateAssetRiskStats(List<AssetRiskEntity> assetRisks) {
        Map<String, Object> stats = new HashMap<>();
        
        // 按风险等级分组
        Map<String, Long> riskLevelCount = assetRisks.stream()
                .collect(Collectors.groupingBy(AssetRiskEntity::getRiskLevel, Collectors.counting()));
        
        // 按资产类型分组
        Map<String, Double> avgRiskByType = assetRisks.stream()
                .collect(Collectors.groupingBy(
                        AssetRiskEntity::getAssetType,
                        Collectors.averagingDouble(AssetRiskEntity::getRiskScore)
                ));
        
        // 高风险资产
        List<AssetRiskEntity> highRiskAssets = assetRisks.stream()
                .filter(asset -> "HIGH".equals(asset.getRiskLevel()) || "CRITICAL".equals(asset.getRiskLevel()))
                .sorted(Comparator.comparing(AssetRiskEntity::getRiskScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        stats.put("riskLevelCount", riskLevelCount);
        stats.put("avgRiskByType", avgRiskByType);
        stats.put("highRiskAssets", highRiskAssets);
        stats.put("totalAssets", assetRisks.size());
        
        return stats;
    }
    
    /**
     * 生成威胁统计数据
     * @param threats 威胁列表
     * @return 威胁统计
     */
    private Map<String, Object> generateThreatStats(List<ThreatIntelligenceEntity> threats) {
        Map<String, Object> stats = new HashMap<>();
        
        // 按威胁类型分组
        Map<String, Long> threatTypeCount = threats.stream()
                .collect(Collectors.groupingBy(ThreatIntelligenceEntity::getThreatType, Collectors.counting()));
        
        // 按严重程度分组
        Map<String, Long> severityCount = threats.stream()
                .collect(Collectors.groupingBy(ThreatIntelligenceEntity::getThreatLevel, Collectors.counting()));
        
        // 最新威胁
        List<ThreatIntelligenceEntity> recentThreats = threats.stream()
                .sorted(Comparator.comparing(ThreatIntelligenceEntity::getFirstSeen).reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        stats.put("threatTypeCount", threatTypeCount);
        stats.put("severityCount", severityCount);
        stats.put("recentThreats", recentThreats);
        stats.put("totalThreats", threats.size());
        
        return stats;
    }
    
    /**
     * 生成关键指标
     * @param situationData 态势数据
     * @return 关键指标
     */
    private Map<String, Object> generateKeyMetrics(SituationDataEntity situationData) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalAssets", situationData.getTotalAssets());
        metrics.put("criticalAssets", situationData.getCriticalAssets());
        metrics.put("activeThreats", situationData.getHighThreats());
        metrics.put("openVulnerabilities", situationData.getCriticalVulnerabilities());
        metrics.put("threatScore", situationData.getThreatScore());
        metrics.put("vulnerabilityScore", situationData.getVulnerabilityScore());
        
        return metrics;
    }
    
    /**
     * 处理趋势数据，准备图表展示
     * @param trendData 历史态势数据
     * @return 处理后的趋势数据
     */
    public List<Map<String, Object>> processTrendData(List<SituationDataEntity> trendData) {
        return trendData.stream().map(entity -> {
            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", entity.getTimestamp());
            point.put("situationIndex", entity.getSituationIndex());
            point.put("situationLevel", entity.getSituationLevel());
            point.put("activeThreats", entity.getHighThreats());
            // point.put("securityEvents", entity.getSecurityEvents()); // 方法不存在
            return point;
        }).collect(Collectors.toList());
    }
    
    /**
     * 生成热力图数据
     * @param assetRisks 资产风险数据
     * @return 热力图数据
     */
    public List<Map<String, Object>> generateHeatmapData(List<AssetRiskEntity> assetRisks) {
        return assetRisks.stream()
                .filter(asset -> asset.getLocation() != null && !asset.getLocation().isEmpty())
                .map(asset -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("assetId", asset.getAssetId());
                    point.put("assetName", asset.getAssetName());
                    point.put("location", asset.getLocation());
                    point.put("riskScore", asset.getRiskScore());
                    point.put("riskLevel", asset.getRiskLevel());
                    return point;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取资产详情
     * @param assetId 资产ID
     * @return 资产详情
     */
    public Map<String, Object> getAssetDetail(String assetId) {
        Map<String, Object> assetDetail = new HashMap<>();
        assetDetail.put("assetId", assetId);
        assetDetail.put("assetName", "Web服务器-01");
        assetDetail.put("assetType", "SERVER");
        assetDetail.put("riskScore", 78.5);
        assetDetail.put("riskLevel", "HIGH");
        assetDetail.put("ipAddress", "192.168.1.10");
        assetDetail.put("location", "数据中心A区");
        assetDetail.put("operatingSystem", "Linux CentOS 7");
        assetDetail.put("businessSystem", "核心业务系统");
        assetDetail.put("assetValueScore", 90);
        assetDetail.put("vulnerabilityScore", 82);
        assetDetail.put("threatScore", 65);
        assetDetail.put("vulnerabilityCount", 12);
        assetDetail.put("threatCount", 8);
        assetDetail.put("lastScanTime", new Date().toString());
        assetDetail.put("status", "ACTIVE");
        
        // 风险组成
        Map<String, Integer> riskComposition = new HashMap<>();
        riskComposition.put("threat", 30);
        riskComposition.put("vulnerability", 50);
        riskComposition.put("misconfiguration", 15);
        riskComposition.put("exposure", 5);
        assetDetail.put("riskComposition", riskComposition);
        
        // 添加漏洞数据
        List<Map<String, Object>> vulnerabilities = getAssetVulnerabilities(assetId);
        assetDetail.put("vulnerabilities", vulnerabilities);
        
        // 添加威胁数据
        List<Map<String, Object>> threats = getAssetThreats(assetId);
        assetDetail.put("threats", threats);
        
        return assetDetail;
    }
    
    /**
     * 获取资产漏洞列表
     * @param assetId 资产ID
     * @return 漏洞列表
     */
    public List<Map<String, Object>> getAssetVulnerabilities(String assetId) {
        List<Map<String, Object>> vulnerabilities = new ArrayList<>();
        
        Map<String, Object> vuln1 = new HashMap<>();
        vuln1.put("vulnId", "VULN-2024-001");
        vuln1.put("cveId", "CVE-2023-46747");
        vuln1.put("vulnerabilityType", "远程代码执行");
        vuln1.put("severity", "CRITICAL");
        vuln1.put("cvssScore", 9.8);
        vuln1.put("description", "严重的远程代码执行漏洞，攻击者可以在未授权的情况下执行任意代码");
        vuln1.put("discoveredTime", new Date().toString());
        vuln1.put("status", "OPEN");
        vuln1.put("remediation", "升级到最新版本");
        vulnerabilities.add(vuln1);
        
        Map<String, Object> vuln2 = new HashMap<>();
        vuln2.put("vulnId", "VULN-2024-002");
        vuln2.put("cveId", "CVE-2023-36760");
        vuln2.put("vulnerabilityType", "SQL注入");
        vuln2.put("severity", "HIGH");
        vuln2.put("cvssScore", 8.6);
        vuln2.put("description", "SQL注入漏洞，可能导致数据库信息泄露");
        vuln2.put("discoveredTime", new Date().toString());
        vuln2.put("status", "OPEN");
        vuln2.put("remediation", "使用参数化查询");
        vulnerabilities.add(vuln2);
        
        Map<String, Object> vuln3 = new HashMap<>();
        vuln3.put("vulnId", "VULN-2024-003");
        vuln3.put("vulnerabilityType", "弱密码");
        vuln3.put("severity", "MEDIUM");
        vuln3.put("cvssScore", 6.5);
        vuln3.put("description", "系统存在弱密码配置");
        vuln3.put("discoveredTime", new Date().toString());
        vuln3.put("patchedTime", new Date().toString());
        vuln3.put("status", "PATCHED");
        vulnerabilities.add(vuln3);
        
        return vulnerabilities;
    }
    
    /**
     * 获取资产威胁列表
     * @param assetId 资产ID
     * @return 威胁列表
     */
    public List<Map<String, Object>> getAssetThreats(String assetId) {
        List<Map<String, Object>> threats = new ArrayList<>();
        
        Map<String, Object> threat1 = new HashMap<>();
        threat1.put("threatId", "THREAT-2024-001");
        threat1.put("threatType", "Ransomware");
        threat1.put("iocValue", "malware.exe");
        threat1.put("iocType", "FILE_HASH");
        threat1.put("severity", "CRITICAL");
        threat1.put("confidenceScore", 0.95);
        threat1.put("discoveredTime", new Date().toString());
        threat1.put("description", "检测到勒索软件活动");
        threats.add(threat1);
        
        Map<String, Object> threat2 = new HashMap<>();
        threat2.put("threatId", "THREAT-2024-002");
        threat2.put("threatType", "BruteForce");
        threat2.put("iocValue", "10.0.0.5");
        threat2.put("iocType", "IP");
        threat2.put("severity", "HIGH");
        threat2.put("confidenceScore", 0.88);
        threat2.put("discoveredTime", new Date().toString());
        threat2.put("description", "检测到暴力破解攻击");
        threats.add(threat2);
        
        return threats;
    }
    
    /**
     * 获取威胁情报详情
     * @param threatId 威胁ID
     * @return 威胁情报详情
     */
    public Map<String, Object> getThreatDetail(String threatId) {
        // 调用威胁情报服务从数据库查询真实数据
        Map<String, Object> threatDetail = threatIntelligenceService.getThreatDetailByThreatId(threatId);
        
        // 如果数据库中没有找到，返回null而不是空对象
        return threatDetail;
    }
    
    /**
     * 获取威胁影响资产
     * @param threatId 威胁ID
     * @return 受影响资产列表
     */
    public List<Map<String, Object>> getThreatAffectedAssets(String threatId) {
        // 调用威胁情报服务从数据库查询真实数据
        return threatIntelligenceService.getThreatAffectedAssets(threatId);
    }
    
    /**
     * 获取资产风险趋势
     * @param assetId 资产ID
     * @param range 时间范围
     * @return 风险趋势数据
     */
    public List<Map<String, Object>> getAssetRiskTrend(String assetId, String range) {
        // 根据range参数确定天数
        int days = 7;
        if ("30d".equals(range)) {
            days = 30;
        } else if ("90d".equals(range)) {
            days = 90;
        }
        
        // 生成模拟风险趋势数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        double baseScore = 78.5;
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> data = new HashMap<>();
            LocalDate date = today.minusDays(i);
            data.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            // 生成围绕基准分的随机波动
            double score = Math.max(0, Math.min(100, baseScore + (Math.random() - 0.5) * 20));
            data.put("score", Math.round(score * 10) / 10.0);
            trendData.add(data);
        }
        
        return trendData;
    }
    
    /**
     * 获取威胁相关事件
     * @param threatId 威胁ID
     * @return 相关事件列表
     */
    public List<Map<String, Object>> getThreatRelatedEvents(String threatId) {
        // 调用威胁情报服务从数据库查询真实数据
        return threatIntelligenceService.getThreatRelatedEvents(threatId);
    }
    
    /**
     * 获取威胁相关情报
     * @param threatId 威胁ID
     * @return 相关情报列表
     */
    public List<Map<String, Object>> getRelatedThreats(String threatId) {
        // 调用威胁情报服务从数据库查询真实数据
        return threatIntelligenceService.getRelatedThreats(threatId);
    }
}