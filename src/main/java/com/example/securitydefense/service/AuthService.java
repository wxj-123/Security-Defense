package com.example.securitydefense.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.securitydefense.dto.LoginRequest;
import com.example.securitydefense.dto.LoginResponse;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录尝试: {}", request.getUsername());
        
        // 验证验证码
        if (StringUtils.hasText(request.getCode()) && StringUtils.hasText(request.getCaptchaKey())) {
            String captchaKey = request.getCaptchaKey();
            String cachedCode = redisTemplate.opsForValue().get("captcha:" + captchaKey);
            
            // 检查验证码是否存在（null 或空字符串都视为无效）
            if (cachedCode == null || cachedCode.trim().isEmpty()) {
                throw new RuntimeException("验证码已过期，请刷新");
            }
            
            // 此时 cachedCode 已确保非 null，可以安全调用方法
            if (!cachedCode.equalsIgnoreCase(request.getCode())) {
                throw new RuntimeException("验证码错误");
            }
            
            // 验证成功后删除验证码
            String key = "captcha:" + captchaKey;
            redisTemplate.delete(key);
            log.info("验证码验证通过");
        }
        
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            log.warn("用户不存在: {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        log.info("数据库密码哈希: {}", user.getPassword());
        log.info("输入的明文密码: {}", request.getPassword());
        
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("密码验证结果: {}", passwordMatches);
        
        if (!passwordMatches) {
            log.warn("密码错误，用户: {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 生成Token (简化版，使用用户ID)
        String token = "TOKEN_" + user.getId() + "_" + System.currentTimeMillis();
        
        // 构造用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatar()
        );
        
        log.info("用户登录成功: {}", request.getUsername());
        
        return new LoginResponse(token, userInfo, false, null);
    }
    
    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }
}
