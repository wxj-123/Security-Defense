package com.example.securitydefense.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        log.info("=== 开始初始化数据 ===");
        
        try {
            // 检查admin用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, "admin");
            User admin = userMapper.selectOne(wrapper);
            
            if (admin != null) {
                // 更新密码为正确的哈希值
                String newPassword = passwordEncoder.encode("admin123");
                admin.setPassword(newPassword);
                userMapper.updateById(admin);
                log.info("✓ admin用户密码已更新");
                log.info("新密码哈希: {}", newPassword);
                
                // 测试密码验证
                boolean matches = passwordEncoder.matches("admin123", newPassword);
                log.info("密码验证测试: {}", matches ? "成功" : "失败");
            } else {
                log.warn("admin用户不存在，请先执行数据库初始化脚本");
            }
        } catch (Exception e) {
            log.error("数据初始化过程中出现错误: {}", e.getMessage());
            log.warn("请确保数据库表已正确创建");
        }
        
        log.info("=== 数据初始化完成 ===");
    }
}