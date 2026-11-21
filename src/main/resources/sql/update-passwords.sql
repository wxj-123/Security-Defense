USE security_defense;

-- 更新所有用户密码为 admin123 的正确BCrypt哈希值
-- 这个哈希值是通过 BCryptPasswordEncoder 使用强度10生成的
UPDATE sys_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username IN ('admin', 'operator', 'viewer');

-- 验证更新
SELECT id, username, email, status FROM sys_user;
