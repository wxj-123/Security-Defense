# 数据库初始化指南

## 1. 数据库准备

### 1.1 安装 MySQL
确保已安装 MySQL 8.0 或更高版本。

### 1.2 创建数据库和导入数据

#### 方式一：使用 SQL 文件初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 执行建表脚本
source d:/dask/Security-Defense/src/main/resources/sql/schema.sql

# 3. 执行初始数据脚本
source d:/dask/Security-Defense/src/main/resources/sql/init-data.sql
```

#### 方式二：使用命令行直接导入

```bash
# Windows PowerShell
mysql -u root -p < d:\dask\Security-Defense\src\main\resources\sql\schema.sql
mysql -u root -p < d:\dask\Security-Defense\src\main\resources\sql\init-data.sql
```

## 2. Redis 准备

### 2.1 安装 Redis
- Windows: 下载并安装 Redis for Windows
- 启动 Redis 服务: `redis-server`

### 2.2 验证 Redis 连接
```bash
redis-cli ping
# 应该返回 PONG
```

## 3. 配置文件说明

### 3.1 数据库配置
配置文件位置: `src/main/resources/application.properties`

```properties
# 数据库连接配置
spring.datasource.url=jdbc:mysql://localhost:3306/security_defense
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}  # 由环境变量注入
```

### 3.2 Redis 配置
```properties
# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=  # 如果设置了密码，请在此配置
```

## 4. 数据库表说明

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 用户表 | id, username, password, email, phone |
| sys_role | 角色表 | id, role_name, role_code |
| sys_user_role | 用户角色关联表 | user_id, role_id |
| security_event | 安全事件表 | event_type, event_level, source_ip, target_ip |
| firewall_rule | 防火墙规则表 | rule_name, rule_type, source_ip, protocol |
| access_control_list | 访问控制列表 | list_type, ip_address |
| network_flow_log | 网络流量日志 | source_ip, target_ip, bytes_sent |
| sys_operation_log | 系统操作日志 | user_id, operation, method |
| threat_intelligence | 威胁情报表 | threat_type, indicator_type, indicator_value |
| policy_template | 策略模板表 | template_name, template_type, template_content |
| approval_workflow | 审批工作流表 | workflow_name, status, initiator_id |
| approval_step | 审批步骤记录表 | workflow_id, approver_id, approval_status |

## 5. 默认账户

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 首次启动后请自行修改 | 系统管理员 | 拥有所有权限 |
| operator | 首次启动后请自行修改 | 安全运维 | 负责安全策略配置和事件处理 |
| viewer | 首次启动后请自行修改 | 访客 | 只能查看基本信息 |

## 6. 验证安装

启动应用后，可以通过以下方式验证：

### 6.1 访问 Druid 监控
- URL: http://localhost:8085/druid
- 用户名: admin
- 密码: admin

### 6.2 检查数据库连接
查看应用日志，确认没有数据库连接错误。

## 7. 常见问题

### 7.1 连接被拒绝
- 检查 MySQL 服务是否启动
- 检查端口 3306 是否被占用
- 检查防火墙设置

### 7.2 认证失败
- 确认数据库用户名密码正确
- MySQL 8.0+ 可能需要修改认证插件:
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_password';
FLUSH PRIVILEGES;
```

### 7.3 时区问题
确保在连接URL中配置了时区参数：
```
serverTimezone=Asia/Shanghai
```
