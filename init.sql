CREATE DATABASE IF NOT EXISTS `aibank`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `aibank`;

CREATE TABLE IF NOT EXISTS sys_company (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  nickname VARCHAR(64) NOT NULL,
  password VARCHAR(120) NOT NULL,
  phone VARCHAR(32) NULL,
  email VARCHAR(128) NULL,
  avatar VARCHAR(255) NULL,
  company_id BIGINT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  theme VARCHAR(16) NOT NULL DEFAULT 'light',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  section VARCHAR(32) NOT NULL,
  path VARCHAR(128) NOT NULL UNIQUE,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sys_user_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_post (user_id, post_id),
  CONSTRAINT fk_user_post_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_post_post FOREIGN KEY (post_id) REFERENCES sys_post (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
  CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sys_agent_module (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  provider_code VARCHAR(32) NOT NULL,
  module_type VARCHAR(32) NOT NULL,
  base_model VARCHAR(128) NOT NULL,
  api_domain VARCHAR(255) NOT NULL,
  api_key VARCHAR(512) NOT NULL,
  remark VARCHAR(255) NULL,
  created_by VARCHAR(64) NOT NULL,
  updated_by VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_agent_module_name (name),
  KEY idx_agent_module_type (module_type)
);

CREATE TABLE IF NOT EXISTS sys_agent_module_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  agent_module_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_agent_module_role (agent_module_id, role_id),
  CONSTRAINT fk_agent_module_role_module FOREIGN KEY (agent_module_id) REFERENCES sys_agent_module (id) ON DELETE CASCADE,
  CONSTRAINT fk_agent_module_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sys_param_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  param_type VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  param_value TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_param_config_type (param_type)
);

INSERT INTO sys_company (id, name, code, status, remark, created_at, updated_at)
VALUES
  (1, '总部', 'HQ', 1, '系统默认所属公司', NOW(), NOW()),
  (2, '华东业务中心', 'EAST', 1, '示例公司数据，可按实际组织调整', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = NOW();

INSERT INTO sys_role (id, name, code, status, remark, created_at, updated_at)
VALUES
  (1, '超级管理员', 'ADMIN', 1, '系统内置管理员角色', NOW(), NOW()),
  (2, '运营', 'OPERATOR', 1, '负责日常运营配置', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = NOW();

INSERT INTO sys_post (id, name, code, status, remark, created_at, updated_at)
VALUES
  (1, '总经理', 'CEO', 1, '系统内置岗位', NOW(), NOW()),
  (2, '产品经理', 'PM', 1, '默认产品岗位', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = NOW();

INSERT INTO sys_user (id, username, nickname, password, phone, email, avatar, company_id, status, remark, created_at, updated_at)
VALUES
  (1, 'admin', '系统管理员', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
   '13800000000', 'admin@example.com', NULL, 1, 1, '默认管理员账号，初始密码：admin123', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  password = VALUES(password),
  phone = VALUES(phone),
  email = VALUES(email),
  company_id = VALUES(company_id),
  status = VALUES(status),
  remark = VALUES(remark),
  theme = 'light',
  updated_at = NOW();

INSERT INTO sys_menu (id, name, code, section, path, sort_order, status, remark, created_at, updated_at)
VALUES
  (1, 'AI工作台', 'CHAT_WORKBENCH', 'ai', '/chat', 10, 1, '默认 AI 工作入口', NOW(), NOW()),
  (2, '智能体配置', 'AGENT_CONFIG', 'ai', '/agents', 20, 1, '智能体配置页面', NOW(), NOW()),
  (17, '参数配置', 'PARAM_CONFIG', 'ai', '/params', 30, 1, 'AI 参数配置页面', NOW(), NOW()),
  (3, '产品库', 'KB_PRODUCTS', 'knowledge', '/knowledge/products', 10, 1, '产品知识库入口', NOW(), NOW()),
  (4, '企业画像', 'KB_PORTRAITS', 'knowledge', '/knowledge/portraits', 20, 1, '企业画像入口', NOW(), NOW()),
  (5, '行业动态', 'KB_TRENDS', 'knowledge', '/knowledge/trends', 30, 1, '行业动态入口', NOW(), NOW()),
  (6, 'Badcase', 'LOG_BADCASE', 'logs', '/logs/badcase', 10, 1, 'Badcase 日志页', NOW(), NOW()),
  (7, '观测认证', 'LOG_OBSERVATION_AUTH', 'logs', '/logs/observation-auth', 20, 1, '观测认证日志页', NOW(), NOW()),
  (8, '回归评测', 'LOG_REGRESSION_REVIEW', 'logs', '/logs/regression-review', 30, 1, '回归评测页', NOW(), NOW()),
  (9, '修复队列', 'LOG_FIX_QUEUE', 'logs', '/logs/fix-queue', 40, 1, '修复队列页', NOW(), NOW()),
  (10, '规则库', 'LOG_RULE_LIBRARY', 'logs', '/logs/rule-library', 50, 1, '规则库页', NOW(), NOW()),
  (11, '说明', 'LOG_INSTRUCTIONS', 'logs', '/logs/instructions', 60, 1, '日志说明页', NOW(), NOW()),
  (12, '角色管理', 'ROLE_MANAGE', 'permission', '/roles', 10, 1, '角色权限管理', NOW(), NOW()),
  (13, '用户管理', 'USER_MANAGE', 'permission', '/users', 20, 1, '用户权限管理', NOW(), NOW()),
  (14, '岗位管理', 'POST_MANAGE', 'permission', '/posts', 30, 1, '岗位权限管理', NOW(), NOW()),
  (15, '菜单管理', 'MENU_MANAGE', 'permission', '/menus', 40, 1, '菜单可见范围管理', NOW(), NOW()),
  (16, '所属公司', 'COMPANY_MANAGE', 'permission', '/companies', 50, 1, '用户所属公司管理', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  section = VALUES(section),
  path = VALUES(path),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = NOW();

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);
INSERT IGNORE INTO sys_user_post (user_id, post_id) VALUES (1, 1);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
VALUES
  (1, 1), (1, 2), (1, 17), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
  (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
  (2, 1), (2, 2), (2, 17), (2, 3), (2, 4), (2, 5), (2, 11);
