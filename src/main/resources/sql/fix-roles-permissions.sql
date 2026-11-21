USE security_defense;

-- 修复角色和权限数据
-- 确保角色编码与 system-manage.sql 一致

-- 1. 清理并重新插入角色数据（使用与 system-manage.sql 一致的编码）
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_role;

INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '系统管理员', 'admin', '系统管理员，拥有所有权限', 1),
(2, '安全管理员', 'security', '安全管理员，负责安全策略和事件管理', 1),
(3, '审计员', 'auditor', '审计员，只能查看日志和报表', 1),
(4, '普通用户', 'user', '普通用户，只有查看权限', 1);

-- 2. 重新分配用户角色
-- admin (ID=1) -> admin角色 (ID=1)
-- operator (ID=2) -> security角色 (ID=2)
-- viewer (ID=3) -> user角色 (ID=4)
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> admin
(2, 2),  -- operator -> security
(3, 4);  -- viewer -> user

-- 3. 为管理员角色分配所有权限
-- 首先删除现有的角色权限关联
TRUNCATE TABLE sys_role_permission;

-- 为管理员角色（ID=1）分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0 AND status = 1;

-- 为安全管理员角色（ID=2）分配部分权限（除系统管理外的所有权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE deleted = 0 AND status = 1 AND permission_code NOT LIKE 'system:%';

-- 为审计员角色（ID=3）分配只读权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE deleted = 0 AND status = 1 AND permission_code LIKE '%:view';

-- 为普通用户角色（ID=4）分配基础查看权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission WHERE deleted = 0 AND status = 1 AND permission_code IN ('system:user:view', 'system:role:view', 'system:permission:view');

-- 4. 验证数据
SELECT '角色数据:' as info, * FROM sys_role;
SELECT '用户角色关联:' as info, * FROM sys_user_role;
SELECT '角色权限关联:' as info, * FROM sys_role_permission LIMIT 10;
SELECT '权限数据:' as info, COUNT(*) as total_permissions FROM sys_permission WHERE deleted = 0 AND status = 1;
