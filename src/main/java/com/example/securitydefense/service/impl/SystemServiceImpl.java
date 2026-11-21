package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.dto.ConfigUpdateDTO;
import com.example.securitydefense.entity.SysConfig;
import com.example.securitydefense.mapper.SysConfigMapper;
import com.example.securitydefense.service.OperationLogService;
import com.example.securitydefense.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
public class SystemServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SystemService {
    
    @Autowired
    private SysConfigMapper configMapper;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    @Lazy
    private OperationLogService operationLogService;
    
    @Override
    public String getConfigValue(String configKey) {
        // 先从缓存获取
        String value = (String) redisTemplate.opsForValue().get("config:" + configKey);
        if (!StringUtils.isEmpty(value)) {
            return value;
        }
        
        // 缓存不存在，从数据库获取
        SysConfig config = configMapper.selectByConfigKey(configKey);
        if (config != null) {
            // 存入缓存，过期时间1小时
            redisTemplate.opsForValue().set("config:" + configKey, config.getConfigValue(), 1, TimeUnit.HOURS);
            return config.getConfigValue();
        }
        
        return null;
    }
    
    @Override
    public List<SysConfig> getConfigByType(String configType) {
        return configMapper.selectByConfigType(configType);
    }
    
    @Override
    @Transactional
    public boolean updateConfig(ConfigUpdateDTO configUpdateDTO, String operator) {
        SysConfig config;
        String oldValue = null;
        boolean isCreate = false;
        
        // 如果ID为null，则创建新配置项
        if (configUpdateDTO.getId() == null) {
            config = new SysConfig();
            config.setCreateTime(LocalDateTime.now());
            isCreate = true;
        } else {
            config = getById(configUpdateDTO.getId());
            if (config == null) {
                return false;
            }
            oldValue = config.getConfigValue();
        }
        
        // 更新或设置配置
        config.setConfigKey(configUpdateDTO.getConfigKey());
        config.setConfigValue(configUpdateDTO.getConfigValue());
        config.setConfigName(configUpdateDTO.getConfigName());
        config.setConfigType(configUpdateDTO.getConfigType());
        config.setDescription(configUpdateDTO.getDescription());
        config.setUpdateTime(LocalDateTime.now());
        
        boolean result = isCreate ? save(config) : updateById(config);
        
        if (result) {
            // 清除缓存
            redisTemplate.delete("config:" + config.getConfigKey());
            
            // 记录操作日志
            String action = isCreate ? "创建" : "更新";
            String message = isCreate ? 
                String.format("创建配置项 [%s]，值为 [%s]", config.getConfigName(), config.getConfigValue()) :
                String.format("更新配置项 [%s]，值从 [%s] 变更为 [%s]", 
                    config.getConfigName(), oldValue, config.getConfigValue());
            
            operationLogService.logOperation(
                operator,
                "系统配置",
                action,
                message,
                "成功",
                null
            );
        }
        
        return result;
    }
    
    @Override
    public boolean testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(3); // 3秒超时
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return false;
        }
    }
    
    @Override
    public boolean testRedisConnection() {
        try {
            // 测试Redis连接，使用ping命令
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equals(pong);
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return false;
        }
    }
}