package com.example.securitydefense.situation.evaluation;

import com.example.securitydefense.situation.model.AssetRisk;
import com.example.securitydefense.situation.model.SituationData;

import java.util.List;
import java.util.Map;

/**
 * 态势评估引擎接口
 * 定义安全态势评估的核心功能
 */
public interface SituationEvaluator {
    
    /**
     * 初始化评估引擎
     */
    void initialize();
    
    /**
     * 计算整体安全态势指数
     * 
     * @return 态势数据对象，包含态势指数、威胁等级等信息
     */
    SituationData calculateOverallSituation();
    
    /**
     * 评估单个资产的风险
     * 
     * @param assetId 资产ID
     * @return 资产风险评估结果
     */
    AssetRisk evaluateAssetRisk(String assetId);
    
    /**
     * 批量评估多个资产的风险
     * 
     * @param assetIds 资产ID列表
     * @return 资产ID到风险评估结果的映射
     */
    Map<String, AssetRisk> evaluateAssetRisks(List<String> assetIds);
    
    /**
     * 获取所有资产的风险评估结果
     * 
     * @return 所有资产的风险评估结果列表
     */
    List<AssetRisk> getAllAssetRisks();
    
    /**
     * 更新评估配置
     * 
     * @param config 配置参数映射
     */
    void updateEvaluationConfig(Map<String, Object> config);
    
    /**
     * 获取评估引擎状态
     * 
     * @return 状态信息映射
     */
    Map<String, Object> getStatus();
    
    /**
     * 获取评估引擎名称
     * 
     * @return 引擎名称
     */
    String getEvaluatorName();
    
    /**
     * 关闭评估引擎
     */
    void shutdown();
    
    /**
     * 检查评估引擎是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
}