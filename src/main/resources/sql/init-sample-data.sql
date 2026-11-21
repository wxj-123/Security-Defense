USE security_defense;

-- 插入示例资产数据
INSERT INTO asset (asset_id, asset_name, asset_type, asset_ip, asset_location, business_importance, asset_value_score, asset_tags, responsible_person, security_status, last_scan_time) VALUES
('AST-001', 'Web服务器01', 'SERVER', '192.168.1.10', '北京数据中心', '3', 85.5, 'Web,Production', '张三', 'NORMAL', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('AST-002', '数据库服务器01', 'DATABASE', '192.168.1.20', '北京数据中心', '3', 95.2, 'Database,Production', '李四', 'NORMAL', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('AST-003', '文件服务器01', 'STORAGE', '192.168.1.30', '上海数据中心', '2', 70.8, 'File,Backup', '王五', 'NORMAL', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('AST-004', '邮件服务器01', 'MAIL', '192.168.1.40', '广州数据中心', '2', 65.3, 'Mail,Communication', '赵六', 'WARNING', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('AST-005', '开发测试服务器01', 'SERVER', '192.168.2.10', '深圳办公室', '1', 45.7, 'Dev,Test', '孙七', 'NORMAL', DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- 插入示例态势数据
INSERT INTO situation_data (timestamp, situation_index, situation_level, total_assets, critical_assets, high_threats, critical_vulnerabilities, threat_score, vulnerability_score, asset_value_score, risk_trend, main_threat_types, main_affected_asset_types, security_event_count, abnormal_behavior_count) VALUES
(DATE_SUB(NOW(), INTERVAL 1 HOUR), 75.6, 'HIGH', 5, 1, 3, 2, 82.3, 78.9, 85.5, 'STABLE', '{"DDoS攻击": 2, "恶意软件": 1}', '{"SERVER": 2, "DATABASE": 1}', 5, 2),
(DATE_SUB(NOW(), INTERVAL 2 HOUR), 68.4, 'MEDIUM', 5, 0, 2, 1, 75.2, 70.1, 82.3, 'UP', '{"端口扫描": 3, "暴力破解": 2}', '{"SERVER": 3, "MAIL": 1}', 4, 1),
(DATE_SUB(NOW(), INTERVAL 3 HOUR), 82.1, 'HIGH', 5, 2, 4, 3, 88.7, 85.2, 90.1, 'DOWN', '{"DDoS攻击": 1, "SQL注入": 2, "恶意软件": 1}', '{"DATABASE": 2, "SERVER": 1}', 7, 3),
(DATE_SUB(NOW(), INTERVAL 4 HOUR), 58.9, 'MEDIUM', 5, 0, 1, 1, 65.4, 62.8, 75.6, 'STABLE', '{"端口扫描": 2}', '{"SERVER": 1, "MAIL": 1}', 3, 1),
(DATE_SUB(NOW(), INTERVAL 5 HOUR), 91.3, 'CRITICAL', 5, 3, 5, 4, 95.6, 92.1, 98.7, 'UP', '{"DDoS攻击": 3, "SQL注入": 1, "恶意软件": 1}', '{"DATABASE": 2, "SERVER": 2}', 9, 4);

-- 插入示例资产风险数据
INSERT INTO asset_risk (asset_id, asset_name, asset_type, business_importance, risk_score, risk_level, vulnerability_count, high_vulnerability_count, medium_vulnerability_count, low_vulnerability_count, threat_event_count, last_threat_time, asset_value_score, exposure_score, attack_surface, asset_ip, asset_location, risk_trend, main_risk_source, remediation_suggestion, responsible_person, security_status, last_scan_time) VALUES
('AST-001', 'Web服务器01', 'SERVER', '3', 72.5, 'HIGH', 5, 2, 2, 1, 3, DATE_SUB(NOW(), INTERVAL 2 HOUR), 85.5, 78.3, '{"ports": [80, 443, 22], "services": ["HTTP", "HTTPS", "SSH"]}', '192.168.1.10', '北京数据中心', 'STABLE', '外部攻击', '建议更新防火墙规则，限制SSH访问', '张三', 'NORMAL', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('AST-002', '数据库服务器01', 'DATABASE', '3', 88.7, 'CRITICAL', 7, 3, 3, 1, 5, DATE_SUB(NOW(), INTERVAL 1 HOUR), 95.2, 92.1, '{"ports": [3306, 1433, 1521], "services": ["MySQL", "MSSQL", "Oracle"]}', '192.168.1.20', '北京数据中心', 'UP', 'SQL注入', '建议加强数据库访问控制，定期更新补丁', '李四', 'NORMAL', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('AST-003', '文件服务器01', 'STORAGE', '2', 55.2, 'MEDIUM', 3, 1, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), 70.8, 65.4, '{"ports": [445, 139], "services": ["SMB", "NetBIOS"]}', '192.168.1.30', '上海数据中心', 'STABLE', '内部威胁', '建议定期扫描文件共享权限', '王五', 'NORMAL', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('AST-004', '邮件服务器01', 'MAIL', '2', 68.9, 'HIGH', 4, 1, 2, 1, 2, DATE_SUB(NOW(), INTERVAL 4 HOUR), 65.3, 72.8, '{"ports": [25, 110, 143, 993], "services": ["SMTP", "POP3", "IMAP"]}', '192.168.1.40', '广州数据中心', 'DOWN', '钓鱼邮件', '建议部署邮件安全网关，加强用户安全意识培训', '赵六', 'WARNING', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('AST-005', '开发测试服务器01', 'SERVER', '1', 42.3, 'MEDIUM', 2, 0, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR), 45.7, 50.2, '{"ports": [80, 443, 22, 3389], "services": ["HTTP", "HTTPS", "SSH", "RDP"]}', '192.168.2.10', '深圳办公室', 'STABLE', '弱密码', '建议加强密码策略，定期更换密码', '孙七', 'NORMAL', DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- 插入示例攻击链数据
INSERT INTO attack_chain (chain_id, attack_phase, phase_order, technique_id, technique_name, related_events, source_ip, target_asset_id, start_time, end_time, duration, confidence_score, severity, status) VALUES
('CHAIN-001', '侦察', 1, 'T1046', '网络服务扫描', '["EVENT-101", "EVENT-102"]', '203.0.113.45', 'AST-001', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR 45 MINUTE), 900, 85.5, 'HIGH', 'ACTIVE'),
('CHAIN-001', '初始访问', 2, 'T1190', '利用面向公众的应用程序', '["EVENT-103"]', '203.0.113.45', 'AST-001', DATE_SUB(NOW(), INTERVAL 2 HOUR 45 MINUTE), DATE_SUB(NOW(), INTERVAL 2 HOUR 30 MINUTE), 900, 92.3, 'CRITICAL', 'ACTIVE'),
('CHAIN-001', '执行', 3, 'T1059', '命令和脚本解释器', '["EVENT-104"]', '203.0.113.45', 'AST-001', DATE_SUB(NOW(), INTERVAL 2 HOUR 30 MINUTE), DATE_SUB(NOW(), INTERVAL 2 HOUR 15 MINUTE), 900, 88.7, 'HIGH', 'ACTIVE'),
('CHAIN-002', '侦察', 1, 'T1046', '网络服务扫描', '["EVENT-201"]', '198.51.100.0', 'AST-002', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR 45 MINUTE), 900, 78.9, 'MEDIUM', 'ACTIVE'),
('CHAIN-002', '初始访问', 2, 'T1110', '暴力破解', '["EVENT-202"]', '198.51.100.0', 'AST-002', DATE_SUB(NOW(), INTERVAL 1 HOUR 45 MINUTE), DATE_SUB(NOW(), INTERVAL 1 HOUR 30 MINUTE), 900, 85.2, 'HIGH', 'ACTIVE'),
('CHAIN-002', '凭证访问', 3, 'T1003', '操作系统凭据转储', '["EVENT-203"]', '198.51.100.0', 'AST-002', DATE_SUB(NOW(), INTERVAL 1 HOUR 30 MINUTE), DATE_SUB(NOW(), INTERVAL 1 HOUR 15 MINUTE), 900, 91.7, 'CRITICAL', 'ACTIVE');

-- 插入示例网络流量数据
INSERT INTO network_traffic (flow_id, timestamp, source_ip, source_port, destination_ip, destination_port, protocol, bytes_sent, bytes_received, packets_sent, packets_received, duration, flow_type, flow_status, risk_score, source_country, source_city, destination_country, destination_city, application_protocol, encryption_status, anomaly_detected, anomaly_type, related_asset_id) VALUES
('FLOW-101', DATE_SUB(NOW(), INTERVAL 1 HOUR), '203.0.113.45', 54321, '192.168.1.10', 80, 'TCP', 1024000, 5120000, 1500, 3500, 3600, 'HTTP', 'ACTIVE', 85.5, '美国', '洛杉矶', '中国', '北京', 'HTTP', 'NONE', 1, 'DDoS攻击', 'AST-001'),
('FLOW-102', DATE_SUB(NOW(), INTERVAL 2 HOUR), '198.51.100.0', 45678, '192.168.1.20', 3306, 'TCP', 512000, 256000, 800, 400, 1800, 'MySQL', 'ACTIVE', 92.3, '俄罗斯', '莫斯科', '中国', '北京', 'MySQL', 'TLS', 1, 'SQL注入', 'AST-002'),
('FLOW-103', DATE_SUB(NOW(), INTERVAL 3 HOUR), '192.168.1.150', '56789', '8.8.8.8', 53, 'UDP', 102400, 204800, 200, 400, 900, 'DNS', 'ACTIVE', 65.7, '中国', '上海', '美国', '加利福尼亚', 'DNS', 'NONE', 0, NULL, 'AST-003'),
('FLOW-104', DATE_SUB(NOW(), INTERVAL 4 HOUR), '10.0.0.50', '3306', '192.168.1.200', 54323, 'TCP', 5120000, 2560000, 5000, 2500, 900, 'Database', 'ACTIVE', 78.9, '中国', '广州', '中国', '深圳', 'MySQL', 'TLS', 0, NULL, 'AST-002'),
('FLOW-105', DATE_SUB(NOW(), INTERVAL 5 HOUR), '172.16.0.100', '12345', '192.168.1.40', 25, 'TCP', 2048000, 10240000, 2000, 8000, 3600, 'SMTP', 'ACTIVE', 72.1, '中国', '北京', '中国', '广州', 'SMTP', 'TLS', 1, '垃圾邮件', 'AST-004');

-- 插入示例安全日志数据
INSERT INTO security_log (timestamp, source_system, log_type, event_id, severity, message, source_ip, user_id, asset_id, action, result, risk_score) VALUES
(DATE_SUB(NOW(), INTERVAL 10 MINUTE), '防火墙', 'NETWORK', 'FW-20230101-001', 'HIGH', '检测到DDoS攻击流量', '203.0.113.45', NULL, 'AST-001', 'BLOCK', 'SUCCESS', 85.5),
(DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'IDS', 'INTRUSION', 'IDS-20230101-002', 'CRITICAL', 'SQL注入攻击尝试', '198.51.100.0', NULL, 'AST-002', 'ALERT', 'SUCCESS', 92.3),
(DATE_SUB(NOW(), INTERVAL 30 MINUTE), '防病毒', 'MALWARE', 'AV-20230101-003', 'HIGH', '发现恶意软件活动', '192.168.1.150', NULL, 'AST-003', 'QUARANTINE', 'SUCCESS', 78.9),
(DATE_SUB(NOW(), INTERVAL 40 MINUTE), '系统日志', 'AUTHENTICATION', 'SYS-20230101-004', 'MEDIUM', '多次登录失败', '10.0.0.50', 'USER-001', 'AST-002', 'DENY', 'SUCCESS', 65.7),
(DATE_SUB(NOW(), INTERVAL 50 MINUTE), '邮件网关', 'EMAIL', 'MG-20230101-005', 'MEDIUM', '收到钓鱼邮件', '172.16.0.100', NULL, 'AST-004', 'BLOCK', 'SUCCESS', 72.1);

-- 插入示例漏洞详情数据
INSERT INTO vulnerability_detail (vuln_id, cve_id, asset_id, vulnerability_type, severity, cvss_score, description, impact, remediation, discovered_time, patched_time, status) VALUES
('VULN-001', 'CVE-2023-1234', 'AST-001', '远程代码执行', 'CRITICAL', 9.8, 'Web服务器存在未授权远程代码执行漏洞', '攻击者可远程执行任意代码，完全控制服务器', '立即应用官方安全补丁', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 'OPEN'),
('VULN-002', 'CVE-2023-5678', 'AST-002', 'SQL注入', 'CRITICAL', 9.4, '数据库服务器存在SQL注入漏洞', '攻击者可绕过认证，读取、修改或删除数据库内容', '应用官方安全补丁并检查输入验证', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 'OPEN'),
('VULN-003', 'CVE-2023-9012', 'AST-001', '跨站脚本', 'HIGH', 8.1, 'Web应用存在反射型XSS漏洞', '攻击者可执行恶意脚本，窃取用户信息', '对用户输入进行严格过滤和转义', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), 'PATCHED'),
('VULN-004', 'CVE-2023-3456', 'AST-004', '弱密码', 'MEDIUM', 5.3, '邮件服务器使用弱密码策略', '攻击者可通过暴力破解获得访问权限', '加强密码策略，启用多因素认证', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, 'OPEN'),
('VULN-005', 'CVE-2023-7890', 'AST-003', '信息泄露', 'MEDIUM', 6.5, '文件服务器配置错误导致敏感信息泄露', '攻击者可获取系统配置信息和文件列表', '修改服务器配置，限制目录访问', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'PATCHED');

-- 插入示例关联结果数据
INSERT INTO correlation_result (correlation_id, event_ids, correlation_type, related_asset_id, related_threat_id, confidence_score, severity, summary, start_time, end_time, status) VALUES
('CORR-001', '["EVENT-101", "EVENT-102", "EVENT-103"]', 'ATTACK_CHAIN', 'AST-001', 'THREAT-001', 92.3, 'CRITICAL', '检测到针对Web服务器的完整攻击链', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'ACTIVE'),
('CORR-002', '["EVENT-201", "EVENT-202", "EVENT-203"]', 'ATTACK_CHAIN', 'AST-002', 'THREAT-002', 88.7, 'HIGH', '检测到针对数据库服务器的攻击链', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'ACTIVE'),
('CORR-003', '["FLOW-101", "FW-20230101-001"]', 'THREAT_CORRELATION', 'AST-001', 'THREAT-001', 85.5, 'HIGH', '网络流量与防火墙告警关联分析', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 50 MINUTE), 'ACTIVE'),
('CORR-004', '["IDS-20230101-002", "FLOW-102"]', 'THREAT_CORRELATION', 'AST-002', 'THREAT-002', 91.7, 'CRITICAL', '入侵检测与网络流量关联分析', DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'ACTIVE');

-- 插入示例预测预警数据
INSERT INTO prediction_alert (alert_id, alert_type, alert_level, predicted_time, related_asset_id, related_threat_type, prediction_confidence, description, recommendation, status) VALUES
('ALERT-001', 'THREAT_PREDICTION', 'HIGH', DATE_ADD(NOW(), INTERVAL 2 HOUR), 'AST-001', 'DDoS攻击', 85.5, '预测未来2小时内可能遭受DDoS攻击', '建议提前调整防火墙策略，准备流量清洗', 'ACTIVE'),
('ALERT-002', 'VULNERABILITY_EXPLOIT', 'CRITICAL', DATE_ADD(NOW(), INTERVAL 4 HOUR), 'AST-002', 'SQL注入', 92.3, '预测数据库服务器漏洞可能被利用', '建议立即应用安全补丁，加强访问控制', 'ACTIVE'),
('ALERT-003', 'THREAT_PREDICTION', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 6 HOUR), 'AST-004', '钓鱼攻击', 72.1, '预测邮件服务器可能遭受钓鱼攻击', '建议更新邮件过滤规则，提醒用户注意可疑邮件', 'ACTIVE'),
('ALERT-004', 'ANOMALY_DETECTION', 'HIGH', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'AST-003', '异常访问', 78.9, '预测文件服务器可能出现异常访问行为', '建议检查访问日志，验证访问权限', 'ACTIVE');