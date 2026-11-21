-- H2数据库不需要USE语句

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    two_factor_enabled TINYINT DEFAULT 0 COMMENT '是否启用双因素认证',
    two_factor_secret VARCHAR(255) COMMENT '双因素认证密钥',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

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

-- 安全事件表
CREATE TABLE IF NOT EXISTS security_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_level VARCHAR(20) NOT NULL COMMENT '事件级别 INFO/WARNING/ERROR/CRITICAL',
    source_ip VARCHAR(50) COMMENT '源IP',
    target_ip VARCHAR(50) COMMENT '目标IP',
    source_port INT COMMENT '源端口',
    target_port INT COMMENT '目标端口',
    protocol VARCHAR(20) COMMENT '协议',
    description TEXT COMMENT '事件描述',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '处理状态 PENDING/PROCESSING/RESOLVED/IGNORED',
    handled_by BIGINT COMMENT '处理人ID',
    handled_time DATETIME COMMENT '处理时间',
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_event_type (event_type),
    INDEX idx_event_level (event_level),
    INDEX idx_status (status),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全事件表';

-- 防火墙规则表
CREATE TABLE IF NOT EXISTS firewall_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型 ALLOW/DENY',
    source_ip VARCHAR(100) COMMENT '源IP',
    target_ip VARCHAR(100) COMMENT '目标IP',
    source_port VARCHAR(50) COMMENT '源端口',
    target_port VARCHAR(50) COMMENT '目标端口',
    protocol VARCHAR(20) COMMENT '协议 TCP/UDP/ICMP/ALL',
    direction VARCHAR(20) COMMENT '方向 IN/OUT/BOTH',
    priority INT DEFAULT 100 COMMENT '优先级 数字越小优先级越高',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    description TEXT COMMENT '规则描述',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_priority (priority),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='防火墙规则表';

-- 访问控制列表表
CREATE TABLE IF NOT EXISTS access_control_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ACL ID',
    list_name VARCHAR(100) NOT NULL COMMENT '列表名称',
    list_type VARCHAR(20) NOT NULL COMMENT '列表类型 WHITELIST/BLACKLIST',
    ip_address VARCHAR(50) NOT NULL COMMENT 'IP地址',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_list_type (list_type),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问控制列表表';

-- 网络流量日志表
CREATE TABLE IF NOT EXISTS network_flow_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    flow_id VARCHAR(100) COMMENT '流量ID',
    source_ip VARCHAR(50) NOT NULL COMMENT '源IP',
    target_ip VARCHAR(50) NOT NULL COMMENT '目标IP',
    source_port INT COMMENT '源端口',
    target_port INT COMMENT '目标端口',
    protocol VARCHAR(20) COMMENT '协议',
    bytes_sent BIGINT DEFAULT 0 COMMENT '发送字节数',
    bytes_received BIGINT DEFAULT 0 COMMENT '接收字节数',
    packets_sent INT DEFAULT 0 COMMENT '发送包数',
    packets_received INT DEFAULT 0 COMMENT '接收包数',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '持续时间(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_source_ip (source_ip),
    INDEX idx_target_ip (target_ip),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网络流量日志表';

-- 系统操作日志表
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
    -- 新增的操作日志字段
    operation_user VARCHAR(50) COMMENT '操作用户',
    operation_module VARCHAR(100) COMMENT '操作模块',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_content TEXT COMMENT '操作内容',
    operation_ip VARCHAR(50) COMMENT '操作IP',
    operation_result VARCHAR(20) COMMENT '操作结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 威胁情报表
CREATE TABLE IF NOT EXISTS threat_intelligence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '情报ID',
    threat_type VARCHAR(50) NOT NULL COMMENT '威胁类型',
    threat_level VARCHAR(20) NOT NULL COMMENT '威胁级别 LOW/MEDIUM/HIGH/CRITICAL',
    threat_source VARCHAR(100) COMMENT '威胁来源',
    indicator_type VARCHAR(50) COMMENT '指标类型 IP/DOMAIN/URL/HASH',
    indicator_value VARCHAR(500) NOT NULL COMMENT '指标值',
    description TEXT COMMENT '描述',
    first_seen DATETIME COMMENT '首次发现时间',
    last_seen DATETIME COMMENT '最后发现时间',
    confidence_score INT DEFAULT 0 COMMENT '置信度分数 0-100',
    status TINYINT DEFAULT 1 COMMENT '状态 0-失效 1-有效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_threat_type (threat_type),
    INDEX idx_indicator_type (indicator_type),
    INDEX idx_indicator_value (indicator_value(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='威胁情报表';

-- 策略模板表
CREATE TABLE IF NOT EXISTS policy_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型',
    template_content TEXT NOT NULL COMMENT '模板内容(JSON)',
    description TEXT COMMENT '描述',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模板',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_type (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略模板表';

-- 审批工作流表
CREATE TABLE IF NOT EXISTS approval_workflow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工作流ID',
    workflow_name VARCHAR(100) NOT NULL COMMENT '工作流名称',
    workflow_type VARCHAR(50) NOT NULL COMMENT '工作流类型',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    target_id BIGINT COMMENT '关联目标ID',
    target_type VARCHAR(50) COMMENT '目标类型',
    current_step INT DEFAULT 1 COMMENT '当前步骤',
    total_steps INT DEFAULT 1 COMMENT '总步骤数',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED/CANCELLED',
    approval_content TEXT COMMENT '审批内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_initiator_id (initiator_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批工作流表';

-- 审批步骤记录表
CREATE TABLE IF NOT EXISTS approval_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '步骤ID',
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    step_number INT NOT NULL COMMENT '步骤编号',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态 PENDING/APPROVED/REJECTED',
    approval_comment TEXT COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批步骤记录表';
