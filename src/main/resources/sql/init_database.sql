-- 安全态势感知系统数据库初始化脚本
-- 
-- 数据库结构说明：
-- 1. 本数据库主要用于存储和管理安全态势感知系统的各类数据
-- 2. 核心表包括资产管理、态势数据、威胁情报、漏洞管理、攻击链分析等
-- 3. 表间通过外键关系确保数据一致性和完整性
-- 4. 索引设计优化了查询性能，特别是针对时间序列和关联查询
-- 5. 支持数据的逻辑删除和状态管理
-- 
-- 数据流向：
-- 资产数据 -> 漏洞/威胁数据采集 -> 态势计算/关联分析 -> 预测预警
-- 
-- 注意事项：
-- - 所有表使用CREATE TABLE IF NOT EXISTS避免重复创建
-- - 关键字段添加了索引以优化查询性能
-- - 日期时间字段使用TIMESTAMP格式
-- - 重要字段添加了NOT NULL约束确保数据完整性

-- 创建资产主表
-- 用途：存储系统所有安全资产的基础信息，是其他数据表的关联中心
-- 说明：
-- - 作为资产数据的核心表，为其他表提供外键关联
-- - 记录资产的基本属性，包括资产ID、名称、IP、类型等
-- - 支持按资产ID、IP地址、资产类型等多种维度查询
-- - 提供资产管理基础数据支持
CREATE TABLE IF NOT EXISTS asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id VARCHAR(100) NOT NULL UNIQUE COMMENT '资产唯一标识',
    asset_name VARCHAR(255) NOT NULL COMMENT '资产名称',
    asset_type VARCHAR(100) NOT NULL COMMENT '资产类型',
    asset_ip VARCHAR(50) COMMENT '资产IP地址',
    asset_location VARCHAR(100) COMMENT '资产位置',
    business_importance VARCHAR(20) DEFAULT '1' COMMENT '业务重要性等级(1-3)',
    asset_value_score DOUBLE NOT NULL DEFAULT 0 COMMENT '资产价值评分',
    asset_tags VARCHAR(200) COMMENT '资产标签',
    responsible_person VARCHAR(50) COMMENT '责任人',
    security_status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '安全状态',
    last_scan_time DATETIME COMMENT '最后扫描时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_asset_id (asset_id),
    INDEX idx_asset_ip (asset_ip),
    INDEX idx_asset_type (asset_type)
) COMMENT='资产主表';

-- 创建态势数据表
-- 用途：存储系统计算出的整体安全态势数据，记录不同时间点的安全状态
-- 说明：
-- - 态势指数是核心指标，综合反映当前系统整体安全状况
-- - 包含威胁、漏洞、资产三个维度的评分数据
-- - 记录趋势信息，支持安全态势变化分析
CREATE TABLE IF NOT EXISTS situation_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    situation_index DOUBLE NOT NULL COMMENT '态势指数(0-100)',
    situation_level VARCHAR(20) NOT NULL COMMENT '态势等级',
    total_assets INT NOT NULL COMMENT '涉事资产数量',
    critical_assets INT NOT NULL COMMENT '危急资产数量',
    high_threats INT NOT NULL COMMENT '高危威胁数量',
    critical_vulnerabilities INT NOT NULL COMMENT '危急漏洞数量',
    threat_score DOUBLE NOT NULL COMMENT '威胁评分',
    vulnerability_score DOUBLE NOT NULL COMMENT '脆弱性评分',
    asset_value_score DOUBLE NOT NULL COMMENT '资产价值评分',
    asset_score DOUBLE DEFAULT 0 COMMENT '资产评分',
    risk_trend VARCHAR(20) COMMENT '风险趋势',
    main_threat_types VARCHAR(500) COMMENT '主要威胁类型JSON',
    main_affected_asset_types VARCHAR(500) COMMENT '主要受影响资产类型JSON',
    security_event_count INT DEFAULT 0 COMMENT '安全事件数量',
    abnormal_behavior_count INT DEFAULT 0 COMMENT '异常行为数量',
    risk_distribution VARCHAR(500) COMMENT '风险分布JSON',
    trend_indicator VARCHAR(20) COMMENT '趋势指标',
    remark VARCHAR(500) COMMENT '备注信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_timestamp (timestamp),
    INDEX idx_deleted (deleted),
    INDEX idx_situation_level (situation_level)
) COMMENT='态势数据表';

-- 创建威胁情报表
-- 用途：存储各类威胁情报信息，包括IP、URL、域名、文件哈希等指标
-- 说明：
-- - 记录威胁指标的类型、级别和来源信息
-- - 通过置信度分数评估威胁情报的可靠性
-- - 跟踪威胁指标的首次和最后发现时间
-- - 支持关联分析，与其他安全事件关联
CREATE TABLE IF NOT EXISTS threat_intelligence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ioc_value VARCHAR(255) NOT NULL COMMENT '指标值',
    ioc_type VARCHAR(50) NOT NULL COMMENT '指标类型(IP/URL/DOMAIN/FILE_HASH等)',
    threat_type VARCHAR(100) NOT NULL COMMENT '威胁类型',
    threat_level VARCHAR(20) NOT NULL COMMENT '威胁等级',
    confidence_score DOUBLE NOT NULL COMMENT '置信度分数(0-100)',
    source VARCHAR(100) NOT NULL COMMENT '来源',
    first_seen DATETIME NOT NULL COMMENT '首次发现时间',
    last_seen DATETIME NOT NULL COMMENT '最后发现时间',
    description TEXT COMMENT '描述',
    related_cve VARCHAR(255) COMMENT '相关CVE编号',
    location_info VARCHAR(255) COMMENT '地理位置信息',
    tags VARCHAR(255) COMMENT '标签',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ioc_value (ioc_value),
    INDEX idx_ioc_type (ioc_type),
    INDEX idx_threat_level (threat_level),
    INDEX idx_status (status)
) COMMENT='威胁情报表';

-- 创建资产风险表
-- 用途：存储各资产的风险评估数据，记录资产安全状况和风险详情
-- 说明：
-- - 外键关联到asset表，确保数据一致性
-- - 包含风险评分、风险等级、漏洞数量等关键指标
-- - 记录风险趋势和主要风险来源
-- - 提供修复建议，指导安全加固工作
-- - 支持按资产ID、IP、风险等级等条件查询
CREATE TABLE IF NOT EXISTS asset_risk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id VARCHAR(100) NOT NULL COMMENT '资产ID',
    asset_name VARCHAR(255) NOT NULL COMMENT '资产名称',
    asset_type VARCHAR(100) NOT NULL COMMENT '资产类型',
    business_importance VARCHAR(20) DEFAULT '1' COMMENT '业务重要性等级(1-3)',
    risk_score DOUBLE NOT NULL COMMENT '风险评分(0-100)',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级(1-4)',
    vulnerability_count INT NOT NULL COMMENT '漏洞数量',
    high_vulnerability_count INT NOT NULL COMMENT '高危漏洞数量',
    medium_vulnerability_count INT DEFAULT 0 COMMENT '中危漏洞数量',
    low_vulnerability_count INT DEFAULT 0 COMMENT '低危漏洞数量',
    threat_event_count INT DEFAULT 0 COMMENT '威胁事件数量',
    last_threat_time DATETIME COMMENT '最后威胁时间',
    asset_value_score DOUBLE NOT NULL COMMENT '资产价值评分',
    exposure_score DOUBLE NOT NULL COMMENT '暴露度评分',
    attack_surface VARCHAR(500) COMMENT '攻击面信息JSON',
    asset_ip VARCHAR(50) COMMENT '资产IP地址',
    asset_location VARCHAR(100) COMMENT '资产位置',
    asset_tags VARCHAR(200) COMMENT '资产标签',
    risk_trend VARCHAR(20) COMMENT '风险趋势',
    main_risk_source VARCHAR(100) COMMENT '主要风险来源',
    remediation_suggestion TEXT COMMENT '修复建议',
    responsible_person VARCHAR(50) COMMENT '责任人',
    security_status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '安全状态',
    last_scan_time DATETIME COMMENT '最后扫描时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_asset_id (asset_id),
    INDEX idx_asset_ip (asset_ip),
    INDEX idx_risk_score (risk_score),
    INDEX idx_risk_level (risk_level),
    INDEX idx_deleted (deleted),
    CONSTRAINT fk_asset_risk_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) COMMENT='资产风险表';

-- 创建攻击链表
-- 用途：存储攻击链分析数据，记录攻击过程中的各个阶段和技术
-- 说明：
-- - 基于MITRE ATT&CK框架设计的攻击链模型
-- - 外键关联到asset表，记录受影响资产
-- - 记录攻击阶段、技术ID、持续时间等信息
-- - 通过置信度评分评估攻击链识别的可靠性
-- - 支持按攻击阶段、目标资产等条件查询
CREATE TABLE IF NOT EXISTS attack_chain (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chain_id VARCHAR(100) NOT NULL COMMENT '攻击链ID',
    attack_phase VARCHAR(100) NOT NULL COMMENT '攻击阶段',
    phase_order INT NOT NULL COMMENT '阶段顺序',
    technique_id VARCHAR(50) COMMENT '技术ID',
    technique_name VARCHAR(255) COMMENT '技术名称',
    related_events VARCHAR(1000) COMMENT '关联事件ID列表(JSON)',
    source_ip VARCHAR(50) COMMENT '源IP',
    target_asset_id VARCHAR(100) COMMENT '目标资产ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '持续时间(秒)',
    confidence_score DOUBLE NOT NULL COMMENT '置信度分数(0-100)',
    severity VARCHAR(20) NOT NULL COMMENT '严重程度',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chain_id (chain_id),
    INDEX idx_attack_phase (attack_phase),
    INDEX idx_target_asset_id (target_asset_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status),
    CONSTRAINT fk_attack_chain_asset FOREIGN KEY (target_asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) COMMENT='攻击链表';

-- 创建网络流量数据表
-- 用途：存储网络流量数据，支持流量分析和异常检测
-- 说明：
-- - 记录源/目标IP、端口、协议等流量基本信息
-- - 统计流量字节数、包数、持续时间等指标
-- - 包含地理位置信息，支持威胁来源分析
-- - 标记异常流量，用于安全告警
-- - 外键关联到asset表，关联到受影响资产
CREATE TABLE IF NOT EXISTS network_traffic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_id VARCHAR(100) COMMENT '流量ID',
    timestamp DATETIME NOT NULL,
    source_ip VARCHAR(50) NOT NULL COMMENT '源IP',
    source_port INT COMMENT '源端口',
    destination_ip VARCHAR(50) NOT NULL COMMENT '目标IP',
    destination_port INT COMMENT '目标端口',
    protocol VARCHAR(20) NOT NULL COMMENT '协议',
    bytes_sent BIGINT DEFAULT 0 COMMENT '发送字节数',
    bytes_received BIGINT DEFAULT 0 COMMENT '接收字节数',
    packets_sent INT DEFAULT 0 COMMENT '发送包数',
    packets_received INT DEFAULT 0 COMMENT '接收包数',
    duration INT COMMENT '持续时间(秒)',
    flow_type VARCHAR(50) COMMENT '流量类型',
    flow_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '流量状态',
    risk_score DOUBLE DEFAULT 0 COMMENT '风险评分',
    source_country VARCHAR(50) COMMENT '源国家',
    source_city VARCHAR(50) COMMENT '源城市',
    destination_country VARCHAR(50) COMMENT '目标国家',
    destination_city VARCHAR(50) COMMENT '目标城市',
    application_protocol VARCHAR(50) COMMENT '应用协议',
    encryption_status VARCHAR(20) COMMENT '加密状态',
    anomaly_detected TINYINT DEFAULT 0 COMMENT '是否检测到异常',
    anomaly_type VARCHAR(100) COMMENT '异常类型',
    related_asset_id VARCHAR(100) COMMENT '关联资产ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_source_ip (source_ip),
    INDEX idx_destination_ip (destination_ip),
    INDEX idx_timestamp (timestamp),
    INDEX idx_risk_score (risk_score),
    INDEX idx_anomaly_detected (anomaly_detected),
    CONSTRAINT fk_network_traffic_asset FOREIGN KEY (related_asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) COMMENT='网络流量数据表';

-- 创建安全日志表
CREATE TABLE IF NOT EXISTS security_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    source_system VARCHAR(100) NOT NULL,
    log_type VARCHAR(50) NOT NULL,
    event_id VARCHAR(50),
    severity VARCHAR(20),
    message TEXT NOT NULL,
    source_ip VARCHAR(50),
    user_id VARCHAR(100),
    asset_id VARCHAR(100),
    action VARCHAR(100),
    result VARCHAR(50),
    risk_score DOUBLE DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_timestamp (timestamp),
    INDEX idx_source_system (source_system),
    INDEX idx_log_type (log_type),
    INDEX idx_severity (severity),
    INDEX idx_source_ip (source_ip),
    INDEX idx_asset_id (asset_id)
) COMMENT='安全日志表';

-- 创建漏洞详情表
-- 用途：存储资产漏洞信息，记录漏洞详情、严重程度和修复状态
-- 说明：
-- - 包含CVE编号、漏洞名称、描述等基本信息
-- - 记录严重程度、CVSS评分、影响范围等风险评估数据
-- - 跟踪漏洞发现时间和修复状态
-- - 提供修复建议，指导漏洞修复工作
-- - 外键关联到asset表，确保与资产数据一致性
CREATE TABLE IF NOT EXISTS vulnerability_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vuln_id VARCHAR(100) NOT NULL COMMENT '漏洞ID',
    cve_id VARCHAR(50) COMMENT 'CVE编号',
    asset_id VARCHAR(100) NOT NULL COMMENT '资产ID',
    vulnerability_type VARCHAR(100) NOT NULL COMMENT '漏洞类型',
    severity VARCHAR(20) NOT NULL COMMENT '严重程度',
    cvss_score DOUBLE NOT NULL COMMENT 'CVSS评分',
    description TEXT COMMENT '描述',
    impact TEXT COMMENT '影响',
    remediation TEXT COMMENT '修复建议',
    discovered_time DATETIME NOT NULL COMMENT '发现时间',
    patched_time DATETIME COMMENT '修复时间',
    status VARCHAR(20) DEFAULT 'OPEN' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vuln_id (vuln_id),
    INDEX idx_cve_id (cve_id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    CONSTRAINT fk_vulnerability_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) COMMENT='漏洞详情表';

-- 创建关联结果表
-- 用途：存储安全事件关联分析结果，识别多源数据中的关联关系
-- 说明：
-- - 记录关联事件的类型、置信度、发生时间等信息
-- - 存储关联规则ID和匹配的规则信息
-- - 分析关联事件的影响范围和严重程度
-- - 支持根因分析和事件溯源
-- - 外键关联到asset表，记录相关资产
CREATE TABLE IF NOT EXISTS correlation_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correlation_id VARCHAR(100) NOT NULL COMMENT '关联ID',
    event_ids VARCHAR(1000) NOT NULL COMMENT '事件ID列表(JSON)',
    correlation_type VARCHAR(50) NOT NULL COMMENT '关联类型',
    related_asset_id VARCHAR(100) COMMENT '相关资产ID',
    related_threat_id VARCHAR(100) COMMENT '相关威胁ID',
    confidence_score DOUBLE NOT NULL COMMENT '置信度分数(0-100)',
    severity VARCHAR(20) NOT NULL COMMENT '严重程度',
    summary TEXT COMMENT '关联概要',
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_correlation_id (correlation_id),
    INDEX idx_correlation_type (correlation_type),
    INDEX idx_related_asset_id (related_asset_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status),
    CONSTRAINT fk_correlation_asset FOREIGN KEY (related_asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) COMMENT='关联分析结果表';

-- 创建预测预警表
-- 用途：存储安全威胁预测和预警信息，基于历史数据和机器学习模型
-- 说明：
-- - 记录预测的威胁类型、风险等级、置信度等信息
-- - 分析可能的攻击目标、攻击时间和影响范围
-- - 提供预警描述和安全建议
-- - 跟踪预警的处理状态和响应措施
-- - 外键关联到asset表，记录相关资产
CREATE TABLE IF NOT EXISTS prediction_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(100) NOT NULL COMMENT '预警ID',
    alert_type VARCHAR(50) NOT NULL COMMENT '预警类型',
    alert_level VARCHAR(20) NOT NULL COMMENT '预警等级',
    predicted_time DATETIME NOT NULL COMMENT '预测时间',
    related_asset_id VARCHAR(100) COMMENT '相关资产ID',
    related_threat_type VARCHAR(100) COMMENT '相关威胁类型',
    prediction_confidence DOUBLE NOT NULL COMMENT '预测置信度(0-100)',
    description TEXT COMMENT '描述',
    recommendation TEXT COMMENT '建议措施',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_alert_id (alert_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_alert_level (alert_level),
    INDEX idx_predicted_time (predicted_time),
    INDEX idx_status (status),
    CONSTRAINT fk_prediction_asset FOREIGN KEY (related_asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) COMMENT='预测预警表';

-- 创建态势评估配置表
CREATE TABLE IF NOT EXISTS situation_evaluation_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    low_risk_threshold DOUBLE DEFAULT 30.0 COMMENT '低风险阈值',
    medium_risk_threshold DOUBLE DEFAULT 60.0 COMMENT '中风险阈值',
    high_risk_threshold DOUBLE DEFAULT 80.0 COMMENT '高风险阈值',
    real_time_calculation_enabled TINYINT DEFAULT 1 COMMENT '是否启用实时计算',
    data_aggregation_window INT DEFAULT 60 COMMENT '数据聚合窗口大小(秒)',
    threat_weight DOUBLE DEFAULT 0.4 COMMENT '威胁权重',
    vulnerability_weight DOUBLE DEFAULT 0.3 COMMENT '漏洞权重',
    asset_importance_weight DOUBLE DEFAULT 0.3 COMMENT '资产重要性权重',
    evaluation_interval INT DEFAULT 60 COMMENT '评估间隔(秒)',
    history_data_retention_days INT DEFAULT 30 COMMENT '历史数据保留天数',
    enable_alert TINYINT DEFAULT 1 COMMENT '是否启用告警',
    alert_threshold DOUBLE DEFAULT 70.0 COMMENT '告警阈值',
    config_description VARCHAR(500) COMMENT '配置描述',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='态势评估配置表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值(JSON)',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    description VARCHAR(255) COMMENT '描述',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key),
    INDEX idx_config_type (config_type),
    INDEX idx_status (status)
) COMMENT='系统配置表';

-- 插入默认态势评估配置
INSERT IGNORE INTO situation_evaluation_config (low_risk_threshold, medium_risk_threshold, high_risk_threshold, real_time_calculation_enabled, data_aggregation_window, threat_weight, vulnerability_weight, asset_importance_weight, evaluation_interval, history_data_retention_days, enable_alert, alert_threshold, config_description, status)
VALUES (30.0, 60.0, 80.0, 1, 60, 0.4, 0.3, 0.3, 60, 30, 1, 70.0, '默认态势评估配置', 'ACTIVE');

-- 插入系统默认配置 - 使用 INSERT IGNORE 避免重复键错误
INSERT IGNORE INTO system_config (config_key, config_value, config_type, description, status)
VALUES 
-- 基础配置
('system.name', '{"value": "安全态势感知系统"}', 'SYSTEM', '系统名称', 'ACTIVE'),
('system.version', '{"value": "1.0.0"}', 'SYSTEM', '系统版本', 'ACTIVE'),
('system.timezone', '{"value": "Asia/Shanghai"}', 'SYSTEM', '系统时区', 'ACTIVE'),
-- 数据采集配置
('data.collection.interval', '{"value": 60}', 'COLLECTION', '数据采集间隔(秒)', 'ACTIVE'),
('data.retention.period', '{"value": 90}', 'COLLECTION', '数据保留周期(天)', 'ACTIVE'),
-- 告警配置
('alert.enabled', '{"value": true}', 'ALERT', '告警功能开关', 'ACTIVE'),
('alert.notification.methods', '{"email": true, "sms": false, "webhook": true}', 'ALERT', '告警通知方式', 'ACTIVE'),
-- 预测分析配置
('prediction.analysis.enabled', '{"value": true}', 'PREDICTION', '预测分析功能开关', 'ACTIVE'),
('prediction.model.update.interval', '{"value": 86400}', 'PREDICTION', '预测模型更新间隔(秒)', 'ACTIVE'),
('prediction.confidence.threshold', '{"value": 65}', 'PREDICTION', '预测置信度阈值', 'ACTIVE');

-- 确保所有表和配置已正确创建
SELECT 'Database initialization completed successfully!' AS status;