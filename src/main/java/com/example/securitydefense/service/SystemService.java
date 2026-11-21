package com.example.securitydefense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.securitydefense.entity.SysConfig;
import com.example.securitydefense.dto.ConfigUpdateDTO;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemService extends IService<SysConfig> {
    
    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置类型获取配置列表
     */
    List<SysConfig> getConfigByType(String configType);
    
    /**
     * 更新配置
     */
    boolean updateConfig(ConfigUpdateDTO configUpdateDTO, String operator);
    
    /**
     * 测试数据库连接
     */
    boolean testDatabaseConnection();
    
    /**
     * 测试Redis连接
     */
    boolean testRedisConnection();
}