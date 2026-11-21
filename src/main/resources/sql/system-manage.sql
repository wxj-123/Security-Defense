-- 系统管理模块数据库表结构

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_name (role_name),
    INDEX idx_role_code (role_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) COMMENT '权限编码',
    resource_type VARCHAR(20) COMMENT '资源类型 menu-菜单 button-按钮 api-接口',
    resource_url VARCHAR(255) COMMENT '资源路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    sort INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(100) COMMENT '图标',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_name (permission_name),
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '操作地点',
    status TINYINT DEFAULT 1 COMMENT '操作状态 0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时间(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 插入操作日志模拟数据
INSERT IGNORE INTO sys_operation_log (username, operation, ip, status, error_msg, create_time) VALUES
('admin', '查询用户列表', '192.168.1.100', 1, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('admin', '新增角色: 安全审计员', '192.168.1.100', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('admin', '修改系统名称为网络安全防御系统', '192.168.1.100', 1, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('operator', '创建系统备份', '192.168.1.101', 1, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('auditor', '查询操作日志', '192.168.1.102', 1, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('admin', '修改用户状态', '192.168.1.100', 1, NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('admin', '为安全审计员分配权限', '192.168.1.100', 1, NULL, DATE_SUB(NOW(), INTERVAL 7 HOUR)),
('operator', '恢复系统备份', '192.168.1.101', 0, '备份文件不存在', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
('admin', '查询系统配置', '192.168.1.100', 1, NULL, DATE_SUB(NOW(), INTERVAL 9 HOUR)),
('auditor', '清理30天前的操作日志', '192.168.1.102', 1, NULL, DATE_SUB(NOW(), INTERVAL 10 HOUR));

-- 数据备份表
CREATE TABLE IF NOT EXISTS sys_backup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '备份ID',
    backup_name VARCHAR(100) NOT NULL COMMENT '备份名称',
    backup_path VARCHAR(255) NOT NULL COMMENT '备份路径',
    backup_type VARCHAR(20) NOT NULL COMMENT '备份类型',
    backup_size BIGINT DEFAULT 0 COMMENT '备份大小(字节)',
    backup_status VARCHAR(20) NOT NULL COMMENT '备份状态',
    backup_user VARCHAR(50) NOT NULL COMMENT '备份用户',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_backup_name (backup_name),
    INDEX idx_backup_type (backup_type),
    INDEX idx_backup_status (backup_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据备份表';

-- 更新用户表，添加真实姓名和部门字段（修复语法错误）
-- 使用存储过程来检查列是否存在，避免重复添加
DELIMITER //
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDefinition TEXT
)
BEGIN
    DECLARE columnExists INT;

    -- 检查列是否存在
    SELECT COUNT(*)
    INTO columnExists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = tableName
    AND COLUMN_NAME = columnName;

    -- 如果列不存在，则添加
    IF columnExists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- 添加真实姓名和部门字段
CALL AddColumnIfNotExists('sys_user', 'real_name', 'VARCHAR(50) COMMENT ''真实姓名''');
CALL AddColumnIfNotExists('sys_user', 'department', 'VARCHAR(100) COMMENT ''部门''');

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

-- 插入系统默认角色
INSERT IGNORE INTO sys_role (id, role_name, role_code, description, status) VALUES 
(1, '系统管理员', 'admin', '系统管理员，拥有所有权限', 1),
(2, '安全管理员', 'security', '安全管理员，负责安全策略和事件管理', 1),
(3, '审计员', 'auditor', '审计员，只能查看日志和报表', 1),
(4, '普通用户', 'user', '普通用户，只有查看权限', 1);

-- 插入系统默认权限
INSERT IGNORE INTO sys_permission (id, permission_name, permission_code, resource_type, resource_url, parent_id, sort, icon, status) VALUES
-- 系统管理模块
(100, '系统管理', '', 'menu', '/system-manage', 0, 1, 'tools', 1),

-- 用户管理
(101, '用户管理', 'system:user', 'menu', '/system-manage/user', 100, 1, 'user', 1),
(102, '用户查看', 'system:user:view', 'button', '', 101, 1, '', 1),
(103, '用户新增', 'system:user:create', 'button', '', 101, 2, '', 1),
(104, '用户编辑', 'system:user:update', 'button', '', 101, 3, '', 1),
(105, '用户删除', 'system:user:delete', 'button', '', 101, 4, '', 1),
(106, '用户分配角色', 'system:user:assign-role', 'button', '', 101, 5, '', 1),
(107, '用户重置密码', 'system:user:reset-password', 'button', '', 101, 6, '', 1),
(108, '用户状态变更', 'system:user:status', 'button', '', 101, 7, '', 1),

-- 角色管理
(110, '角色管理', 'system:role', 'menu', '/system-manage/role', 100, 2, 'tools', 1),
(111, '角色查看', 'system:role:view', 'button', '', 110, 1, '', 1),
(112, '角色新增', 'system:role:create', 'button', '', 110, 2, '', 1),
(113, '角色编辑', 'system:role:update', 'button', '', 110, 3, '', 1),
(114, '角色删除', 'system:role:delete', 'button', '', 110, 4, '', 1),
(115, '角色分配权限', 'system:role:assign-permission', 'button', '', 110, 5, '', 1),

-- 权限管理
(120, '权限管理', 'system:permission', 'menu', '/system-manage/permission', 100, 3, 'key', 1),
(121, '权限查看', 'system:permission:view', 'button', '', 120, 1, '', 1),

-- 系统配置
(130, '系统配置', 'system:config', 'menu', '/system-manage/config', 100, 4, 'setting', 1),
(131, '配置查看', 'system:config:view', 'button', '', 130, 1, '', 1),
(132, '配置更新', 'system:config:update', 'button', '', 130, 2, '', 1),
(133, '连接测试', 'system:connection:test', 'button', '', 130, 3, '', 1),
(134, '系统信息', 'system:info:view', 'button', '', 130, 4, '', 1),

-- 操作日志
(140, '操作日志', 'system:log', 'menu', '/system-manage/log', 100, 5, 'files', 1),
(141, '日志查看', 'system:log:view', 'button', '', 140, 1, '', 1),
(142, '日志清理', 'system:log:clean', 'button', '', 140, 2, '', 1),

-- 数据备份
(150, '数据备份', 'system:backup', 'menu', '/system-manage/backup', 100, 6, 'database', 1),
(151, '备份查看', 'system:backup:view', 'button', '', 150, 1, '', 1),
(152, '备份创建', 'system:backup:create', 'button', '', 150, 2, '', 1),
(153, '备份恢复', 'system:backup:restore', 'button', '', 150, 3, '', 1),
(154, '备份删除', 'system:backup:delete', 'button', '', 150, 4, '', 1),
(155, '备份清理', 'system:backup:clean', 'button', '', 150, 5, '', 1);

-- 为系统管理员角色分配所有权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0 AND status = 1;

-- 插入系统默认配置
INSERT IGNORE INTO sys_config (config_key, config_value, config_name, config_type, description) VALUES
-- 基础配置
('system.name', '网络安全防御系统', '系统名称', 'basic', '系统名称'),
('system.logo', '/images/logo.png', '系统Logo', 'basic', '系统Logo地址'),
('session.timeout', '30', '会话超时时间', 'basic', '会话超时时间(分钟)'),
('password.min.length', '6', '密码最小长度', 'basic', '密码最小长度'),
('password.complexity', 'medium', '密码复杂度', 'basic', '密码复杂度(simple/medium/complex)'),
('notification.methods', 'email,system', '告警通知方式', 'basic', '告警通知方式'),

-- 日志配置
('log.retention.days', '180', '日志保留天数', 'log', '操作日志保留天数'),
('log.enable.operation', 'true', '操作日志启用', 'log', '是否启用操作日志'),
('log.enable.security', 'true', '安全日志启用', 'log', '是否启用安全日志'),

-- 备份配置
('backup.auto.enable', 'false', '自动备份启用', 'backup', '是否启用自动备份'),
('backup.cycle', 'weekly', '备份周期', 'backup', '备份周期(daily/weekly/monthly)'),
('backup.retention.days', '30', '备份保留天数', 'backup', '备份文件保留天数'),
('backup.path', '/data/backups', '备份存储路径', 'backup', '备份文件存储路径');