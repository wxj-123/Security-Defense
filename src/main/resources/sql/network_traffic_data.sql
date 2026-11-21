USE security_defense;

-- 插入更多网络流量示例数据
INSERT IGNORE INTO network_traffic (flow_id, timestamp, source_ip, source_port, destination_ip, destination_port, protocol, bytes_sent, bytes_received, packets_sent, packets_received, duration, flow_type, flow_status, risk_score, source_country, source_city, destination_country, destination_city, application_protocol, encryption_status, anomaly_detected, anomaly_type, related_asset_id) VALUES
-- 针对邮件服务器的流量数据
('FLOW-106', DATE_SUB(NOW(), INTERVAL 360 MINUTE), '192.0.2.100', 34567, '192.168.1.40', 25, 'TCP', 1536000, 7680000, 2000, 5000, 3600, 'SMTP', 'ACTIVE', 78.6, '美国', '纽约', '中国', '广州', 'SMTP', 'TLS', 1, '垃圾邮件', 'AST-004'),
('FLOW-107', DATE_SUB(NOW(), INTERVAL 420 MINUTE), '192.0.2.100', 45678, '192.168.1.40', 110, 'TCP', 768000, 384000, 1000, 800, 1800, 'POP3', 'ACTIVE', 82.3, '美国', '纽约', '中国', '广州', 'POP3', 'NONE', 1, '钓鱼邮件', 'AST-004'),

-- 针对文件服务器的流量数据
('FLOW-108', DATE_SUB(NOW(), INTERVAL 480 MINUTE), '192.168.2.50', 56789, '192.168.1.30', 445, 'TCP', 2048000, 1024000, 3000, 1500, 3600, 'SMB', 'ACTIVE', 85.7, '中国', '深圳', '中国', '上海', 'SMB', 'NONE', 1, '异常文件访问', 'AST-003'),
('FLOW-109', DATE_SUB(NOW(), INTERVAL 540 MINUTE), '192.168.2.50', 12345, '192.168.1.30', 139, 'TCP', 1024000, 512000, 1500, 1000, 1800, 'NetBIOS', 'ACTIVE', 72.4, '中国', '深圳', '中国', '上海', 'NetBIOS', 'NONE', 0, NULL, 'AST-003'),

-- 针对开发测试服务器的流量数据
('FLOW-110', DATE_SUB(NOW(), INTERVAL 600 MINUTE), '203.0.113.75', 23456, '192.168.2.10', 22, 'TCP', 512000, 256000, 800, 400, 900, 'SSH', 'ACTIVE', 68.9, '英国', '伦敦', '中国', '深圳', 'SSH', 'TLS', 1, '暴力破解', 'AST-005'),
('FLOW-111', DATE_SUB(NOW(), INTERVAL 660 MINUTE), '203.0.113.75', 34567, '192.168.2.10', 3389, 'TCP', 1024000, 512000, 1500, 800, 1800, 'RDP', 'ACTIVE', 75.2, '英国', '伦敦', '中国', '深圳', 'RDP', 'TLS', 1, '远程桌面爆破', 'AST-005'),

-- 针对Web服务器的更多流量数据
('FLOW-112', DATE_SUB(NOW(), INTERVAL 720 MINUTE), '198.51.100.50', 45678, '192.168.1.10', 80, 'TCP', 3072000, 15360000, 4000, 8000, 3600, 'HTTP', 'ACTIVE', 88.5, '德国', '柏林', '中国', '北京', 'HTTP', 'NONE', 1, 'DDoS攻击', 'AST-001'),
('FLOW-113', DATE_SUB(NOW(), INTERVAL 780 MINUTE), '198.51.100.50', 56789, '192.168.1.10', 443, 'TCP', 2048000, 10240000, 3000, 6000, 3600, 'HTTPS', 'ACTIVE', 82.7, '德国', '柏林', '中国', '北京', 'HTTPS', 'TLS', 1, 'Web应用攻击', 'AST-001'),

-- 针对数据库服务器的更多流量数据
('FLOW-114', DATE_SUB(NOW(), INTERVAL 840 MINUTE), '192.0.2.200', 12345, '192.168.1.20', 1433, 'TCP', 256000, 128000, 500, 300, 900, 'MSSQL', 'ACTIVE', 89.3, '法国', '巴黎', '中国', '北京', 'MSSQL', 'TLS', 1, 'SQL注入', 'AST-002'),
('FLOW-115', DATE_SUB(NOW(), INTERVAL 900 MINUTE), '192.0.2.200', 23456, '192.168.1.20', 1521, 'TCP', 384000, 192000, 600, 400, 1800, 'Oracle', 'ACTIVE', 85.6, '法国', '巴黎', '中国', '北京', 'Oracle', 'TLS', 1, '数据库扫描', 'AST-002');