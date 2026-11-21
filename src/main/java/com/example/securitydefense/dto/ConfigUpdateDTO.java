package com.example.securitydefense.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 配置更新DTO
 */
@Data
public class ConfigUpdateDTO {
    
    private Long id;
    
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    @NotBlank(message = "配置名称不能为空")
    private String configName;
    
    private String configType;
    
    private String description;
}