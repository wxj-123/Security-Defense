package com.example.securitydefense.situation.detector;

import com.example.securitydefense.situation.model.ThreatResult;
import java.util.List;
import java.util.Map;

/**
 * 威胁检测器接口
 * 定义威胁检测的通用方法
 */
public interface ThreatDetector {
    
    /**
     * 初始化检测器
     */
    void initialize();
    
    /**
     * 检测威胁
     * @param data 待检测的数据
     * @return 检测结果列表
     */
    List<ThreatResult> detect(Object data);
    
    /**
     * 批量检测威胁
     * @param dataList 待检测的数据列表
     * @return 检测结果列表
     */
    List<ThreatResult> detectBatch(List<Object> dataList);
    
    /**
     * 更新检测规则或模型
     * @param updateData 更新数据
     */
    void updateDetectionRules(Map<String, Object> updateData);
    
    /**
     * 获取检测器状态
     * @return 检测器状态信息
     */
    Map<String, Object> getStatus();
    
    /**
     * 获取检测器名称
     * @return 检测器名称
     */
    String getDetectorName();
    
    /**
     * 关闭检测器并释放资源
     */
    void shutdown();
    
    /**
     * 检测器是否可用
     * @return 是否可用
     */
    boolean isAvailable();
}