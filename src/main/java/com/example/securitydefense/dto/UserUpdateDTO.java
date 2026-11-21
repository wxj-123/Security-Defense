package com.example.securitydefense.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户更新DTO
 */
@Data
public class UserUpdateDTO {
    
    private Long id;
    
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String department;
    
    private Integer status;
    
    private List<Long> roleIds;
}