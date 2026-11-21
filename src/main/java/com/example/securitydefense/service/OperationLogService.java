package com.example.securitydefense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.securitydefense.entity.SysOperationLog;
import com.example.securitydefense.dto.OperationLogQueryDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<SysOperationLog> {
    
    /**
     * 记录操作日志
     */
    void logOperation(String operator, String operationModule, String operationType, 
                      String operationContent, String operationResult, String errorMsg);
    
    /**
     * 记录操作日志（带请求对象）
     */
    void logOperation(String operator, String operationModule, String operationType, 
                      String operationContent, String operationResult, String errorMsg,
                      HttpServletRequest request);
    
    /**
     * 分页查询操作日志
     */
    IPage<SysOperationLog> queryOperationLogPage(OperationLogQueryDTO queryDTO);
    
    /**
     * 清理过期日志
     */
    void cleanExpiredLogs(int retentionDays);
}