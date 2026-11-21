USE security_defense;

-- 插入默认用户 (密码使用BCrypt加密，原始密码为: admin123)
-- BCrypt强度10加密: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
INSERT IGNORE INTO sys_user (username, password, email, phone, avatar, status, two_factor_enabled) VALUES
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@security.com', '13800138000', '/avatar/admin.png', 1, 0),
('operator', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'operator@security.com', '13800138001', '/avatar/operator.png', 1, 0),
('viewer', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'viewer@security.com', '13800138002', '/avatar/viewer.png', 1, 0);

-- 插入默认角色
INSERT IGNORE INTO sys_role (role_name, role_code, description, status) VALUES
('系统管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1),
('安全运维', 'ROLE_OPERATOR', '负责安全策略配置和事件处理', 1),
('安全审计', 'ROLE_AUDITOR', '负责安全审计和报表查看', 1),
('访客', 'ROLE_VIEWER', '只能查看基本信息', 1);

-- 分配用户角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> 系统管理员
(2, 2),  -- operator -> 安全运维
(3, 4);  -- viewer -> 访客

-- 插入示例安全事件（包含详细的payload描述）
INSERT INTO security_event (event_type, event_level, source_ip, target_ip, source_port, target_port, protocol, description, status, event_time) VALUES
('PORT_SCAN', 'WARNING', '192.168.1.100', '10.0.0.10', 54321, 22, 'TCP', '检测到端口扫描行为，已记录详细日志。\n扫描端口范围: 1-65535\n扫描方式: SYN扫描\n检测到的开放端口: [22, 80, 443, 3306, 3389]', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('BRUTE_FORCE', 'CRITICAL', '203.0.113.45', '10.0.0.20', 45678, 22, 'TCP', 'SSH暴力破解尝试，已触发告警。\n尝试的账户名: [admin, root, user, test]\n失败次数: 15次\n攻击特征: 字典攻击 + 暴力破解', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('DDOS', 'CRITICAL', '198.51.100.0', '10.0.0.30', 12345, 80, 'TCP', '检测到DDoS攻击流量，已启动防护机制。\n攻击类型: SYN Flood\n峰值流量: 12.5 Gbps\n源IP数量: 1524个\n攻击持续时间: 3600秒', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('MALWARE', 'HIGH', '192.168.1.150', '10.0.0.40', 56789, 443, 'HTTPS', '发现恶意软件通信，已隔离相关流量。\n恶意软件名称: TrickBot\nC2服务器: 192.168.100.45\n通信协议: HTTPS/TCP\n检测特征: 域名生成算法(DGA)', 'PENDING', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
('SQL_INJECTION', 'HIGH', '203.0.113.100', '10.0.0.50', 34567, 3306, 'TCP', '检测到SQL注入尝试，已拦截请求。\n注入点: /api/user/login\n攻击Payload: SELECT * FROM users WHERE id = ''1'' OR ''1''=''1''\n注入类型: Union-based SQL Injection\nWAF拦截: 已拦截并记录', 'RESOLVED', DATE_SUB(NOW(), INTERVAL 5 MINUTE));

-- 插入示例防火墙规则
INSERT INTO firewall_rule (rule_name, rule_type, source_ip, target_ip, source_port, target_port, protocol, direction, priority, status, description, create_by) VALUES
('允许HTTP访问', 'ALLOW', '*', '10.0.0.0/24', '*', '80', 'TCP', 'IN', 10, 1, '允许所有HTTP流量', 1),
('允许HTTPS访问', 'ALLOW', '*', '10.0.0.0/24', '*', '443', 'TCP', 'IN', 10, 1, '允许所有HTTPS流量', 1),
('阻止恶意IP', 'DENY', '203.0.113.45', '*', '*', '*', 'ALL', 'BOTH', 5, 1, '阻止已知恶意IP', 1),
('允许SSH管理', 'ALLOW', '192.168.1.0/24', '10.0.0.0/24', '*', '22', 'TCP', 'IN', 20, 1, '仅允许内网SSH访问', 1),
('阻止出站Telnet', 'DENY', '*', '*', '*', '23', 'TCP', 'OUT', 15, 1, '禁止Telnet协议', 1);

-- 插入访问控制列表
INSERT IGNORE INTO access_control_list (list_name, list_type, ip_address, description, status, create_by) VALUES
('可信IP白名单', 'WHITELIST', '192.168.1.1', '公司办公网关', 1, 1),
('可信IP白名单', 'WHITELIST', '192.168.1.100', '管理员工作站', 1, 1),
('恶意IP黑名单', 'BLACKLIST', '203.0.113.45', '暴力破解攻击源', 1, 1),
('恶意IP黑名单', 'BLACKLIST', '198.51.100.0', 'DDoS攻击源', 1, 1),
('恶意IP黑名单', 'BLACKLIST', '203.0.113.100', 'SQL注入攻击源', 1, 1);

-- 插入网络流量日志
INSERT INTO network_flow_log (flow_id, source_ip, target_ip, source_port, target_port, protocol, bytes_sent, bytes_received, packets_sent, packets_received, start_time, end_time, duration) VALUES
('FLOW-001', '192.168.1.100', '10.0.0.10', 54321, 80, 'TCP', 1024000, 5120000, 1500, 3500, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 3600),
('FLOW-002', '192.168.1.101', '10.0.0.20', 54322, 443, 'TCP', 2048000, 10240000, 2000, 8000, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 3600),
('FLOW-003', '203.0.113.45', '10.0.0.30', 45678, 22, 'TCP', 512000, 256000, 800, 400, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 1800),
('FLOW-004', '192.168.1.150', '8.8.8.8', 56789, 53, 'UDP', 102400, 204800, 200, 400, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), 900),
('FLOW-005', '10.0.0.50', '192.168.1.200', 3306, 54323, 'TCP', 5120000, 2560000, 5000, 2500, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), 900);

-- 插入威胁情报 - 修复：添加 indicator_value 字段
INSERT INTO threat_intelligence (ioc_type, ioc_value, indicator_value, threat_type, threat_level, source, description, first_seen, last_seen, confidence_score, status) VALUES
('IP', '203.0.113.45', '203.0.113.45', 'C2服务器', 'CRITICAL', 'AlienVault OTX', '已知的僵尸网络C2服务器', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 1 HOUR), 95, 1),
('DOMAIN', 'malicious-site.example.com', 'malicious-site.example.com', '恶意域名', 'HIGH', 'Abuse.ch', '钓鱼网站域名', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 88, 1),
('URL', 'http://phishing.example.com/login', 'http://phishing.example.com/login', '恶意URL', 'HIGH', 'PhishTank', '银行钓鱼页面', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 92, 1),
('FILE_HASH', 'd41d8cd98f00b204e9800998ecf8427e', 'd41d8cd98f00b204e9800998ecf8427e', '恶意文件', 'CRITICAL', 'VirusTotal', '勒索软件样本', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 98, 1),
('IP', '198.51.100.0', '198.51.100.0', '僵尸网络', 'MEDIUM', 'Spamhaus', '僵尸网络节点', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 5 HOUR), 75, 1);

-- 插入策略模板
INSERT IGNORE INTO policy_template (template_name, template_type, template_content, description, is_default, status, create_by) VALUES
('标准防火墙策略', 'FIREWALL', '{"rules":[{"action":"allow","port":"80,443","protocol":"tcp"},{"action":"deny","port":"*","protocol":"*"}]}', '适用于Web服务器的标准防火墙策略', 1, 1, 1),
('严格访问控制', 'ACL', '{"type":"whitelist","ips":["192.168.1.0/24"],"services":["ssh","https"]}', '仅允许内网访问的严格策略', 0, 1, 1),
('DDoS防护策略', 'ANTI_DDOS', '{"threshold":{"connections":1000,"rate":"100/s"},"action":"block","duration":3600}', 'DDoS攻击防护策略', 1, 1, 1);

-- 插入示例审批工作流
INSERT INTO approval_workflow (workflow_name, workflow_type, initiator_id, target_id, target_type, current_step, total_steps, status, approval_content) VALUES
('新增防火墙规则审批', 'FIREWALL_RULE', 2, 6, 'FIREWALL_RULE', 1, 2, 'PENDING', '申请添加新的防火墙规则以允许新服务访问'),
('修改访问控制审批', 'ACL_UPDATE', 2, 3, 'ACL', 2, 2, 'APPROVED', '修改白名单，添加新的可信IP地址');

-- 插入审批步骤记录
INSERT INTO approval_step (workflow_id, step_number, approver_id, approval_status, approval_comment, approval_time) VALUES
(1, 1, 1, 'PENDING', NULL, NULL),
(2, 1, 1, 'APPROVED', '审批通过，符合安全策略', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, 1, 'APPROVED', '最终审批通过', DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- 插入系统操作日志
INSERT INTO sys_operation_log (user_id, username, operation, method, params, ip, location, status, execution_time) VALUES
(1, 'admin', '用户登录', 'POST /api/auth/login', '{"username":"admin"}', '192.168.1.100', '上海市', 1, 120),
(1, 'admin', '查看安全事件', 'GET /api/security/events', '{"page":1,"size":10}', '192.168.1.100', '上海市', 1, 85),
(2, 'operator', '添加防火墙规则', 'POST /api/policy/firewall', '{"ruleName":"新规则"}', '192.168.1.101', '北京市', 1, 150),
(2, 'operator', '处理安全事件', 'PUT /api/security/events/3', '{"status":"PROCESSING"}', '192.168.1.101', '北京市', 1, 95),
(1, 'admin', '审批通过', 'POST /api/workflow/approve/2', '{"approved":true}', '192.168.1.100', '上海市', 1, 110);
