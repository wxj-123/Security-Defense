package com.example.securitydefense.situation.evaluation;

import com.example.securitydefense.situation.config.EvaluationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 资产价值评估器
 * 基于业务重要性对资产进行分级和价值评估
 */
@Component
public class AssetValueEvaluator {
    
    @Autowired
    private EvaluationProperties evaluationProperties;
    
    /**
     * 核心业务关键字模式集合
     */
    private Set<Pattern> coreBusinessPatterns;
    
    /**
     * 初始化评估器
     */
    public void initialize() {
        // 初始化核心业务关键字模式
        String[] coreBusinessKeywords = evaluationProperties.getAssetImportance().getCoreBusinessKeywords();
        coreBusinessPatterns = Stream.of(coreBusinessKeywords)
                .map(keyword -> Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toSet());
    }
    
    /**
     * 评估资产价值
     * 
     * @param assetId 资产ID
     * @param assetName 资产名称
     * @param assetType 资产类型
     * @param assetAttributes 资产属性映射
     * @return 资产价值评分（0-100分）
     */
    public double evaluateAssetValue(String assetId, String assetName, String assetType, 
                                    Map<String, String> assetAttributes) {
        
        // 基础价值分数
        double baseValue = 50.0;
        
        // 应用资产类型权重
        double typeWeight = getAssetTypeWeight(assetType);
        double typeScore = baseValue * typeWeight;
        
        // 应用业务重要性权重
        String importanceLevel = assessBusinessImportance(assetName, assetAttributes);
        double importanceFactor = getImportanceFactor(importanceLevel);
        double importanceScore = typeScore * importanceFactor;
        
        // 应用额外属性调整
        double attributeAdjustment = calculateAttributeAdjustment(assetAttributes);
        double finalScore = Math.min(100.0, importanceScore + attributeAdjustment);
        
        return Math.max(0.0, finalScore);
    }
    
    /**
     * 评估业务重要性等级
     * 
     * @param assetName 资产名称
     * @param assetAttributes 资产属性
     * @return 业务重要性等级
     */
    public String assessBusinessImportance(String assetName, Map<String, String> assetAttributes) {
        // 检查是否为核心业务资产
        if (isCoreBusinessAsset(assetName, assetAttributes)) {
            return "CORE";
        }
        
        // 检查是否为业务资产
        if (isBusinessAsset(assetAttributes)) {
            return "BUSINESS";
        }
        
        // 检查是否为支撑资产
        if (isSupportAsset(assetAttributes)) {
            return "SUPPORT";
        }
        
        // 默认基础设施
        return "INFRASTRUCTURE";
    }
    
    /**
     * 判断是否为核心业务资产
     */
    private boolean isCoreBusinessAsset(String assetName, Map<String, String> assetAttributes) {
        // 检查资产名称是否包含核心业务关键字
        if (assetName != null) {
            for (Pattern pattern : coreBusinessPatterns) {
                if (pattern.matcher(assetName).find()) {
                    return true;
                }
            }
        }
        
        // 检查资产属性
        if (assetAttributes != null) {
            // 检查业务重要性属性
            String importance = assetAttributes.get("businessImportance");
            if (importance != null && (importance.equalsIgnoreCase("core") || 
                                      importance.equalsIgnoreCase("critical") || 
                                      importance.equalsIgnoreCase("核心") || 
                                      importance.equalsIgnoreCase("关键"))) {
                return true;
            }
            
            // 检查业务类型属性
            String businessType = assetAttributes.get("businessType");
            if (businessType != null && (businessType.contains("核心") || 
                                         businessType.contains("关键") || 
                                         businessType.contains("Core") || 
                                         businessType.contains("Critical"))) {
                return true;
            }
            
            // 检查数据敏感性
            String dataSensitivity = assetAttributes.get("dataSensitivity");
            if (dataSensitivity != null && (dataSensitivity.equalsIgnoreCase("high") || 
                                           dataSensitivity.equalsIgnoreCase("机密") || 
                                           dataSensitivity.equalsIgnoreCase("敏感"))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断是否为业务资产
     */
    private boolean isBusinessAsset(Map<String, String> assetAttributes) {
        if (assetAttributes != null) {
            // 检查业务重要性属性
            String importance = assetAttributes.get("businessImportance");
            if (importance != null && (importance.equalsIgnoreCase("business") || 
                                      importance.equalsIgnoreCase("业务") || 
                                      importance.equalsIgnoreCase("important"))) {
                return true;
            }
            
            // 检查服务类型
            String serviceType = assetAttributes.get("serviceType");
            if (serviceType != null && (serviceType.equalsIgnoreCase("business") || 
                                       serviceType.equalsIgnoreCase("应用") || 
                                       serviceType.equalsIgnoreCase("application"))) {
                return true;
            }
            
            // 检查是否面向外部
            String external = assetAttributes.get("isExternal");
            if (external != null && external.equalsIgnoreCase("true")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断是否为支撑资产
     */
    private boolean isSupportAsset(Map<String, String> assetAttributes) {
        if (assetAttributes != null) {
            // 检查服务类型
            String serviceType = assetAttributes.get("serviceType");
            if (serviceType != null && (serviceType.equalsIgnoreCase("support") || 
                                       serviceType.equalsIgnoreCase("支撑") || 
                                       serviceType.equalsIgnoreCase("internal"))) {
                return true;
            }
            
            // 检查是否为内部系统
            String internal = assetAttributes.get("isInternal");
            if (internal != null && internal.equalsIgnoreCase("true")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取资产类型权重
     */
    private double getAssetTypeWeight(String assetType) {
        if (assetType == null) {
            return 1.0;
        }
        
        Map<String, Double> typeWeights = evaluationProperties.getWeights().getAssetTypeWeights();
        return typeWeights.getOrDefault(assetType.toUpperCase(), 1.0);
    }
    
    /**
     * 获取重要性因子
     */
    private double getImportanceFactor(String importanceLevel) {
        switch (importanceLevel) {
            case "CORE":
                return evaluationProperties.getAssetImportance().getCoreBusinessMultiplier();
            case "BUSINESS":
                return evaluationProperties.getAssetImportance().getBusinessMultiplier();
            case "SUPPORT":
                return 1.0; // 支撑资产默认权重
            case "INFRASTRUCTURE":
                return evaluationProperties.getAssetImportance().getNonBusinessMultiplier();
            default:
                return 1.0;
        }
    }
    
    /**
     * 计算属性调整分数
     */
    private double calculateAttributeAdjustment(Map<String, String> assetAttributes) {
        if (assetAttributes == null) {
            return 0.0;
        }
        
        double adjustment = 0.0;
        
        // 检查数据敏感性
        String dataSensitivity = assetAttributes.get("dataSensitivity");
        if (dataSensitivity != null) {
            switch (dataSensitivity.toLowerCase()) {
                case "critical":
                case "机密":
                    adjustment += 20.0;
                    break;
                case "high":
                case "敏感":
                    adjustment += 10.0;
                    break;
                case "medium":
                case "一般":
                    adjustment += 5.0;
                    break;
            }
        }
        
        // 检查用户影响范围
        String userScope = assetAttributes.get("userScope");
        if (userScope != null) {
            switch (userScope.toLowerCase()) {
                case "all":
                case "全部":
                case "everyone":
                    adjustment += 15.0;
                    break;
                case "many":
                case "大量":
                    adjustment += 10.0;
                    break;
                case "some":
                case "部分":
                    adjustment += 5.0;
                    break;
            }
        }
        
        // 检查业务连续性要求
        String continuityRequirement = assetAttributes.get("continuityRequirement");
        if (continuityRequirement != null) {
            switch (continuityRequirement.toLowerCase()) {
                case "high":
                case "高":
                    adjustment += 15.0;
                    break;
                case "medium":
                case "中":
                    adjustment += 8.0;
                    break;
                case "low":
                case "低":
                    adjustment += 3.0;
                    break;
            }
        }
        
        // 检查资产年龄（越新的资产可能价值越高）
        String assetAge = assetAttributes.get("assetAge");
        if (assetAge != null) {
            try {
                int ageInYears = Integer.parseInt(assetAge);
                if (ageInYears <= 1) {
                    adjustment += 5.0;
                } else if (ageInYears > 5) {
                    adjustment -= 5.0;
                }
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }
        }
        
        // 检查是否为生产环境
        String environment = assetAttributes.get("environment");
        if (environment != null && (environment.equalsIgnoreCase("production") || 
                                   environment.equalsIgnoreCase("生产"))) {
            adjustment += 10.0;
        }
        
        return adjustment;
    }
    
    /**
     * 获取资产价值等级
     * 
     * @param valueScore 价值评分
     * @return 价值等级
     */
    public String getValueLevel(double valueScore) {
        if (valueScore >= 80.0) {
            return "CRITICAL";
        } else if (valueScore >= 60.0) {
            return "HIGH";
        } else if (valueScore >= 40.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * 获取价值等级的中文表示
     * 
     * @param valueLevel 价值等级
     * @return 中文价值等级
     */
    public String getValueLevelChinese(String valueLevel) {
        switch (valueLevel) {
            case "CRITICAL":
                return "极重要";
            case "HIGH":
                return "重要";
            case "MEDIUM":
                return "一般";
            case "LOW":
                return "次要";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取价值等级对应的颜色代码
     * 
     * @param valueLevel 价值等级
     * @return 颜色代码
     */
    public String getValueLevelColor(String valueLevel) {
        switch (valueLevel) {
            case "CRITICAL":
                return "#ff4d4f";
            case "HIGH":
                return "#fa8c16";
            case "MEDIUM":
                return "#faad14";
            case "LOW":
                return "#52c41a";
            default:
                return "#1890ff";
        }
    }
    
    /**
     * 更新核心业务关键字
     * 
     * @param keywords 关键字数组
     */
    public void updateCoreBusinessKeywords(String[] keywords) {
        if (keywords != null) {
            coreBusinessPatterns = Stream.of(keywords)
                    .map(keyword -> Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE))
                    .collect(Collectors.toSet());
        }
    }
    
    /**
     * 计算资产风险价值（风险评分 * 资产价值评分）
     * 
     * @param riskScore 风险评分
     * @param valueScore 价值评分
     * @return 风险价值评分
     */
    public double calculateRiskValue(double riskScore, double valueScore) {
        return riskScore * (valueScore / 100.0);
    }
    
    /**
     * 根据风险价值获取综合等级
     * 
     * @param riskValue 风险价值
     * @return 综合等级
     */
    public String getRiskValueLevel(double riskValue) {
        if (riskValue >= 70.0) {
            return "CRITICAL";
        } else if (riskValue >= 50.0) {
            return "HIGH";
        } else if (riskValue >= 30.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}