package com.example.securitydefense.situation.evaluation;

import com.example.securitydefense.situation.config.EvaluationProperties;
import com.example.securitydefense.situation.model.AssetRisk;
import com.example.securitydefense.situation.model.SituationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 加权综合评价模型的态势评估引擎实现
 * 基于资产重要性、威胁活跃度和漏洞分布的加权计算
 */
@Component
public class WeightedSituationEvaluator implements SituationEvaluator {
    
    @Autowired
    private EvaluationProperties evaluationProperties;
    
    /**
     * 引擎状态
     */
    private boolean initialized = false;
    
    /**
     * 资产风险缓存
     */
    private final Map<String, AssetRisk> assetRiskCache = new ConcurrentHashMap<>();
    
    /**
     * 最近态势评估结果
     */
    private SituationData latestSituationData;
    
    @Override
    public void initialize() {
        // 初始化态势评估引擎
        System.out.println("初始化加权综合评价模型态势评估引擎...");
        initialized = true;
        System.out.println("态势评估引擎初始化完成。");
    }
    
    @Override
    public SituationData calculateOverallSituation() {
        // 计算整体安全态势指数
        SituationData situationData = new SituationData();
        situationData.setEvaluationId(generateEvaluationId());
        situationData.setTimestamp(LocalDateTime.now());
        situationData.setEvaluatorName(getEvaluatorName());
        situationData.setStatus("COMPLETED");
        
        // 获取所有资产风险评估结果
        List<AssetRisk> allAssetRisks = getAllAssetRisks();
        
        // 计算各维度得分
        double assetScore = calculateAssetDimensionScore(allAssetRisks);
        double threatScore = calculateThreatDimensionScore(allAssetRisks);
        double vulnerabilityScore = calculateVulnerabilityDimensionScore(allAssetRisks);
        
        // 应用权重计算综合得分
        double overallScore = calculateWeightedScore(assetScore, threatScore, vulnerabilityScore);
        situationData.setSituationScore(overallScore);
        
        // 确定态势等级
        String situationLevel = getLevelByScore(overallScore);
        situationData.setSituationLevel(situationLevel);
        
        // 计算涉事资产数量
        int affectedAssetCount = (int) allAssetRisks.stream()
                .filter(assetRisk -> assetRisk.getRiskScore() > 0)
                .count();
        situationData.setAffectedAssetCount(affectedAssetCount);
        
        // 计算活跃威胁数量
        int activeThreatCount = allAssetRisks.stream()
                .mapToInt(AssetRisk::getThreatEventCount)
                .sum();
        situationData.setActiveThreatCount(activeThreatCount);
        
        // 计算高危漏洞数量
        int highVulnerabilityCount = allAssetRisks.stream()
                .mapToInt(AssetRisk::getHighVulnerabilityCount)
                .sum();
        situationData.setHighVulnerabilityCount(highVulnerabilityCount);
        
        // 计算威胁类型分布
        Map<String, Integer> threatTypeDistribution = calculateThreatTypeDistribution(allAssetRisks);
        situationData.setThreatTypeDistribution(threatTypeDistribution);
        
        // 计算资产风险分布
        Map<String, Integer> assetRiskDistribution = calculateAssetRiskDistribution(allAssetRisks);
        situationData.setAssetRiskDistribution(assetRiskDistribution);
        
        // 获取主要威胁
        List<SituationData.ThreatSummary> majorThreats = getMajorThreats(allAssetRisks);
        situationData.setMajorThreats(majorThreats);
        
        // 获取风险资产
        List<SituationData.AssetRiskSummary> riskyAssets = getRiskyAssets(allAssetRisks);
        situationData.setRiskyAssets(riskyAssets);
        
        // 设置维度得分
        SituationData.DimensionScores dimensionScores = new SituationData.DimensionScores();
        dimensionScores.setAssetScore(assetScore);
        dimensionScores.setThreatScore(threatScore);
        dimensionScores.setVulnerabilityScore(vulnerabilityScore);
        dimensionScores.setAssetWeight(evaluationProperties.getAssetWeight());
        dimensionScores.setThreatWeight(evaluationProperties.getThreatWeight());
        dimensionScores.setVulnerabilityWeight(evaluationProperties.getVulnerabilityWeight());
        situationData.setDimensionScores(dimensionScores);
        
        // 保存最近的态势评估结果
        latestSituationData = situationData;
        
        return situationData;
    }
    
    @Override
    public AssetRisk evaluateAssetRisk(String assetId) {
        // 评估单个资产的风险
        AssetRisk assetRisk = assetRiskCache.get(assetId);
        if (assetRisk == null) {
            // 如果缓存中没有，创建新的资产风险评估结果
            assetRisk = createDefaultAssetRisk(assetId);
            assetRiskCache.put(assetId, assetRisk);
        }
        
        // 重新计算资产风险评分
        recalculateAssetRiskScore(assetRisk);
        
        return assetRisk;
    }
    
    @Override
    public Map<String, AssetRisk> evaluateAssetRisks(List<String> assetIds) {
        // 批量评估多个资产的风险
        return assetIds.stream()
                .collect(Collectors.toMap(
                        assetId -> assetId,
                        this::evaluateAssetRisk
                ));
    }
    
    @Override
    public List<AssetRisk> getAllAssetRisks() {
        // 获取所有资产的风险评估结果
        // 注意：这里应该从资产服务或数据库获取所有资产并评估风险
        // 这里为了简化，我们返回缓存中的所有资产风险
        return new ArrayList<>(assetRiskCache.values());
    }
    
    @Override
    public void updateEvaluationConfig(Map<String, Object> config) {
        // 更新评估配置
        if (config.containsKey("assetWeight")) {
            evaluationProperties.setAssetWeight((double) config.get("assetWeight"));
        }
        if (config.containsKey("threatWeight")) {
            evaluationProperties.setThreatWeight((double) config.get("threatWeight"));
        }
        if (config.containsKey("vulnerabilityWeight")) {
            evaluationProperties.setVulnerabilityWeight((double) config.get("vulnerabilityWeight"));
        }
        // 其他配置更新...
    }
    
    @Override
    public Map<String, Object> getStatus() {
        // 获取评估引擎状态
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized);
        status.put("evaluatorName", getEvaluatorName());
        status.put("assetCount", assetRiskCache.size());
        status.put("lastEvaluationTime", latestSituationData != null ? latestSituationData.getTimestamp() : null);
        return status;
    }
    
    @Override
    public String getEvaluatorName() {
        return "WeightedSituationEvaluator";
    }
    
    @Override
    public void shutdown() {
        // 关闭评估引擎
        initialized = false;
        assetRiskCache.clear();
        latestSituationData = null;
    }
    
    @Override
    public boolean isAvailable() {
        return initialized;
    }
    
    /**
     * 生成评估ID
     */
    private String generateEvaluationId() {
        return "eval_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 计算资产维度得分
     */
    private double calculateAssetDimensionScore(List<AssetRisk> assetRisks) {
        if (assetRisks.isEmpty()) {
            return 0;
        }
        
        // 基于业务重要性和风险等级计算资产维度得分
        double totalScore = 0;
        double totalWeight = 0;
        
        for (AssetRisk assetRisk : assetRisks) {
            double importanceFactor = getBusinessImportanceFactor(assetRisk.getBusinessImportance());
            double riskLevelFactor = getRiskLevelFactor(assetRisk.getRiskLevel());
            double assetScore = importanceFactor * riskLevelFactor * 100;
            
            totalScore += assetScore;
            totalWeight += importanceFactor;
        }
        
        return totalWeight > 0 ? totalScore / totalWeight : 0;
    }
    
    /**
     * 计算威胁维度得分
     */
    private double calculateThreatDimensionScore(List<AssetRisk> assetRisks) {
        if (assetRisks.isEmpty()) {
            return 0;
        }
        
        // 计算活跃威胁的加权得分
        int totalThreatCount = assetRisks.stream()
                .mapToInt(AssetRisk::getThreatEventCount)
                .sum();
        int totalHighThreatCount = 0; // AssetRisk类中没有highThreatCount字段
        
        // 基于威胁数量和高危威胁比例计算得分
        double threatDensity = (double) totalThreatCount / assetRisks.size();
        double highThreatRatio = totalThreatCount > 0 ? (double) totalHighThreatCount / totalThreatCount : 0;
        
        return (threatDensity * 20 + highThreatRatio * 80);
    }
    
    /**
     * 计算漏洞维度得分
     */
    private double calculateVulnerabilityDimensionScore(List<AssetRisk> assetRisks) {
        if (assetRisks.isEmpty()) {
            return 0;
        }
        
        // 计算漏洞的加权得分
        int totalVulnerabilityCount = assetRisks.stream()
                .mapToInt(AssetRisk::getVulnerabilityCount)
                .sum();
        int totalHighVulnerabilityCount = assetRisks.stream()
                .mapToInt(AssetRisk::getHighVulnerabilityCount)
                .sum();
        
        // 基于漏洞数量和高危漏洞比例计算得分
        double vulnerabilityDensity = (double) totalVulnerabilityCount / assetRisks.size();
        double highVulnerabilityRatio = totalVulnerabilityCount > 0 ? (double) totalHighVulnerabilityCount / totalVulnerabilityCount : 0;
        
        return (vulnerabilityDensity * 15 + highVulnerabilityRatio * 85);
    }
    
    /**
     * 计算加权综合得分
     */
    private double calculateWeightedScore(double assetScore, double threatScore, double vulnerabilityScore) {
        double assetWeight = evaluationProperties.getAssetWeight();
        double threatWeight = evaluationProperties.getThreatWeight();
        double vulnerabilityWeight = evaluationProperties.getVulnerabilityWeight();
        
        return assetScore * assetWeight + threatScore * threatWeight + vulnerabilityScore * vulnerabilityWeight;
    }
    
    /**
     * 计算威胁类型分布
     */
    private Map<String, Integer> calculateThreatTypeDistribution(List<AssetRisk> assetRisks) {
        Map<String, Integer> distribution = new HashMap<>();
        
        // AssetRisk类中没有getRelatedThreats方法，暂时返回空的分布
        return distribution;
    }
    
    /**
     * 计算资产风险分布
     */
    private Map<String, Integer> calculateAssetRiskDistribution(List<AssetRisk> assetRisks) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (AssetRisk assetRisk : assetRisks) {
            String riskLevel = getRiskLevelString(assetRisk.getRiskLevel());
            distribution.put(riskLevel, 
                    distribution.getOrDefault(riskLevel, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * 获取主要威胁列表
     */
    private List<SituationData.ThreatSummary> getMajorThreats(List<AssetRisk> assetRisks) {
        // AssetRisk类中没有getRelatedThreats方法，暂时返回空列表
        return new ArrayList<>();
    }
    
    /**
     * 获取风险资产列表
     */
    private List<SituationData.AssetRiskSummary> getRiskyAssets(List<AssetRisk> assetRisks) {
        // 按风险评分排序，返回前10个高风险资产
        return assetRisks.stream()
                .filter(assetRisk -> assetRisk.getRiskScore() > 0)
                .map(this::createAssetRiskSummary)
                .sorted(Comparator.comparingDouble(SituationData.AssetRiskSummary::getRiskScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建威胁摘要
     */
    private SituationData.ThreatSummary createThreatSummary(/*AssetRisk.ThreatInfo*/ Object threat) {
        SituationData.ThreatSummary summary = new SituationData.ThreatSummary();
        // 由于AssetRisk.ThreatInfo不存在，暂时不设置任何属性
        return summary;
    }
    
    /**
     * 创建资产风险摘要
     */
    private SituationData.AssetRiskSummary createAssetRiskSummary(AssetRisk assetRisk) {
        SituationData.AssetRiskSummary summary = new SituationData.AssetRiskSummary();
        summary.setAssetId(assetRisk.getAssetId());
        summary.setAssetName(assetRisk.getAssetName());
        summary.setAssetType(assetRisk.getAssetType());
        summary.setRiskScore(assetRisk.getRiskScore());
        summary.setRiskLevel(getRiskLevelString(assetRisk.getRiskLevel()));
        summary.setThreatCount(assetRisk.getThreatEventCount());
        summary.setVulnerabilityCount(assetRisk.getVulnerabilityCount());
        return summary;
    }
    
    /**
     * 获取风险等级因子
     */
    private double getRiskLevelFactor(Integer riskLevel) {
        if (riskLevel == null) {
            return 0.0;
        }
        
        switch (riskLevel) {
            case 4: // CRITICAL
                return 1.0;
            case 3: // HIGH
                return 0.8;
            case 2: // MEDIUM
                return 0.5;
            case 1: // LOW
                return 0.2;
            default:
                return 0.0;
        }
    }
    
    /**
     * 获取业务重要性因子
     */
    private double getBusinessImportanceFactor(Integer businessImportance) {
        if (businessImportance == null) {
            return 1.0;
        }
        
        switch (businessImportance) {
            case 3: // 核心业务
                return 1.5;
            case 2: // 重要业务
                return 1.2;
            case 1: // 一般业务
                return 1.0;
            default:
                return 1.0;
        }
    }
    
    /**
     * 创建默认资产风险对象
     */
    private AssetRisk createDefaultAssetRisk(String assetId) {
        AssetRisk assetRisk = new AssetRisk();
        assetRisk.setAssetId(assetId);
        assetRisk.setAssetName("Asset-" + assetId);
        assetRisk.setAssetType("UNKNOWN");
        assetRisk.setBusinessImportance(1); // 1-一般业务
        assetRisk.setRiskScore(0.0);
        assetRisk.setRiskLevel(1); // 1-低风险
        assetRisk.setThreatEventCount(0);
        assetRisk.setVulnerabilityCount(0);
        assetRisk.setHighVulnerabilityCount(0);
        assetRisk.setMediumVulnerabilityCount(0);
        assetRisk.setLowVulnerabilityCount(0);
        assetRisk.setLastThreatTime(null);
        assetRisk.setRiskTrend(2); // 2-稳定
        assetRisk.setPrimaryRiskSource("NONE");
        assetRisk.setRemediationSuggestions("{}");
        assetRisk.setResponsiblePerson("Unknown");
        assetRisk.setSecurityStatus(1); // 1-正常
        assetRisk.setLastScanTime(null);
        assetRisk.setDeleted(0);
        
        return assetRisk;
    }
    
    /**
     * 重新计算资产风险评分
     */
    private void recalculateAssetRiskScore(AssetRisk assetRisk) {
        // 基于威胁、漏洞和业务重要性计算资产风险评分
        double threatFactor = calculateThreatFactor(assetRisk);
        double vulnerabilityFactor = calculateVulnerabilityFactor(assetRisk);
        double importanceFactor = getBusinessImportanceFactor(assetRisk.getBusinessImportance());
        
        // 组合计算风险评分
        double baseScore = (threatFactor * 0.4 + vulnerabilityFactor * 0.6) * 100;
        double finalScore = Math.min(100, baseScore * importanceFactor);
        
        assetRisk.setRiskScore(finalScore);        
        // 确定风险等级
        String riskLevel = getRiskLevelString((int) finalScore);
        assetRisk.setRiskLevel(getRiskLevelInteger(riskLevel));
        
        // 更新最后评估时间
        // AssetRisk类中没有setLastEvaluationTime方法，暂时注释掉
        // assetRisk.setLastEvaluationTime(LocalDateTime.now());
        
        // 更新风险构成
        updateRiskComposition(assetRisk, threatFactor, vulnerabilityFactor, importanceFactor);
    }
    
    /**
     * 计算威胁因素
     */
    private double calculateThreatFactor(AssetRisk assetRisk) {
        int threatCount = assetRisk.getThreatEventCount();
        int highThreatCount = 0; // AssetRisk类中没有getHighThreatCount方法
        
        // 基于威胁数量和高危威胁比例计算威胁因素
        double countFactor = Math.min(1.0, (double) threatCount / 10);
        double severityFactor = highThreatCount > 0 ? (double) highThreatCount / threatCount : 0;
        
        return countFactor * (0.3 + 0.7 * severityFactor);
    }
    
    /**
     * 计算漏洞因素
     */
    private double calculateVulnerabilityFactor(AssetRisk assetRisk) {
        int vulnerabilityCount = assetRisk.getVulnerabilityCount();
        int highVulnerabilityCount = assetRisk.getHighVulnerabilityCount();
        
        // 基于漏洞数量和高危漏洞比例计算漏洞因素
        double countFactor = Math.min(1.0, (double) vulnerabilityCount / 20);
        double severityFactor = highVulnerabilityCount > 0 ? (double) highVulnerabilityCount / vulnerabilityCount : 0;
        
        return countFactor * (0.4 + 0.6 * severityFactor);
    }
    
    /**
     * 更新风险构成
     */
    private void updateRiskComposition(AssetRisk assetRisk, double threatFactor, 
                                      double vulnerabilityFactor, double importanceFactor) {
        // AssetRisk类中没有RiskComposition和getRiskComposition方法，暂时不实现
    }
    
    /**
     * 将风险等级数字转换为字符串
     */
    private String getRiskLevelString(Integer riskLevel) {
        if (riskLevel == null) {
            return "UNKNOWN";
        }
        
        switch (riskLevel) {
            case 4:
                return "CRITICAL";
            case 3:
                return "HIGH";
            case 2:
                return "MEDIUM";
            case 1:
                return "LOW";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * 将风险等级字符串转换为整数
     */
    private Integer getRiskLevelInteger(String riskLevel) {
        if (riskLevel == null) {
            return 1; // 默认低风险
        }
        
        switch (riskLevel) {
            case "CRITICAL":
                return 4;
            case "HIGH":
                return 3;
            case "MEDIUM":
                return 2;
            case "LOW":
                return 1;
            default:
                return 1;
        }
    }
    
    /**
     * 根据得分获取等级
     */
    private String getLevelByScore(double score) {
        if (score >= evaluationProperties.getHighRiskThreshold()) {
            return "HIGH";
        } else if (score >= evaluationProperties.getMediumRiskThreshold()) {
            return "MEDIUM";
        } else if (score >= evaluationProperties.getLowRiskThreshold()) {
            return "LOW";
        } else {
            return "SAFE";
        }
    }
}