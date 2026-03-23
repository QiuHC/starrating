CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    display_name VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    shop_code VARCHAR(50),
    shop_name VARCHAR(100),
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    role_scope VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_code VARCHAR(50) NOT NULL UNIQUE,
    permission_name VARCHAR(100) NOT NULL,
    permission_group VARCHAR(50) NOT NULL,
    permission_scope VARCHAR(20) NOT NULL,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS star_registration (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    shop_code VARCHAR(50) NOT NULL,
    shop_name VARCHAR(100) NOT NULL,
    target_star VARCHAR(20),
    quarter VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    payment_url VARCHAR(255),
    canopy_url VARCHAR(255),
    reject_reason TEXT,
    submit_time TIMESTAMPTZ,
    audit_time TIMESTAMPTZ,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(200),
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sys_config (config_key, config_value, description)
VALUES ('registration_period', '{"startTime": "2024-01-01", "endTime": "2026-12-31"}', '报名申请周期设置')
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO sys_permission (permission_code, permission_name, permission_group, permission_scope) VALUES
('dashboard', '首页看板', '通用模块', 'FACTORY'),
('account_manage', '账号管理', '系统管理', 'FACTORY'),
('role_manage', '角色管理', '系统管理', 'FACTORY'),
('registration_manage', '申报管理', '业务管理', 'FACTORY'),
('config_manage', '周期配置', '业务管理', 'FACTORY'),
('shop_portal', '店端门户', '店端模块', 'SHOP')
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_role (role_code, role_name, role_scope, status, is_system) VALUES
('SUPER_ADMIN', '超级管理员', 'FACTORY', 'ACTIVE', TRUE),
('FACTORY_OPERATOR', '厂端运营', 'FACTORY', 'ACTIVE', TRUE),
('SHOP_ACCOUNT', '店端账号', 'SHOP', 'ACTIVE', TRUE)
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO sys_user (username, password, email, display_name, user_type, status, shop_code, shop_name) VALUES
('6666666', '$2a$10$1horPvg321thwVzjgh/llurbKHSljRfzJ/.zwE.ZRaw0E2RR9r9a.', 'admin@starrating.local', '厂家管理员', 'FACTORY', 'ACTIVE', NULL, NULL),
('BYDGD100W', '$2a$10$1horPvg321thwVzjgh/llurbKHSljRfzJ/.zwE.ZRaw0E2RR9r9a.', 'shop@starrating.local', '比亚迪广东店', 'SHOP', 'ACTIVE', 'BYDGD100W', '比亚迪广东店')
ON CONFLICT (username) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'SUPER_ADMIN'
WHERE u.username = '6666666'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'SHOP_ACCOUNT'
WHERE u.username = 'BYDGD100W'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('dashboard', 'account_manage', 'role_manage', 'registration_manage', 'config_manage')
WHERE r.role_code = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('dashboard', 'registration_manage', 'config_manage')
WHERE r.role_code = 'FACTORY_OPERATOR'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code = 'shop_portal'
WHERE r.role_code = 'SHOP_ACCOUNT'
ON CONFLICT (role_id, permission_id) DO NOTHING;
