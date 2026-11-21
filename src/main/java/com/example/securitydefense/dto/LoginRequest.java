package com.example.securitydefense.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    private String username;
    
    private String password;
    
    private String code;
    
    private String captchaKey;
    
    private Boolean remember;
}
