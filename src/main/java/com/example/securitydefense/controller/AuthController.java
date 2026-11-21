package com.example.securitydefense.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.securitydefense.common.Result;
import com.example.securitydefense.dto.LoginRequest;
import com.example.securitydefense.dto.LoginResponse;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.mapper.UserMapper;
import com.example.securitydefense.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            log.info("收到登录请求: {}", request.getUsername());
            LoginResponse response = authService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    public Result<User> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从token中获取用户ID
            Long userId = getCurrentUserId(authorization);

            // 从数据库查询用户信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                // 如果数据库中没有用户,返回Mock数据
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setUsername("admin");
                mockUser.setEmail("admin@security.com");
                mockUser.setAvatar("/uploads/avatars/default.png");
                return Result.success(mockUser);
            }

            // 查询用户角色
            List<String> roles = userMapper.selectUserRoles(userId);
            user.setRoles(roles);

            // 查询用户权限
            List<String> permissions = userMapper.selectUserPermissions(userId);
            user.setPermissions(permissions);

            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从token中获取当前用户ID
     */
    private Long getCurrentUserId(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            // 解析 mock_token_username_timestamp 格式的token
            if (token.startsWith("mock_token_")) {
                String[] parts = token.split("_");
                if (parts.length >= 3) {
                    String username = parts[2];
                    // 根据用户名查询用户ID
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("username", username);
                    User user = userMapper.selectOne(queryWrapper);
                    if (user != null) {
                        return user.getId();
                    }
                }
            }
        }
        // 默认返回admin用户ID
        return 1L;
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response) throws IOException {
        // 创建线性验证码，宽120，高40，4位验证码，20条干扰线
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);

        // 生成唯一标识
        String uuid = UUID.randomUUID().toString();

        // 将验证码存入Redis，5分钟过期
        redisTemplate.opsForValue().set("captcha:" + uuid, captcha.getCode(), 5, TimeUnit.MINUTES);

        // 将uuid返回给前端（通过cookie）
        response.setHeader("Captcha-Key", uuid);

        // 设置响应头
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 输出图片
        captcha.write(response.getOutputStream());
    }
}
