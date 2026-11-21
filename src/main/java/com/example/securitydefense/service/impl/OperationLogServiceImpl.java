package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.dto.OperationLogQueryDTO;
import com.example.securitydefense.entity.SysOperationLog;
import com.example.securitydefense.mapper.SysOperationLogMapper;
import com.example.securitydefense.service.OperationLogService;
import com.example.securitydefense.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 操作日志服务实现类
 */
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements OperationLogService {
    
    @Autowired
    private SysOperationLogMapper operationLogMapper;
    
    @Autowired
    private SystemService systemService;
    
    @Override
    @Async
    public void logOperation(String operator, String operationModule, String operationType, 
                            String operationContent, String operationResult, String errorMsg) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            
            logOperation(operator, operationModule, operationType, operationContent, operationResult, errorMsg, request);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
    
    @Override
    @Async
    public void logOperation(String operator, String operationModule, String operationType, 
                            String operationContent, String operationResult, String errorMsg,
                            HttpServletRequest request) {
        try {
            // 获取操作IP
            String operationIp = getClientIp(request);
            
            // 创建日志对象
            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setOperationUser(operator);
            operationLog.setOperationModule(operationModule);
            operationLog.setOperationType(operationType);
            operationLog.setOperation(operationContent);  // 使用操作内容作为操作类型
            operationLog.setOperationContent(operationContent);
            operationLog.setOperationIp(operationIp);
            operationLog.setOperationResult(operationResult);
            operationLog.setErrorMsg(errorMsg);
            operationLog.setCreateTime(LocalDateTime.now());
            
            // 保存日志
            save(operationLog);
            
            // 异步保存到Redis缓存（7天过期）
            saveLogToCache(operationLog);
            
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
    
    @Override
    public IPage<SysOperationLog> queryOperationLogPage(OperationLogQueryDTO queryDTO) {
        Page<SysOperationLog> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        return operationLogMapper.selectOperationLogPage(page, queryDTO);
    }
    
    @Override
    @Async
    public void cleanExpiredLogs(int retentionDays) {
        try {
            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().minus(retentionDays, ChronoUnit.DAYS);
            
            // 删除过期日志
            QueryWrapper<SysOperationLog> wrapper = new QueryWrapper<>();
            wrapper.lt("create_time", expireTime);
            int deletedCount = operationLogMapper.delete(wrapper);
            
            log.info("清理过期操作日志完成，删除了 {} 条记录", deletedCount);
            
            // 记录清理操作
            logOperation(
                "系统",
                "操作日志",
                "清理",
                String.format("清理了 %d 天前的操作日志，删除了 %d 条记录", retentionDays, deletedCount),
                "成功",
                null
            );
            
        } catch (Exception e) {
            log.error("清理过期操作日志失败", e);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "未知";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多个IP，取第一个
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 保存日志到Redis缓存
     */
    private void saveLogToCache(SysOperationLog operationLog) {
        try {
            // 获取日志保留周期配置，默认180天
            String retentionDaysConfig = systemService.getConfigValue("log.retention.days");
            int retentionDays = StringUtils.hasText(retentionDaysConfig) ? 
                Integer.parseInt(retentionDaysConfig) : 180;
            
            // 转换为秒
            long expireTime = retentionDays * 24 * 60 * 60L;
            
            // 存储到Redis（这里简化处理，实际应用中可以根据需求调整存储策略）
            // 可以按日期分键，例如：operation_log:2023-10-01
            String dateKey = "operation_log:" + operationLog.getCreateTime().toLocalDate();
            
            // 这里只是示例，实际应用中可能需要更复杂的存储策略
            // 如使用Redis的Hash结构存储，或者使用消息队列异步处理
            
        } catch (Exception e) {
            log.error("保存操作日志到缓存失败", e);
        }
    }
}