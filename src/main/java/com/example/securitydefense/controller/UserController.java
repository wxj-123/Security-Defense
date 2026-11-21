package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.securitydefense.common.ApiResponse;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private static final String UPLOAD_DIR = "uploads" + File.separator + "avatars" + File.separator;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    private UserMapper userMapper;

    /**
     * 从Token中获取用户ID
     * 简化版: 从 mock_token_{username}_{timestamp} 中解析用户名
     */
    private Long getCurrentUserId(String token) {
        if (token == null || !token.startsWith("mock_token_")) {
            return 1L; // 默认返回admin用户ID
        }

        try {
            // 解析格式: mock_token_{username}_{timestamp}
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1L; // 默认返回admin用户ID
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<Map<String, Object>> uploadAvatar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ApiResponse.error(400, "文件不能为空");
            }

            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return ApiResponse.error(400, "文件大小不能超过2MB");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return ApiResponse.error(400, "只支持JPG和PNG格式的图片");
            }

            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    return ApiResponse.error(500, "无法创建上传目录");
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            // 确保扩展名是允许的类型
            if (!extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png")) {
                return ApiResponse.error(400, "只支持JPG和PNG格式的图片");
            }

            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            // 保存文件
            Files.write(filePath, file.getBytes());

            // 返回文件访问URL
            String fileUrl = "/uploads/avatars/" + fileName;

            // 从Token中获取用户ID并保存到数据库
            String token = (authorization != null && authorization.startsWith("Bearer "))
                ? authorization.substring(7)
                : null;
            Long userId = getCurrentUserId(token);

            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", userId).set("avatar", fileUrl);
            userMapper.update(null, updateWrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("fileName", fileName);

            return ApiResponse.success(data);

        } catch (IOException e) {
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "上传过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public ApiResponse<Map<String, Object>> updateUserInfo(
            @RequestBody Map<String, Object> userInfo) {
        try {
            // TODO: 实际业务逻辑应该从数据库更新用户信息
            // 这里仅作为示例返回成功
            return ApiResponse.success(userInfo);

        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResponse<Boolean> changePassword(
            @RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            // TODO: 实际业务逻辑
            // 1. 验证旧密码是否正确
            // 2. 加密新密码
            // 3. 更新数据库

            // 这里仅作为示例
            if (oldPassword == null || oldPassword.isEmpty()) {
                return ApiResponse.error(400, "原密码不能为空");
            }

            if (newPassword == null || newPassword.length() < 6) {
                return ApiResponse.error(400, "新密码长度不能少于6位");
            }

            return ApiResponse.success(true);

        } catch (Exception e) {
            return ApiResponse.error(500, "密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/detail")
    public ApiResponse<Map<String, Object>> getUserDetail() {
        try {
            // TODO: 从数据库获取用户详细信息
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("id", 1);
            userDetail.put("username", "admin");
            userDetail.put("email", "admin@security.com");
            userDetail.put("avatar", "/uploads/avatars/default.png");
            userDetail.put("roles", new String[]{"admin"});
            userDetail.put("permissions", new String[]{"*"});

            return ApiResponse.success(userDetail);

        } catch (Exception e) {
            return ApiResponse.error(500, "获取失败: " + e.getMessage());
        }
    }
}