-- 更新操作日志表，添加缺失的字段
-- 该脚本用于解决 "Unknown column 'operation_user' in 'field list'" 错误

-- 添加 operation_user 字段
ALTER TABLE sys_operation_log ADD COLUMN operation_user VARCHAR(50) COMMENT '操作用户';

-- 添加 operation_module 字段
ALTER TABLE sys_operation_log ADD COLUMN operation_module VARCHAR(100) COMMENT '操作模块';

-- 添加 operation_type 字段
ALTER TABLE sys_operation_log ADD COLUMN operation_type VARCHAR(50) COMMENT '操作类型';

-- 添加 operation_content 字段
ALTER TABLE sys_operation_log ADD COLUMN operation_content TEXT COMMENT '操作内容';

-- 添加 operation_ip 字段
ALTER TABLE sys_operation_log ADD COLUMN operation_ip VARCHAR(50) COMMENT '操作IP';

-- 添加 operation_result 字段
ALTER TABLE sys_operation_log ADD COLUMN operation_result VARCHAR(20) COMMENT '操作结果';