USE security_defense;

-- 插入更多预测预警示例数据
INSERT IGNORE INTO prediction_alert (alert_id, alert_type, alert_level, predicted_time, related_asset_id, related_threat_type, prediction_confidence, description, recommendation, status) VALUES
-- Web服务器相关预测预警
('ALERT-005', 'THREAT_PREDICTION', 'CRITICAL', DATE_ADD(NOW(), INTERVAL 180 MINUTE), 'AST-001', 'Web应用攻击', 88.7, '预测Web服务器在未来3小时内可能遭受Web应用攻击', '建议检查Web应用防火墙规则，更新漏洞补丁', 'ACTIVE'),
('ALERT-006', 'VULNERABILITY_EXPLOIT', 'HIGH', DATE_ADD(NOW(), INTERVAL 300 MINUTE), 'AST-001', '远程代码执行', 82.3, '预测Web服务器的远程代码执行漏洞可能被利用', '建议立即应用相关安全补丁，限制不必要的访问', 'ACTIVE'),

-- 数据库服务器相关预测预警
('ALERT-007', 'THREAT_PREDICTION', 'CRITICAL', DATE_ADD(NOW(), INTERVAL 240 MINUTE), 'AST-002', '数据库注入攻击', 91.2, '预测数据库服务器在未来4小时内可能遭受注入攻击', '建议加强输入验证，启用数据库活动监控', 'ACTIVE'),
('ALERT-008', 'ANOMALY_DETECTION', 'HIGH', DATE_ADD(NOW(), INTERVAL 360 MINUTE), 'AST-002', '异常数据库访问', 78.6, '预测数据库服务器可能出现异常访问模式', '建议审查数据库访问日志，验证用户权限', 'ACTIVE'),

-- 邮件服务器相关预测预警
('ALERT-009', 'THREAT_PREDICTION', 'HIGH', DATE_ADD(NOW(), INTERVAL 420 MINUTE), 'AST-004', '高级持续性威胁(APT)', 85.9, '预测邮件服务器可能成为APT攻击的入口点', '建议部署高级威胁防护解决方案，加强邮件过滤', 'ACTIVE'),
('ALERT-010', 'VULNERABILITY_EXPLOIT', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 540 MINUTE), 'AST-004', '邮件服务器配置缺陷', 72.4, '预测邮件服务器配置缺陷可能被利用', '建议审查邮件服务器配置，关闭不必要的服务', 'ACTIVE'),

-- 文件服务器相关预测预警
('ALERT-011', 'THREAT_PREDICTION', 'HIGH', DATE_ADD(NOW(), INTERVAL 600 MINUTE), 'AST-003', '勒索软件攻击', 87.3, '预测文件服务器在未来10小时内可能遭受勒索软件攻击', '建议备份重要数据，部署勒索软件防护工具', 'ACTIVE'),
('ALERT-012', 'ANOMALY_DETECTION', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 720 MINUTE), 'AST-003', '异常文件访问', 75.8, '预测文件服务器可能出现异常文件访问行为', '建议实施文件访问控制，启用文件完整性监控', 'ACTIVE'),

-- 开发测试服务器相关预测预警
('ALERT-013', 'THREAT_PREDICTION', 'MEDIUM', DATE_ADD(NOW(), INTERVAL 840 MINUTE), 'AST-005', '弱密码爆破', 68.7, '预测开发测试服务器可能遭受弱密码爆破攻击', '建议实施强密码策略，启用账户锁定机制', 'ACTIVE'),
('ALERT-014', 'VULNERABILITY_EXPLOIT', 'HIGH', DATE_ADD(NOW(), INTERVAL 960 MINUTE), 'AST-005', '未授权访问', 82.1, '预测开发测试服务器的未授权访问漏洞可能被利用', '建议审查访问控制策略，限制开发环境访问', 'ACTIVE'),

-- 跨资产威胁预测预警
('ALERT-015', 'LATERAL_MOVEMENT', 'CRITICAL', DATE_ADD(NOW(), INTERVAL 1080 MINUTE), NULL, '横向移动攻击', 89.5, '预测网络中可能发生横向移动攻击，多个资产可能受影响', '建议隔离受感染资产，审查网络分段策略', 'ACTIVE'),
('ALERT-016', 'CAMPAIGN_DETECTION', 'HIGH', DATE_ADD(NOW(), INTERVAL 1200 MINUTE), NULL, '定向攻击活动', 84.6, '预测可能存在针对本组织的定向攻击活动', '建议提高安全警戒级别，加强威胁情报收集', 'ACTIVE');