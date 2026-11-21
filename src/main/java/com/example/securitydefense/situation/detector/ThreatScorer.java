package com.example.securitydefense.situation.detector;

import com.example.securitydefense.situation.collector.*;
import com.example.securitydefense.situation.config.DetectionProperties;
import com.example.securitydefense.situation.model.ThreatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 威胁评分检测器
 * 动态计算资产威胁等级
 */
@Slf4j
@Component
public class ThreatScorer implements ThreatDetector {
    
    @Autowired
    private DetectionProperties detectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 资产威胁评分存储
    private final Map<String, AssetThreatScore> assetThreatScores = new ConcurrentHashMap<>();
    
    // 威胁类型权重映射
    private final Map<String, Double> threatTypeWeights = new ConcurrentHashMap<>();
    
    // 威胁等级权重映射
    private final Map<String, Double> threatLevelWeights = new ConcurrentHashMap<>();
    
    // 时间衰减因子（用于旧威胁加权）
    private final double timeDecayFactor;
    
    public ThreatScorer() {
        this.timeDecayFactor = 0.95; // 默认时间衰减因子
    }
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                log.info("Initializing Threat Scorer");
                
                // 初始化威胁类型权重
                initThreatTypeWeights();
                
                // 初始化威胁等级权重
                initThreatLevelWeights();
                
                running.set(true);
                log.info("Threat Scorer initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Threat Scorer: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    private void initThreatTypeWeights() {
        // 初始化威胁类型权重
        threatTypeWeights.put("MULTI_STAGE_ATTACK", 1.0);
        threatTypeWeights.put("IOC_MATCH", 0.8);
        threatTypeWeights.put("ANOMALY_DETECTION", 0.7);
        threatTypeWeights.put("VULNERABILITY", 0.9);
        threatTypeWeights.put("BRUTE_FORCE", 0.85);
        threatTypeWeights.put("MALWARE", 0.95);
        threatTypeWeights.put("DATA_EXFILTRATION", 1.0);
        threatTypeWeights.put("SUSPICIOUS_BEHAVIOR", 0.6);
    }
    
    private void initThreatLevelWeights() {
        // 初始化威胁等级权重
        threatLevelWeights.put("CRITICAL", 1.0);
        threatLevelWeights.put("HIGH", 0.8);
        threatLevelWeights.put("MEDIUM", 0.5);
        threatLevelWeights.put("LOW", 0.2);
    }
    
    @Override
    public List<ThreatResult> detect(Object data) {
        List<Object> dataList = Collections.singletonList(data);
        return detectBatch(dataList);
    }
    
    @Override
    public List<ThreatResult> detectBatch(List<Object> dataList) {
        List<ThreatResult> threatResults = new ArrayList<>();
        
        if (!isAvailable()) {
            log.warn("Threat Scorer is not available, skipping detection");
            return threatResults;
        }
        
        try {
            // 处理所有输入数据
            for (Object data : dataList) {
                if (data instanceof ThreatResult) {
                    // 处理威胁检测结果
                    processThreatResult((ThreatResult) data, threatResults);
                } else if (data instanceof VulnerabilityData) {
                    // 处理漏洞数据
                    processVulnerabilityData((VulnerabilityData) data, threatResults);
                } else if (data instanceof NetFlowData || 
                           data instanceof SecurityLogData || 
                           data instanceof SystemLogData) {
                    // 处理基础数据，更新资产威胁分数
                    updateAssetScoresFromData(data);
                }
            }
            
            // 生成资产威胁等级结果
            generateAssetThreatResults(threatResults);
            
            // 清理过期的资产威胁数据
            cleanupExpiredAssetScores();
            
        } catch (Exception e) {
            log.error("Error during threat scoring: {}", e.getMessage());
        }
        
        return threatResults;
    }
    
    private void processThreatResult(ThreatResult threatResult, List<ThreatResult> results) {
        String targetAsset = identifyTargetAsset(threatResult);
        if (targetAsset != null) {
            // 更新资产威胁分数
            updateAssetThreatScore(targetAsset, threatResult);
            
            // 如果威胁等级发生变化，生成新的威胁结果
            AssetThreatScore assetScore = assetThreatScores.get(targetAsset);
            if (assetScore != null && assetScore.getThreatLevelChanged()) {
                ThreatResult assetThreatResult = createAssetThreatLevelResult(assetScore);
                results.add(assetThreatResult);
                log.info("Asset threat level changed: {} - {} (previous: {})", 
                         targetAsset, assetScore.getCurrentThreatLevel(), 
                         assetScore.getPreviousThreatLevel());
            }
        }
    }
    
    private void processVulnerabilityData(VulnerabilityData vulnerabilityData, List<ThreatResult> results) {
        String assetId = vulnerabilityData.getAssetId();
        if (assetId != null) {
            // 创建漏洞相关的威胁结果
            ThreatResult vulnThreatResult = createVulnerabilityThreatResult(vulnerabilityData);
            
            // 更新资产威胁分数
            updateAssetThreatScore(assetId, vulnThreatResult);
            
            // 检查威胁等级变化
            AssetThreatScore assetScore = assetThreatScores.get(assetId);
            if (assetScore != null && assetScore.getThreatLevelChanged()) {
                ThreatResult assetThreatResult = createAssetThreatLevelResult(assetScore);
                results.add(assetThreatResult);
            }
            
            // 添加漏洞威胁结果
            results.add(vulnThreatResult);
        }
    }
    
    private void updateAssetScoresFromData(Object data) {
        String assetId = identifyAssetFromData(data);
        if (assetId != null) {
            // 获取或创建资产威胁分数对象
            AssetThreatScore assetScore = assetThreatScores.computeIfAbsent(assetId, 
                    k -> new AssetThreatScore(assetId));
            
            // 更新最近活动时间
            assetScore.updateLastActivityTime();
        }
    }
    
    private String identifyTargetAsset(ThreatResult threatResult) {
        // 优先使用目标IP作为资产标识
        if (threatResult.getDestIp() != null && !threatResult.getDestIp().isEmpty()) {
            return threatResult.getDestIp();
        }
        
        // 如果有额外信息中的资产ID，使用它
        Map<String, String> additionalInfo = threatResult.getAdditionalInfo();
        if (additionalInfo != null && additionalInfo.containsKey("asset_id")) {
            return additionalInfo.get("asset_id");
        }
        
        return null;
    }
    
    private String identifyAssetFromData(Object data) {
        if (data instanceof NetFlowData) {
            return ((NetFlowData) data).getDestIp();
        } else if (data instanceof SecurityLogData) {
            return ((SecurityLogData) data).getDestIp();
        } else if (data instanceof SystemLogData) {
            return ((SystemLogData) data).getHostname();
        }
        return null;
    }
    
    private void updateAssetThreatScore(String assetId, ThreatResult threatResult) {
        AssetThreatScore assetScore = assetThreatScores.computeIfAbsent(assetId, 
                k -> new AssetThreatScore(assetId));
        
        // 计算威胁权重
        double threatTypeWeight = threatTypeWeights.getOrDefault(
                threatResult.getThreatType(), 0.5);
        
        double threatLevelWeight = threatLevelWeights.getOrDefault(
                threatResult.getThreatLevel(), 0.5);
        
        // 应用时间衰减到现有分数
        assetScore.applyTimeDecay(timeDecayFactor);
        
        // 计算新威胁的贡献分数
        double detectionScore = threatResult.getDetectionScore();
        double threatContribution = detectionScore * threatTypeWeight * threatLevelWeight;
        
        // 更新资产分数
        assetScore.addThreatScore(threatContribution);
        
        // 添加威胁事件记录
        assetScore.addThreatEvent(threatResult);
        
        // 记录资产信息
        if (threatResult.getDestIp() != null) {
            assetScore.setIpAddress(threatResult.getDestIp());
        }
        
        log.debug("Updated asset threat score: {} - Score: {}", assetId, assetScore.getCurrentScore());
    }
    
    private void generateAssetThreatResults(List<ThreatResult> results) {
        // 检查是否需要生成定期资产威胁报告
        if (shouldGenerateAssetReport()) {
            for (AssetThreatScore assetScore : assetThreatScores.values()) {
                // 只为高风险资产生成报告
                if (assetScore.getCurrentThreatLevel().equals("CRITICAL") ||
                    assetScore.getCurrentThreatLevel().equals("HIGH")) {
                    ThreatResult report = createAssetThreatReport(assetScore);
                    results.add(report);
                }
            }
        }
    }
    
    private boolean shouldGenerateAssetReport() {
        // 简单实现：每小时生成一次报告
        long currentTime = System.currentTimeMillis();
        int currentMinute = (int) ((currentTime / (1000 * 60)) % 60);
        return currentMinute == 0; // 整点生成报告
    }
    
    private ThreatResult createVulnerabilityThreatResult(VulnerabilityData vulnerabilityData) {
        ThreatResult result = new ThreatResult();
        result.setDetectionId("VULN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setTimestamp(System.currentTimeMillis());
        result.setThreatType("VULNERABILITY");
        
        // 根据CVSS分数设置威胁等级
        double cvssScore = vulnerabilityData.getCvssScore();
        if (cvssScore >= 9.0) {
            result.setThreatLevel("CRITICAL");
            result.setDetectionScore(100.0);
        } else if (cvssScore >= 7.0) {
            result.setThreatLevel("HIGH");
            result.setDetectionScore(80.0);
        } else if (cvssScore >= 4.0) {
            result.setThreatLevel("MEDIUM");
            result.setDetectionScore(60.0);
        } else {
            result.setThreatLevel("LOW");
            result.setDetectionScore(30.0);
        }
        
        result.setSourceIp(null);
        result.setDestIp(vulnerabilityData.getAssetIp());
        result.setDescription("资产存在漏洞: " + vulnerabilityData.getCveId() + " - " + vulnerabilityData.getDescription());
        result.setDetectorName(getDetectorName());
        result.setRecommendedAction("修复漏洞: " + vulnerabilityData.getCveId());
        
        // 添加额外信息
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("asset_id", vulnerabilityData.getAssetId());
        additionalInfo.put("cve_id", vulnerabilityData.getCveId());
        additionalInfo.put("cvss_score", String.valueOf(cvssScore));
        additionalInfo.put("asset_type", vulnerabilityData.getAssetType());
        result.setAdditionalInfo(additionalInfo);
        
        return result;
    }
    
    private ThreatResult createAssetThreatLevelResult(AssetThreatScore assetScore) {
        ThreatResult result = new ThreatResult();
        result.setDetectionId("ASSET-" + UUID.randomUUID().toString().substring(0, 8));
        result.setTimestamp(System.currentTimeMillis());
        result.setThreatType("ASSET_THREAT_LEVEL_CHANGE");
        result.setThreatLevel(assetScore.getCurrentThreatLevel());
        result.setDetectionScore(assetScore.getCurrentScore());
        
        result.setSourceIp(null);
        result.setDestIp(assetScore.getIpAddress());
        result.setDescription(String.format("资产威胁等级变更: %s (当前: %s, 之前: %s, 分数: %.2f)", 
                assetScore.getAssetId(), 
                assetScore.getCurrentThreatLevel(), 
                assetScore.getPreviousThreatLevel(), 
                assetScore.getCurrentScore()));
        result.setDetectorName(getDetectorName());
        result.setRecommendedAction(getRecommendedActionForThreatLevel(assetScore.getCurrentThreatLevel()));
        
        // 添加额外信息
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("asset_id", assetScore.getAssetId());
        additionalInfo.put("threat_count", String.valueOf(assetScore.getThreatEventCount()));
        additionalInfo.put("last_threat_time", String.valueOf(assetScore.getLastThreatTime()));
        result.setAdditionalInfo(additionalInfo);
        
        return result;
    }
    
    private ThreatResult createAssetThreatReport(AssetThreatScore assetScore) {
        ThreatResult result = new ThreatResult();
        result.setDetectionId("REPORT-" + UUID.randomUUID().toString().substring(0, 8));
        result.setTimestamp(System.currentTimeMillis());
        result.setThreatType("ASSET_THREAT_REPORT");
        result.setThreatLevel(assetScore.getCurrentThreatLevel());
        result.setDetectionScore(assetScore.getCurrentScore());
        
        result.setSourceIp(null);
        result.setDestIp(assetScore.getIpAddress());
        
        // 构建详细报告描述
        StringBuilder description = new StringBuilder();
        description.append("资产威胁状态报告: ")
                .append(assetScore.getAssetId())
                .append("\n");
        description.append("当前威胁等级: ")
                .append(assetScore.getCurrentThreatLevel())
                .append("\n");
        description.append("威胁分数: ")
                .append(String.format("%.2f", assetScore.getCurrentScore()))
                .append("\n");
        description.append("威胁事件数量: ")
                .append(assetScore.getThreatEventCount())
                .append("\n");
        description.append("最近威胁时间: ")
                .append(new Date(assetScore.getLastThreatTime()));
        
        result.setDescription(description.toString());
        result.setDetectorName(getDetectorName());
        result.setRecommendedAction(getRecommendedActionForThreatLevel(assetScore.getCurrentThreatLevel()));
        
        return result;
    }
    
    private String getRecommendedActionForThreatLevel(String threatLevel) {
        switch (threatLevel) {
            case "CRITICAL":
                return "立即隔离受影响资产，启动应急响应流程";
            case "HIGH":
                return "优先处理高危威胁，增强监控频率";
            case "MEDIUM":
                return "安排处理时间，持续监控资产状态";
            case "LOW":
                return "纳入常规安全维护计划";
            default:
                return "持续监控资产状态";
        }
    }
    
    private void cleanupExpiredAssetScores() {
        // 清理长时间没有活动的资产威胁数据
        long currentTime = System.currentTimeMillis();
        long expiryTimeMs = detectionProperties.getAssetThreatScoreExpiry() * 24 * 60 * 60 * 1000; // 转换为毫秒
        
        assetThreatScores.entrySet().removeIf(entry -> {
            AssetThreatScore assetScore = entry.getValue();
            return (currentTime - assetScore.getLastActivityTime() > expiryTimeMs) &&
                   assetScore.getCurrentScore() == 0.0;
        });
    }
    
    @Override
    public void updateDetectionRules(Map<String, Object> updateData) {
        // 更新威胁类型权重
        if (updateData.containsKey("threat_type_weights")) {
            Object weights = updateData.get("threat_type_weights");
            if (weights instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Double> newWeights = (Map<String, Double>) weights;
                threatTypeWeights.putAll(newWeights);
                log.info("Updated threat type weights: {}", newWeights);
            }
        }
        
        // 更新威胁等级权重
        if (updateData.containsKey("threat_level_weights")) {
            Object weights = updateData.get("threat_level_weights");
            if (weights instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Double> newWeights = (Map<String, Double>) weights;
                threatLevelWeights.putAll(newWeights);
                log.info("Updated threat level weights: {}", newWeights);
            }
        }
        
        // 更新评分阈值
        if (updateData.containsKey("score_thresholds")) {
            Object thresholds = updateData.get("score_thresholds");
            if (thresholds instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Double> newThresholds = (Map<String, Double>) thresholds;
                // 更新所有资产评分对象的阈值
                assetThreatScores.values().forEach(score -> score.updateThresholds(newThresholds));
                log.info("Updated score thresholds: {}", newThresholds);
            }
        }
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("asset_count", assetThreatScores.size());
        
        // 统计各威胁等级的资产数量
        Map<String, Long> threatLevelStats = assetThreatScores.values().stream()
                .collect(Collectors.groupingBy(AssetThreatScore::getCurrentThreatLevel, Collectors.counting()));
        status.put("threat_level_stats", threatLevelStats);
        
        return status;
    }
    
    @Override
    public String getDetectorName() {
        return "Threat Scorer";
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            assetThreatScores.clear();
            log.info("Threat Scorer shutdown");
        }
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get();
    }
    
    /**
     * 获取指定资产的威胁分数
     */
    public Double getAssetThreatScore(String assetId) {
        AssetThreatScore assetScore = assetThreatScores.get(assetId);
        return assetScore != null ? assetScore.getCurrentScore() : 0.0;
    }
    
    /**
     * 获取指定资产的威胁等级
     */
    public String getAssetThreatLevel(String assetId) {
        AssetThreatScore assetScore = assetThreatScores.get(assetId);
        return assetScore != null ? assetScore.getCurrentThreatLevel() : "LOW";
    }
    
    /**
     * 资产威胁分数内部类
     */
    private static class AssetThreatScore {
        private final String assetId;
        private String ipAddress;
        private double currentScore = 0.0;
        private String currentThreatLevel = "LOW";
        private String previousThreatLevel = "LOW";
        private long lastActivityTime;
        private long lastThreatTime;
        private int threatEventCount = 0;
        
        // 威胁等级阈值
        private double criticalThreshold = 80.0;
        private double highThreshold = 60.0;
        private double mediumThreshold = 40.0;
        private double lowThreshold = 20.0;
        
        // 威胁事件记录（最近的10个）
        private final List<ThreatResult> recentThreatEvents = new ArrayList<>();
        
        public AssetThreatScore(String assetId) {
            this.assetId = assetId;
            this.lastActivityTime = System.currentTimeMillis();
            this.lastThreatTime = System.currentTimeMillis();
        }
        
        public void addThreatScore(double score) {
            this.currentScore = Math.min(100.0, this.currentScore + score);
            this.previousThreatLevel = this.currentThreatLevel;
            updateThreatLevel();
        }
        
        public void applyTimeDecay(double decayFactor) {
            // 应用时间衰减，使旧的威胁影响逐渐降低
            long timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime;
            
            // 如果超过1小时，应用衰减
            if (timeSinceLastActivity > 3600000) { // 1小时
                // 根据时间间隔计算衰减次数
                int decayIntervals = (int) (timeSinceLastActivity / 3600000);
                for (int i = 0; i < decayIntervals; i++) {
                    this.currentScore = Math.max(0.0, this.currentScore * decayFactor);
                }
                
                // 更新威胁等级
                updateThreatLevel();
            }
        }
        
        private void updateThreatLevel() {
            String oldLevel = this.currentThreatLevel;
            
            if (currentScore >= criticalThreshold) {
                this.currentThreatLevel = "CRITICAL";
            } else if (currentScore >= highThreshold) {
                this.currentThreatLevel = "HIGH";
            } else if (currentScore >= mediumThreshold) {
                this.currentThreatLevel = "MEDIUM";
            } else if (currentScore >= lowThreshold) {
                this.currentThreatLevel = "LOW";
            } else {
                this.currentThreatLevel = "LOW";
            }
            
            // 如果是第一次设置等级，保持previousThreatLevel不变
            if (oldLevel.equals("LOW") && previousThreatLevel.equals("LOW")) {
                this.previousThreatLevel = this.currentThreatLevel;
            }
        }
        
        public void addThreatEvent(ThreatResult threatResult) {
            this.recentThreatEvents.add(threatResult);
            // 只保留最近的10个事件
            if (this.recentThreatEvents.size() > 10) {
                this.recentThreatEvents.remove(0);
            }
            this.threatEventCount++;
            this.lastThreatTime = System.currentTimeMillis();
            this.lastActivityTime = System.currentTimeMillis();
        }
        
        public void updateLastActivityTime() {
            this.lastActivityTime = System.currentTimeMillis();
        }
        
        public void updateThresholds(Map<String, Double> thresholds) {
            if (thresholds.containsKey("critical")) {
                this.criticalThreshold = thresholds.get("critical");
            }
            if (thresholds.containsKey("high")) {
                this.highThreshold = thresholds.get("high");
            }
            if (thresholds.containsKey("medium")) {
                this.mediumThreshold = thresholds.get("medium");
            }
            if (thresholds.containsKey("low")) {
                this.lowThreshold = thresholds.get("low");
            }
            
            // 更新威胁等级
            this.previousThreatLevel = this.currentThreatLevel;
            updateThreatLevel();
        }
        
        public String getAssetId() { return assetId; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public double getCurrentScore() { return currentScore; }
        public String getCurrentThreatLevel() { return currentThreatLevel; }
        public String getPreviousThreatLevel() { return previousThreatLevel; }
        public long getLastActivityTime() { return lastActivityTime; }
        public long getLastThreatTime() { return lastThreatTime; }
        public int getThreatEventCount() { return threatEventCount; }
        public boolean getThreatLevelChanged() { 
            return !currentThreatLevel.equals(previousThreatLevel); 
        }
        public List<ThreatResult> getRecentThreatEvents() { 
            return new ArrayList<>(recentThreatEvents); 
        }
    }
}