USE security_defense;

-- 分批插入攻击链数据以避免语法问题

-- 攻击链3：针对邮件服务器的攻击
INSERT IGNORE INTO attack_chain (chain_id, attack_phase, phase_order, technique_id, technique_name, related_events, source_ip, target_asset_id, start_time, end_time, duration, confidence_score, severity, status) VALUES
('CHAIN-003', '侦察', 1, 'T1046', '网络服务扫描', '["EVENT-301", "EVENT-302"]', '192.0.2.100', 'AST-004', DATE_SUB(NOW(), INTERVAL 300 MINUTE), DATE_SUB(NOW(), INTERVAL 285 MINUTE), 900, 75.3, 'MEDIUM', 'ACTIVE'),
('CHAIN-003', '初始访问', 2, 'T1566', '网络钓鱼', '["EVENT-303"]', '192.0.2.100', 'AST-004', DATE_SUB(NOW(), INTERVAL 285 MINUTE), DATE_SUB(NOW(), INTERVAL 270 MINUTE), 900, 82.7, 'HIGH', 'ACTIVE'),
('CHAIN-003', '执行', 3, 'T1204', '用户执行', '["EVENT-304"]', '192.0.2.100', 'AST-004', DATE_SUB(NOW(), INTERVAL 270 MINUTE), DATE_SUB(NOW(), INTERVAL 255 MINUTE), 900, 88.2, 'HIGH', 'ACTIVE'),
('CHAIN-003', '持久化', 4, 'T1547', '注册表启动项', '["EVENT-305"]', '192.0.2.100', 'AST-004', DATE_SUB(NOW(), INTERVAL 255 MINUTE), DATE_SUB(NOW(), INTERVAL 240 MINUTE), 900, 85.6, 'HIGH', 'ACTIVE');

-- 攻击链4：针对文件服务器的内部攻击
INSERT IGNORE INTO attack_chain (chain_id, attack_phase, phase_order, technique_id, technique_name, related_events, source_ip, target_asset_id, start_time, end_time, duration, confidence_score, severity, status) VALUES
('CHAIN-004', '初始访问', 1, 'T1078', '有效账户', '["EVENT-401"]', '192.168.2.50', 'AST-003', DATE_SUB(NOW(), INTERVAL 360 MINUTE), DATE_SUB(NOW(), INTERVAL 345 MINUTE), 900, 72.8, 'MEDIUM', 'ACTIVE'),
('CHAIN-004', '执行', 2, 'T1059', '命令和脚本解释器', '["EVENT-402"]', '192.168.2.50', 'AST-003', DATE_SUB(NOW(), INTERVAL 345 MINUTE), DATE_SUB(NOW(), INTERVAL 330 MINUTE), 900, 78.9, 'MEDIUM', 'ACTIVE'),
('CHAIN-004', '防御规避', 3, 'T1070', '指示器移除', '["EVENT-403"]', '192.168.2.50', 'AST-003', DATE_SUB(NOW(), INTERVAL 330 MINUTE), DATE_SUB(NOW(), INTERVAL 315 MINUTE), 900, 82.4, 'HIGH', 'ACTIVE'),
('CHAIN-004', '横向移动', 4, 'T1021', '远程服务', '["EVENT-404"]', '192.168.2.50', 'AST-003', DATE_SUB(NOW(), INTERVAL 315 MINUTE), DATE_SUB(NOW(), INTERVAL 300 MINUTE), 900, 85.7, 'HIGH', 'ACTIVE'),
('CHAIN-004', '收集', 5, 'T1005', '数据从本地系统', '["EVENT-405"]', '192.168.2.50', 'AST-003', DATE_SUB(NOW(), INTERVAL 300 MINUTE), DATE_SUB(NOW(), INTERVAL 285 MINUTE), 900, 89.3, 'CRITICAL', 'ACTIVE');

-- 攻击链5：针对开发测试服务器的攻击
INSERT IGNORE INTO attack_chain (chain_id, attack_phase, phase_order, technique_id, technique_name, related_events, source_ip, target_asset_id, start_time, end_time, duration, confidence_score, severity, status) VALUES
('CHAIN-005', '侦察', 1, 'T1040', '网络嗅探', '["EVENT-501"]', '203.0.113.75', 'AST-005', DATE_SUB(NOW(), INTERVAL 420 MINUTE), DATE_SUB(NOW(), INTERVAL 405 MINUTE), 900, 68.7, 'MEDIUM', 'ACTIVE'),
('CHAIN-005', '初始访问', 2, 'T1110', '暴力破解', '["EVENT-502"]', '203.0.113.75', 'AST-005', DATE_SUB(NOW(), INTERVAL 405 MINUTE), DATE_SUB(NOW(), INTERVAL 390 MINUTE), 900, 75.2, 'MEDIUM', 'ACTIVE'),
('CHAIN-005', '执行', 3, 'T1059', '命令和脚本解释器', '["EVENT-503"]', '203.0.113.75', 'AST-005', DATE_SUB(NOW(), INTERVAL 390 MINUTE), DATE_SUB(NOW(), INTERVAL 375 MINUTE), 900, 81.6, 'HIGH', 'ACTIVE'),
('CHAIN-005', '防御规避', 4, 'T1140', '混淆文件/信息', '["EVENT-504"]', '203.0.113.75', 'AST-005', DATE_SUB(NOW(), INTERVAL 375 MINUTE), DATE_SUB(NOW(), INTERVAL 360 MINUTE), 900, 78.9, 'MEDIUM', 'ACTIVE'),
('CHAIN-005', '发现', 5, 'T1082', '系统信息发现', '["EVENT-505"]', '203.0.113.75', 'AST-005', DATE_SUB(NOW(), INTERVAL 360 MINUTE), DATE_SUB(NOW(), INTERVAL 345 MINUTE), 900, 82.1, 'HIGH', 'ACTIVE'),
('CHAIN-005', '凭证访问', 6, 'T1555', '密码', '["EVENT-506"]', '203.0.113.75', 'AST-005', DATE_SUB(NOW(), INTERVAL 345 MINUTE), DATE_SUB(NOW(), INTERVAL 330 MINUTE), 900, 88.5, 'HIGH', 'ACTIVE');