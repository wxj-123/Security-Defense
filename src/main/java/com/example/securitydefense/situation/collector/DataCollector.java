package com.example.securitydefense.situation.collector;

import java.util.List;
import java.util.Map;

/**
 * 数据采集器接口
 * 定义通用的数据采集方法
 */
public interface DataCollector<T> {
    
    /**
     * 初始化采集器
     */
    void initialize();
    
    /**
     * 采集数据
     * @return 采集的数据列表
     */
    List<T> collect();
    
    /**
     * 批量采集数据
     * @param batchSize 批次大小
     * @return 采集的数据列表
     */
    List<T> collectBatch(int batchSize);
    
    /**
     * 关闭采集器
     */
    void shutdown();
    
    /**
     * 获取采集器状态
     * @return 采集器状态信息
     */
    Map<String, Object> getStatus();
    
    /**
     * 获取采集器名称
     * @return 采集器名称
     */
    String getCollectorName();
    
    /**
     * 数据是否可用
     * @return true表示数据可用，false表示不可用
     */
    boolean isAvailable();
}