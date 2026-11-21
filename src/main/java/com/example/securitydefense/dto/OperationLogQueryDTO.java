package com.example.securitydefense.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志查询DTO
 */
@Data
public class OperationLogQueryDTO {
    
    private String operationUser;
    
    private String operationModule;
    
    private String operationType;
    
    private String operationResult;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer current = 1;
    
    private Integer size = 10;
}