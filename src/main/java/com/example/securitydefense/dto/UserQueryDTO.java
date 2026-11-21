package com.example.securitydefense.dto;

import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO {
    
    private String username;
    
    private String phone;
    
    private String department;
    
    private Integer status;
    
    private Integer current = 1;
    
    private Integer size = 10;
}