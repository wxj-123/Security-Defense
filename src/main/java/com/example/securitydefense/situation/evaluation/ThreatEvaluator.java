package com.example.securitydefense.situation.evaluation;

import com.example.securitydefense.situation.config.EvaluationProperties;
import com.example.securitydefense.situation.model.ThreatResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 威胁性评估器
 * 实时威胁活跃度分析
 */
@Component
public class ThreatEvaluator {
    
    @Autowired
    private EvaluationProperties evaluationProperties;
    
    /**
     * 威胁类型权重映射
     */
    private final Map<String, Double> threatTypeWeightMap = new HashMap<String, Double>() {
        {   
            put("APT", 1.5);
            put("RANSOMWARE", 1.4);
            put("DATA_BREACH", 1.3);
            put("SQL_INJECTION", 1.2);
            put("XSS", 1.1);
            put("CSRF", 1.0);
            put("MALWARE", 1.3);
            put("PHISHING", 1.2);
            put("DDoS", 1.0);
            put("BRUTE_FORCE", 0.9);
            put("PRIVILEGE_ESCALATION", 1.3);
            put("LATERAL_MOVEMENT", 1.2);
            put("DATA_EXFILTRATION", 1.4);
        }
    };
    
    /**
     * 威胁等级权重映射
     */
    private final Map<String, Double> threatLevelWeightMap = new HashMap<String, Double>() {
        {   
            put("CRITICAL", 10.0);
            put("HIGH", 8.0);
            put("MEDIUM", 5.0);
            put("LOW", 2.0);
            put("INFO", 1.0);
        }
    };
    
    /**
     * 评估实时威胁活跃度
     * 
     * @param recentThreats 近期威胁列表
     * @param historicalThreats 历史威胁数据
     * @param timeWindow 时间窗口（分钟）
     * @return 威胁活跃度评分（0-100分）
     */
    public double evaluateThreatActivity(List<ThreatResult> recentThreats, 
                                       List<ThreatSnapshot> historicalThreats, 
                                       int timeWindow) {
        
        if (recentThreats == null || recentThreats.isEmpty()) {
            return 0.0;
        }
        
        // 计算基础威胁评分
        double baseThreatScore = calculateBaseThreatScore(recentThreats);
        
        // 计算威胁密度评分
        double densityScore = calculateThreatDensity(recentThreats, timeWindow);
        
        // 计算威胁趋势评分
        double trendScore = calculateThreatTrend(recentThreats, historicalThreats);
        
        // 计算攻击链完整性评分
        double attackChainScore = calculateAttackChainCompleteness(recentThreats);
        
        // 计算综合威胁活跃度评分
        double threatWeight = evaluationProperties.getThreatWeight();
        
        double activityScore = (
            baseThreatScore * 0.4 + 
            densityScore * 0.2 + 
            trendScore * 0.2 + 
            attackChainScore * 0.2
        ) * threatWeight;
        
        return Math.min(100.0, activityScore);
    }
    
    /**
     * 计算基础威胁评分
     */
    private double calculateBaseThreatScore(List<ThreatResult> threats) {
        if (threats.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;
        
        for (ThreatResult threat : threats) {
            // 获取威胁等级权重
            double levelWeight = threatLevelWeightMap.getOrDefault(
                    threat.getThreatLevel(), 1.0);
            
            // 获取威胁类型权重
            double typeWeight = getThreatTypeWeight(threat.getThreatType());
            
            // 计算威胁置信度因子
            double confidenceFactor = threat.getConfidenceScore() / 100.0;
            
            // 计算时间因子（越新的威胁权重越高）
            double timeFactor = calculateTimeFactor(threat.getDetectedTime());
            
            // 计算单个威胁得分
            double threatScore = levelWeight * typeWeight * confidenceFactor * timeFactor;
            totalScore += threatScore;
            
            // 统计各等级威胁数量
            String level = threat.getThreatLevel();
            if (level != null) {
                switch (level) {
                    case "CRITICAL":
                        criticalCount++;
                        break;
                    case "HIGH":
                        highCount++;
                        break;
                    case "MEDIUM":
                        mediumCount++;
                        break;
                    case "LOW":
                        lowCount++;
                        break;
                }
            }
        }
        
        // 计算威胁分布因子
        double distributionFactor = calculateThreatDistributionFactor(
                criticalCount, highCount, mediumCount, lowCount, threats.size());
        
        // 计算平均威胁得分
        double avgScore = totalScore / threats.size();
        
        // 基础威胁评分 = 平均得分 * 分布因子
        return avgScore * distributionFactor;
    }
    
    /**
     * 计算威胁密度
     */
    private double calculateThreatDensity(List<ThreatResult> threats, int timeWindow) {
        if (timeWindow <= 0 || threats.isEmpty()) {
            return 0.0;
        }
        
        // 计算单位时间内的威胁数量
        double threatsPerMinute = (double) threats.size() / timeWindow;
        
        // 将威胁密度映射到0-100分
        if (threatsPerMinute <= 0.1) {
            return 10.0;  // 极低密度
        } else if (threatsPerMinute <= 0.5) {
            return 30.0;  // 低密度
        } else if (threatsPerMinute <= 1.0) {
            return 50.0;  // 中等密度
        } else if (threatsPerMinute <= 5.0) {
            return 70.0;  // 高密度
        } else if (threatsPerMinute <= 10.0) {
            return 85.0;  // 极高密度
        } else {
            return 100.0; // 饱和密度
        }
    }
    
    /**
     * 计算威胁趋势
     */
    private double calculateThreatTrend(List<ThreatResult> currentThreats, 
                                      List<ThreatSnapshot> historicalThreats) {
        if (historicalThreats == null || historicalThreats.size() < 2) {
            return 50.0;  // 历史数据不足，返回中等分数
        }
        
        // 按时间排序
        historicalThreats.sort(Comparator.comparing(ThreatSnapshot::getTimestamp));
        
        // 获取最近的历史快照
        ThreatSnapshot latestHistorical = historicalThreats.get(historicalThreats.size() - 1);
        
        // 计算当前威胁数量
        int currentCriticalCount = countThreatsByLevel(currentThreats, "CRITICAL");
        int currentHighCount = countThreatsByLevel(currentThreats, "HIGH");
        int currentTotalCount = currentThreats.size();
        
        // 计算变化率
        double criticalChangeRate = calculateChangeRate(
                latestHistorical.getCriticalThreatCount(), currentCriticalCount);
        double highChangeRate = calculateChangeRate(
                latestHistorical.getHighThreatCount(), currentHighCount);
        double totalChangeRate = calculateChangeRate(
                latestHistorical.getTotalThreatCount(), currentTotalCount);
        
        // 综合计算趋势分数
        // 高危和危急威胁变化权重更高
        double trendScore = (
            criticalChangeRate * 0.4 + 
            highChangeRate * 0.3 + 
            totalChangeRate * 0.3
        ) * 100.0;
        
        // 确保分数在0-100范围内
        return Math.min(100.0, Math.max(0.0, trendScore));
    }
    
    /**
     * 计算攻击链完整性
     */
    private double calculateAttackChainCompleteness(List<ThreatResult> threats) {
        // 获取所有威胁的攻击阶段
        Set<String> attackPhases = threats.stream()
                .map(ThreatResult::getAttackPhase)
                .filter(Objects::nonNull)
                .filter(phase -> !phase.isEmpty())
                .collect(Collectors.toSet());
        
        if (attackPhases.isEmpty()) {
            return 0.0;
        }
        
        // MITRE ATT&CK框架的典型攻击阶段数量
        int totalAttackPhases = 14; // 侦察、资源开发、初始访问、执行等
        
        // 计算攻击链覆盖度
        double coverage = (double) attackPhases.size() / totalAttackPhases;
        
        // 检查是否存在关键攻击阶段
        boolean hasInitialAccess = attackPhases.stream().anyMatch(phase -> 
                phase.contains("Initial Access") || phase.contains("初始访问"));
        boolean hasExecution = attackPhases.stream().anyMatch(phase -> 
                phase.contains("Execution") || phase.contains("执行"));
        boolean hasPrivilegeEscalation = attackPhases.stream().anyMatch(phase -> 
                phase.contains("Privilege Escalation") || phase.contains("权限提升"));
        boolean hasLateralMovement = attackPhases.stream().anyMatch(phase -> 
                phase.contains("Lateral Movement") || phase.contains("横向移动"));
        boolean hasDataExfiltration = attackPhases.stream().anyMatch(phase -> 
                phase.contains("Exfiltration") || phase.contains("数据窃取"));
        
        // 关键攻击阶段权重
        int criticalPhaseCount = 0;
        if (hasInitialAccess) criticalPhaseCount++;
        if (hasExecution) criticalPhaseCount++;
        if (hasPrivilegeEscalation) criticalPhaseCount++;
        if (hasLateralMovement) criticalPhaseCount++;
        if (hasDataExfiltration) criticalPhaseCount++;
        
        // 计算关键阶段因子
        double criticalPhaseFactor = 1.0 + (criticalPhaseCount * 0.1);
        
        // 计算攻击链完整性得分
        double completenessScore = coverage * criticalPhaseFactor * 100.0;
        
        return Math.min(100.0, completenessScore);
    }
    
    /**
     * 获取威胁类型权重
     */
    private double getThreatTypeWeight(String threatType) {
        if (threatType == null) {
            return 1.0;
        }
        
        return threatTypeWeightMap.getOrDefault(threatType.toUpperCase(), 1.0);
    }
    
    /**
     * 计算时间因子
     */
    private double calculateTimeFactor(Date detectedTime) {
        if (detectedTime == null) {
            return 1.0;
        }
        
        long now = System.currentTimeMillis();
        long detected = detectedTime.getTime();
        long minutesSinceDetection = (now - detected) / (1000 * 60);
        
        // 越新的威胁，权重越高
        if (minutesSinceDetection <= 5) {
            return 1.5;  // 非常新的威胁
        } else if (minutesSinceDetection <= 30) {
            return 1.3;  // 较新的威胁
        } else if (minutesSinceDetection <= 60) {
            return 1.1;  // 近期威胁
        } else if (minutesSinceDetection <= 120) {
            return 1.0;  // 普通威胁
        } else if (minutesSinceDetection <= 360) {
            return 0.9;  // 较早的威胁
        } else {
            return 0.8;  // 早期威胁
        }
    }
    
    /**
     * 计算威胁分布因子
     */
    private double calculateThreatDistributionFactor(int criticalCount, int highCount, 
                                                  int mediumCount, int lowCount, 
                                                  int totalCount) {
        if (totalCount == 0) {
            return 1.0;
        }
        
        // 高危和危急威胁占比越高，分布因子越大
        double highSeverityRatio = (double) (criticalCount + highCount) / totalCount;
        
        if (highSeverityRatio >= 0.7) {
            return 1.4;
        } else if (highSeverityRatio >= 0.5) {
            return 1.2;
        } else if (highSeverityRatio >= 0.3) {
            return 1.0;
        } else if (highSeverityRatio >= 0.1) {
            return 0.9;
        } else {
            return 0.8;
        }
    }
    
    /**
     * 计算变化率
     */
    private double calculateChangeRate(int previousCount, int currentCount) {
        if (previousCount == 0) {
            return currentCount > 0 ? 1.0 : 0.0;
        }
        
        return (double) (currentCount - previousCount) / previousCount;
    }
    
    /**
     * 按等级统计威胁数量
     */
    private int countThreatsByLevel(List<ThreatResult> threats, String level) {
        return (int) threats.stream()
                .filter(threat -> level.equals(threat.getThreatLevel()))
                .count();
    }
    
    /**
     * 分析威胁来源
     */
    public Map<String, Integer> analyzeThreatSources(List<ThreatResult> threats) {
        return threats.stream()
                .filter(threat -> threat.getSourceIp() != null && !threat.getSourceIp().isEmpty())
                .collect(Collectors.groupingBy(
                        ThreatResult::getSourceIp,
                        Collectors.summingInt(threat -> 1)
                ));
    }
    
    /**
     * 分析威胁目标
     */
    public Map<String, Integer> analyzeThreatTargets(List<ThreatResult> threats) {
        return threats.stream()
                .filter(threat -> threat.getTargetAssetId() != null && !threat.getTargetAssetId().isEmpty())
                .collect(Collectors.groupingBy(
                        ThreatResult::getTargetAssetId,
                        Collectors.summingInt(threat -> 1)
                ));
    }
    
    /**
     * 分析威胁类型分布
     */
    public Map<String, Integer> analyzeThreatTypeDistribution(List<ThreatResult> threats) {
        return threats.stream()
                .filter(threat -> threat.getThreatType() != null && !threat.getThreatType().isEmpty())
                .collect(Collectors.groupingBy(
                        ThreatResult::getThreatType,
                        Collectors.summingInt(threat -> 1)
                ));
    }
    
    /**
     * 获取威胁活跃度等级
     */
    public String getThreatActivityLevel(double activityScore) {
        if (activityScore >= 80.0) {
            return "CRITICAL";
        } else if (activityScore >= 60.0) {
            return "HIGH";
        } else if (activityScore >= 40.0) {
            return "MEDIUM";
        } else if (activityScore >= 20.0) {
            return "LOW";
        } else {
            return "INFO";
        }
    }
    
    /**
     * 分析威胁时间分布
     */
    public Map<String, List<ThreatResult>> analyzeThreatTimeDistribution(
            List<ThreatResult> threats, TimeDistributionType type) {
        
        Map<String, List<ThreatResult>> result = new HashMap<>();
        
        for (ThreatResult threat : threats) {
            if (threat.getDetectedTime() == null) {
                continue;
            }
            
            String key;
            Calendar cal = Calendar.getInstance();
            cal.setTime(threat.getDetectedTime());
            
            switch (type) {
                case HOUR_OF_DAY:
                    key = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
                    break;
                case DAY_OF_WEEK:
                    key = String.valueOf(cal.get(Calendar.DAY_OF_WEEK));
                    break;
                case DAY_OF_MONTH:
                    key = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                    break;
                default:
                    key = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            }
            
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(threat);
        }
        
        return result;
    }
    
    /**
     * 威胁快照内部类
     */
    public static class ThreatSnapshot {
        private Date timestamp;
        private int totalThreatCount;
        private int criticalThreatCount;
        private int highThreatCount;
        private int mediumThreatCount;
        private int lowThreatCount;
        private Map<String, Integer> threatTypeDistribution;
        
        // Getter 方法
        public Date getTimestamp() {
            return timestamp;
        }
        
        public int getTotalThreatCount() {
            return totalThreatCount;
        }
        
        public int getCriticalThreatCount() {
            return criticalThreatCount;
        }
        
        public int getHighThreatCount() {
            return highThreatCount;
        }
        
        public int getMediumThreatCount() {
            return mediumThreatCount;
        }
        
        public int getLowThreatCount() {
            return lowThreatCount;
        }
        
        public Map<String, Integer> getThreatTypeDistribution() {
            return threatTypeDistribution;
        }
    }
    
    /**
     * 时间分布类型枚举
     */
    public enum TimeDistributionType {
        HOUR_OF_DAY,
        DAY_OF_WEEK,
        DAY_OF_MONTH
    }
}