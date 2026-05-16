-- ============================================================
-- 炉批卷追溯与报工入库对账系统 - 数据库初始化脚本
-- 数据库: trace_db
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS trace_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE trace_db;

-- ============================================================
-- 1. 炉次表
-- ============================================================
DROP TABLE IF EXISTS tr_heat;
CREATE TABLE tr_heat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    heat_id VARCHAR(50) NOT NULL COMMENT '炉号',
    smelt_date DATE NOT NULL COMMENT '冶炼日期',
    steel_grade VARCHAR(100) COMMENT '钢种',
    c_content DECIMAL(10,4) COMMENT '碳含量(%)',
    si_content DECIMAL(10,4) COMMENT '硅含量(%)',
    mn_content DECIMAL(10,4) COMMENT '锰含量(%)',
    p_content DECIMAL(10,4) COMMENT '磷含量(%)',
    s_content DECIMAL(10,4) COMMENT '硫含量(%)',
    cast_start_time DATETIME COMMENT '开浇时间',
    shift_group VARCHAR(50) COMMENT '冶炼班组',
    equipment_id VARCHAR(50) COMMENT '设备编号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-正常 1-异常',
    abnormal_desc VARCHAR(500) COMMENT '异常说明',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_heat_id (heat_id),
    INDEX idx_smelt_date (smelt_date),
    INDEX idx_steel_grade (steel_grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='炉次表';

-- ============================================================
-- 2. 铸坯表
-- ============================================================
DROP TABLE IF EXISTS tr_slab;
CREATE TABLE tr_slab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slab_id VARCHAR(50) NOT NULL COMMENT '铸坯号',
    heat_id VARCHAR(50) NOT NULL COMMENT '关联炉号',
    specifications VARCHAR(100) COMMENT '规格',
    weight DECIMAL(12,3) COMMENT '重量(吨)',
    cast_shift VARCHAR(50) COMMENT '连铸班组',
    cast_time DATETIME COMMENT '连铸时间',
    roll_batch_id VARCHAR(50) COMMENT '去向(轧制批次号)',
    quality_status TINYINT DEFAULT 0 COMMENT '0-待检 1-合格 2-不合格 3-改判',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_slab_id (slab_id),
    INDEX idx_heat_id (heat_id),
    INDEX idx_roll_batch_id (roll_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='铸坯表';

-- ============================================================
-- 3. 轧制批次表
-- ============================================================
DROP TABLE IF EXISTS tr_roll_batch;
CREATE TABLE tr_roll_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL COMMENT '批次号',
    production_line VARCHAR(50) COMMENT '产线',
    work_order_id VARCHAR(50) COMMENT '工单编号',
    roll_date DATE COMMENT '轧制日期',
    shift_group VARCHAR(50) COMMENT '班组',
    status TINYINT DEFAULT 0 COMMENT '0-正常 1-异常 2-已合并 3-已拆分',
    abnormal_desc VARCHAR(500) COMMENT '异常说明',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_batch_id (batch_id),
    INDEX idx_roll_date (roll_date),
    INDEX idx_work_order_id (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轧制批次表';

-- ============================================================
-- 4. 钢卷档案表
-- ============================================================
DROP TABLE IF EXISTS tr_coil;
CREATE TABLE tr_coil (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coil_id VARCHAR(50) NOT NULL COMMENT '卷号',
    specifications VARCHAR(100) COMMENT '规格',
    weight DECIMAL(12,3) COMMENT '重量(吨)',
    material VARCHAR(50) COMMENT '材质',
    quality_grade VARCHAR(50) COMMENT '质量等级',
    batch_id VARCHAR(50) COMMENT '关联轧制批次',
    inbound_order_no VARCHAR(50) COMMENT '入库单号',
    storage_location VARCHAR(100) COMMENT '库存位置',
    stock_status TINYINT DEFAULT 0 COMMENT '0-在库 1-已出库 2-锁定',
    outbound_order_no VARCHAR(50) COMMENT '出库单号',
    customer_id VARCHAR(50) COMMENT '客户编号',
    customer_name VARCHAR(200) COMMENT '客户名称',
    lifecycle_status TINYINT DEFAULT 0 COMMENT '0-生产 1-质检 2-入库 3-出库 4-异议',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_coil_id (coil_id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_stock_status (stock_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钢卷档案表';

-- ============================================================
-- 5. 质检记录表
-- ============================================================
DROP TABLE IF EXISTS tr_qc_record;
CREATE TABLE tr_qc_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    relate_type TINYINT NOT NULL COMMENT '关联类型 1-炉次 2-铸坯 3-钢卷',
    relate_id VARCHAR(50) NOT NULL COMMENT '关联编号',
    inspect_item VARCHAR(100) COMMENT '检验项目',
    inspect_value VARCHAR(200) COMMENT '检验值',
    standard_value VARCHAR(200) COMMENT '标准值',
    result TINYINT NOT NULL COMMENT '判定 1-合格 2-不合格 3-让步接收',
    inspect_time DATETIME COMMENT '检验时间',
    inspector VARCHAR(50) COMMENT '检验人',
    fail_reason VARCHAR(500) COMMENT '不合格原因',
    dispose_method VARCHAR(200) COMMENT '处置方式',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_relate (relate_type, relate_id),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检记录表';

-- ============================================================
-- 6. 报工记录表
-- ============================================================
DROP TABLE IF EXISTS tr_work_report;
CREATE TABLE tr_work_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id VARCHAR(50) NOT NULL COMMENT '工单编号',
    process_name VARCHAR(100) COMMENT '工序名称',
    batch_id VARCHAR(50) COMMENT '批次号',
    coil_id VARCHAR(50) COMMENT '卷号',
    report_quantity DECIMAL(12,3) COMMENT '报工数量',
    report_time DATETIME COMMENT '报工时间',
    operator VARCHAR(50) COMMENT '操作人',
    shift_group VARCHAR(50) COMMENT '班组',
    approve_status TINYINT DEFAULT 0 COMMENT '0-待审 1-通过 2-驳回',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_work_order_id (work_order_id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_coil_id (coil_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报工记录表';

-- ============================================================
-- 7. 入库出库表
-- ============================================================
DROP TABLE IF EXISTS tr_inventory;
CREATE TABLE tr_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_no VARCHAR(50) NOT NULL COMMENT '单据编号',
    doc_type TINYINT NOT NULL COMMENT '类型 1-入库 2-出库',
    batch_id VARCHAR(50) COMMENT '批次号',
    coil_id VARCHAR(50) COMMENT '卷号',
    quantity DECIMAL(12,3) COMMENT '数量',
    operate_time DATETIME COMMENT '操作时间',
    warehouse VARCHAR(100) COMMENT '仓库',
    operator VARCHAR(50) COMMENT '操作人',
    status TINYINT DEFAULT 0 COMMENT '0-待审 1-已审 2-作废',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_doc_no (doc_no),
    INDEX idx_doc_type (doc_type),
    INDEX idx_coil_id (coil_id),
    INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库出库表';

-- ============================================================
-- 8. 库存台账表
-- ============================================================
DROP TABLE IF EXISTS tr_stock;
CREATE TABLE tr_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coil_id VARCHAR(50) NOT NULL COMMENT '卷号',
    warehouse VARCHAR(100) COMMENT '仓库',
    location VARCHAR(100) COMMENT '库位',
    quantity DECIMAL(12,3) COMMENT '库存数量',
    stock_status TINYINT DEFAULT 0 COMMENT '0-正常 1-冻结 2-已出库',
    version INT DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_coil_wh (coil_id, warehouse)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存台账表';

-- ============================================================
-- 9. 对账差异表
-- ============================================================
DROP TABLE IF EXISTS tr_recon_diff;
CREATE TABLE tr_recon_diff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(50) COMMENT '批次号',
    coil_id VARCHAR(50) COMMENT '卷号',
    work_report_qty DECIMAL(12,3) COMMENT 'MES报工数量',
    qc_pass_qty DECIMAL(12,3) COMMENT '质检合格数量',
    erp_inbound_qty DECIMAL(12,3) COMMENT 'ERP入库数量',
    stock_qty DECIMAL(12,3) COMMENT '库存台账数量',
    diff_type TINYINT NOT NULL COMMENT '差异类型 1-数量不符 2-数据缺失',
    description VARCHAR(500) COMMENT '差异描述',
    responsible_dept VARCHAR(100) COMMENT '责任部门',
    status TINYINT DEFAULT 0 COMMENT '0-未处理 1-处理中 2-已关闭',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_batch_id (batch_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账差异表';

-- ============================================================
-- 10. 断链诊断记录表
-- ============================================================
DROP TABLE IF EXISTS tr_chain_break;
CREATE TABLE tr_chain_break (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    break_type TINYINT NOT NULL COMMENT '断链类型 1-批次断链 2-质检缺失 3-未质检入库 4-MES/ERP不一致 5-重复报工 6-数量异常',
    heat_id VARCHAR(50) COMMENT '炉号',
    slab_id VARCHAR(50) COMMENT '铸坯号',
    batch_id VARCHAR(50) COMMENT '批次号',
    coil_id VARCHAR(50) COMMENT '卷号',
    break_desc VARCHAR(500) COMMENT '断链描述',
    risk_level TINYINT COMMENT '风险等级 1-低 2-中 3-高 4-严重',
    responsible_dept VARCHAR(100) COMMENT '责任部门',
    status TINYINT DEFAULT 0 COMMENT '0-待处理 1-处理中 2-已关闭',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_break_type (break_type),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='断链诊断记录表';

-- ============================================================
-- 11. 质量异议表
-- ============================================================
DROP TABLE IF EXISTS tr_complaint;
CREATE TABLE tr_complaint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(50) NOT NULL COMMENT '投诉单号',
    customer_id VARCHAR(50) COMMENT '客户编号',
    customer_name VARCHAR(200) COMMENT '客户名称',
    coil_id VARCHAR(50) COMMENT '关联卷号',
    problem_desc TEXT COMMENT '问题描述',
    severity TINYINT COMMENT '严重度 1-轻微 2-一般 3-严重 4-重大',
    responsible_dept VARCHAR(100) COMMENT '责任部门',
    root_cause TEXT COMMENT '原因分析',
    corrective_measures TEXT COMMENT '处理措施',
    rectification_result TEXT COMMENT '整改结果',
    status TINYINT DEFAULT 0 COMMENT '0-登记 1-追溯中 2-处理中 3-复检中 4-已关闭 5-已归档',
    close_time DATETIME COMMENT '关闭时间',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_complaint_id (complaint_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_coil_id (coil_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量异议表';

-- ============================================================
-- 12. 追溯链路快照表
-- ============================================================
DROP TABLE IF EXISTS tr_trace_link;
CREATE TABLE tr_trace_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    link_id VARCHAR(50) NOT NULL COMMENT '链路ID',
    source_type TINYINT NOT NULL COMMENT '类型 1-正向 2-反向',
    input_value VARCHAR(100) NOT NULL COMMENT '输入值',
    link_data JSON NOT NULL COMMENT '链路数据(全节点JSON)',
    node_count INT COMMENT '节点数',
    abnormal_node_count INT COMMENT '异常节点数',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_link_id (link_id),
    INDEX idx_input_value (input_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追溯链路快照表';

-- ============================================================
-- 13. 规则配置表
-- ============================================================
DROP TABLE IF EXISTS tr_rule_config;
CREATE TABLE tr_rule_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(100) COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '类型 1-追溯 2-对账 3-断链',
    rule_content JSON COMMENT '规则内容',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则配置表';

-- ============================================================
-- 14. 用户表
-- ============================================================
DROP TABLE IF EXISTS tr_user;
CREATE TABLE tr_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    department VARCHAR(100),
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 15. 角色表
-- ============================================================
DROP TABLE IF EXISTS tr_role;
CREATE TABLE tr_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100),
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================================
-- 16. 用户角色关联表
-- ============================================================
DROP TABLE IF EXISTS tr_user_role;
CREATE TABLE tr_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================================
-- 17. 角色菜单关联表
-- ============================================================
DROP TABLE IF EXISTS tr_role_menu;
CREATE TABLE tr_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================================
-- 18. 菜单权限表
-- ============================================================
DROP TABLE IF EXISTS tr_menu;
CREATE TABLE tr_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(100),
    parent_id BIGINT,
    path VARCHAR(200),
    permission_code VARCHAR(100),
    sort_order INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ============================================================
-- 18. 操作日志表
-- ============================================================
DROP TABLE IF EXISTS tr_operation_log;
CREATE TABLE tr_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) COMMENT '操作人',
    ip VARCHAR(50) COMMENT 'IP地址',
    module VARCHAR(100) COMMENT '操作模块',
    action VARCHAR(100) COMMENT '操作类型',
    description VARCHAR(500) COMMENT '操作描述',
    request_url VARCHAR(200) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    execute_time BIGINT COMMENT '执行耗时(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态 1-成功 0-失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 模拟数据
-- ============================================================

-- ---- 用户 ----
INSERT INTO tr_user (username, password, real_name, department, status) VALUES
('admin', '$2a$10$RuCtZujFJ3aW1yMUg6duwO0epnW6JdK9HXznVWOnEW9akV7OiqO92', '系统管理员', '信息部', 1),
('zhangsan', '$2a$10$RuCtZujFJ3aW1yMUg6duwO0epnW6JdK9HXznVWOnEW9akV7OiqO92', '张三', '质量管理部', 1),
('lisi', '$2a$10$RuCtZujFJ3aW1yMUg6duwO0epnW6JdK9HXznVWOnEW9akV7OiqO92', '李四', '生产车间', 1);
-- 密码均为: 123456 (BCrypt加密)

INSERT INTO tr_role (role_code, role_name) VALUES
('ROLE_ADMIN', '系统管理员'),
('ROLE_QA', '质量管理员'),
('ROLE_PRODUCER', '生产操作员');

INSERT INTO tr_user_role (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3);

-- ---- 炉次 (20炉) ----
INSERT INTO tr_heat (heat_id, smelt_date, steel_grade, c_content, si_content, mn_content, p_content, s_content, cast_start_time, shift_group, equipment_id, status) VALUES
('H202601-001', '2026-01-05', 'Q235B', 0.1800, 0.2500, 0.5500, 0.0180, 0.0120, '2026-01-05 08:30:00', '甲班', 'LF-01', 0),
('H202601-002', '2026-01-08', 'Q345B', 0.2000, 0.3000, 1.2000, 0.0200, 0.0100, '2026-01-08 14:00:00', '乙班', 'LF-02', 0),
('H202601-003', '2026-01-12', 'Q235B', 0.1700, 0.2200, 0.5000, 0.0150, 0.0110, '2026-01-12 22:00:00', '丙班', 'LF-01', 0),
('H202601-004', '2026-01-15', 'SPHC', 0.0800, 0.1000, 0.3500, 0.0120, 0.0080, '2026-01-15 06:00:00', '甲班', 'LF-03', 0),
('H202601-005', '2026-01-18', 'Q345B', 0.2100, 0.3200, 1.2500, 0.0190, 0.0090, '2026-01-18 10:00:00', '乙班', 'LF-02', 0),
('H202602-001', '2026-02-01', 'Q235B', 0.1800, 0.2400, 0.5200, 0.0160, 0.0100, '2026-02-01 08:00:00', '甲班', 'LF-01', 0),
('H202602-002', '2026-02-05', 'Q345B', 0.1900, 0.2800, 1.1500, 0.0210, 0.0110, '2026-02-05 16:00:00', '丙班', 'LF-03', 0),
('H202602-003', '2026-02-10', 'SPHC', 0.0700, 0.0900, 0.3200, 0.0110, 0.0070, '2026-02-10 07:30:00', '甲班', 'LF-02', 0),
('H202602-004', '2026-02-15', 'Q235B', 0.1750, 0.2300, 0.5100, 0.0170, 0.0130, '2026-02-15 12:00:00', '乙班', 'LF-01', 0),
('H202603-001', '2026-03-02', 'Q345B', 0.2050, 0.3100, 1.1800, 0.0180, 0.0090, '2026-03-02 05:00:00', '甲班', 'LF-03', 0),
('H202603-002', '2026-03-08', 'Q235B', 0.1600, 0.2100, 0.4800, 0.0140, 0.0120, '2026-03-08 19:00:00', '丙班', 'LF-01', 0),
('H202603-003', '2026-03-12', 'SPHC', 0.0850, 0.1100, 0.3600, 0.0130, 0.0080, '2026-03-12 11:00:00', '乙班', 'LF-02', 0),
('H202603-004', '2026-03-18', 'Q345B', 0.1950, 0.2900, 1.2200, 0.0200, 0.0100, '2026-03-18 15:00:00', '甲班', 'LF-03', 1),
('H202603-005', '2026-03-22', 'Q235B', 0.1650, 0.2200, 0.4900, 0.0160, 0.0110, '2026-03-22 09:00:00', '乙班', 'LF-01', 0),
('H202604-001', '2026-04-01', 'Q345B', 0.2000, 0.3000, 1.2000, 0.0180, 0.0100, '2026-04-01 08:00:00', '甲班', 'LF-02', 0),
('H202604-002', '2026-04-05', 'SPHC', 0.0750, 0.0950, 0.3300, 0.0120, 0.0070, '2026-04-05 14:00:00', '丙班', 'LF-03', 0),
('H202604-003', '2026-04-10', 'Q235B', 0.1850, 0.2600, 0.5300, 0.0150, 0.0120, '2026-04-10 20:00:00', '甲班', 'LF-01', 0),
('H202604-004', '2026-04-15', 'Q345B', 0.2100, 0.3300, 1.2600, 0.0220, 0.0110, '2026-04-15 06:30:00', '乙班', 'LF-02', 0),
('H202604-005', '2026-04-20', 'Q235B', 0.1700, 0.2300, 0.5100, 0.0170, 0.0130, '2026-04-20 10:00:00', '丙班', 'LF-01', 0),
('H202604-006', '2026-04-25', 'SPHC', 0.0800, 0.1050, 0.3400, 0.0110, 0.0080, '2026-04-25 16:00:00', '甲班', 'LF-03', 0);

-- ---- 铸坯 (60+条) ----
INSERT INTO tr_slab (slab_id, heat_id, specifications, weight, cast_shift, cast_time, roll_batch_id, quality_status) VALUES
('S202601-001', 'H202601-001', '200x1200mm', 25.500, '甲班', '2026-01-05 10:30:00', 'B202601-001', 1),
('S202601-002', 'H202601-001', '200x1200mm', 25.800, '甲班', '2026-01-05 10:45:00', 'B202601-001', 1),
('S202601-003', 'H202601-001', '200x1200mm', 25.200, '甲班', '2026-01-05 11:00:00', 'B202601-001', 1),
('S202601-004', 'H202601-002', '200x1500mm', 30.100, '乙班', '2026-01-08 15:30:00', 'B202601-002', 1),
('S202601-005', 'H202601-002', '200x1500mm', 30.500, '乙班', '2026-01-08 15:50:00', 'B202601-002', 1),
('S202601-006', 'H202601-002', '200x1500mm', 29.800, '乙班', '2026-01-08 16:10:00', 'B202601-002', 1),
('S202601-007', 'H202601-003', '200x1200mm', 24.900, '丙班', '2026-01-12 23:30:00', 'B202601-003', 1),
('S202601-008', 'H202601-003', '200x1200mm', 25.100, '丙班', '2026-01-12 23:45:00', 'B202601-003', 2),
('S202601-009', 'H202601-003', '200x1200mm', 25.400, '丙班', '2026-01-13 00:00:00', 'B202601-003', 1),
('S202601-010', 'H202601-004', '180x1250mm', 22.300, '甲班', '2026-01-15 07:30:00', 'B202601-004', 1),
('S202601-011', 'H202601-004', '180x1250mm', 22.100, '甲班', '2026-01-15 07:50:00', 'B202601-004', 3),
('S202601-012', 'H202601-005', '200x1500mm', 31.200, '乙班', '2026-01-18 11:30:00', 'B202601-005', 1),
('S202601-013', 'H202601-005', '200x1500mm', 30.900, '乙班', '2026-01-18 11:50:00', 'B202601-005', 1),
('S202602-001', 'H202602-001', '200x1200mm', 25.600, '甲班', '2026-02-01 09:30:00', 'B202602-001', 1),
('S202602-002', 'H202602-001', '200x1200mm', 25.000, '甲班', '2026-02-01 09:50:00', 'B202602-001', 1),
('S202602-003', 'H202602-002', '200x1500mm', 30.300, '丙班', '2026-02-05 17:30:00', 'B202602-002', 1),
('S202602-004', 'H202602-002', '200x1500mm', 30.700, '丙班', '2026-02-05 17:50:00', 'B202602-002', 1),
('S202602-005', 'H202602-003', '180x1250mm', 22.500, '甲班', '2026-02-10 08:30:00', 'B202602-003', 1),
('S202602-006', 'H202602-003', '180x1250mm', 22.800, '甲班', '2026-02-10 08:50:00', 'B202602-003', 1),
('S202602-007', 'H202602-004', '200x1200mm', 25.300, '乙班', '2026-02-15 13:30:00', 'B202602-004', 1),
('S202602-008', 'H202602-004', '200x1200mm', 25.700, '乙班', '2026-02-15 13:50:00', 'B202602-004', 1),
('S202603-001', 'H202603-001', '200x1500mm', 31.500, '甲班', '2026-03-02 06:30:00', 'B202603-001', 1),
('S202603-002', 'H202603-001', '200x1500mm', 31.000, '甲班', '2026-03-02 06:50:00', 'B202603-001', 1),
('S202603-003', 'H202603-002', '200x1200mm', 24.800, '丙班', '2026-03-08 20:30:00', 'B202603-002', 1),
('S202603-004', 'H202603-002', '200x1200mm', 25.100, '丙班', '2026-03-08 20:50:00', 'B202603-002', 1),
('S202603-005', 'H202603-003', '180x1250mm', 22.600, '乙班', '2026-03-12 12:30:00', 'B202603-003', 1),
('S202603-006', 'H202603-003', '180x1250mm', 22.900, '乙班', '2026-03-12 12:50:00', 'B202603-003', 1),
('S202603-007', 'H202603-004', '200x1500mm', 30.800, '甲班', '2026-03-18 16:30:00', 'B202603-004', 2),
('S202603-008', 'H202603-004', '200x1500mm', 31.100, '甲班', '2026-03-18 16:50:00', 'B202603-004', 1),
('S202603-009', 'H202603-005', '200x1200mm', 25.400, '乙班', '2026-03-22 10:30:00', 'B202603-005', 1),
('S202603-010', 'H202603-005', '200x1200mm', 25.000, '乙班', '2026-03-22 10:50:00', 'B202603-005', 1),
('S202604-001', 'H202604-001', '200x1500mm', 31.200, '甲班', '2026-04-01 09:30:00', 'B202604-001', 1),
('S202604-002', 'H202604-001', '200x1500mm', 30.600, '甲班', '2026-04-01 09:50:00', 'B202604-001', 1),
('S202604-003', 'H202604-002', '180x1250mm', 22.400, '丙班', '2026-04-05 15:30:00', 'B202604-002', 1),
('S202604-004', 'H202604-002', '180x1250mm', 22.700, '丙班', '2026-04-05 15:50:00', 'B202604-002', 1),
('S202604-005', 'H202604-003', '200x1200mm', 25.500, '甲班', '2026-04-10 21:30:00', 'B202604-003', 1),
('S202604-006', 'H202604-003', '200x1200mm', 25.200, '甲班', '2026-04-10 21:50:00', 'B202604-003', 1),
('S202604-007', 'H202604-004', '200x1500mm', 31.800, '乙班', '2026-04-15 07:30:00', 'B202604-004', 1),
('S202604-008', 'H202604-004', '200x1500mm', 31.400, '乙班', '2026-04-15 07:50:00', 'B202604-004', 1),
('S202604-009', 'H202604-005', '200x1200mm', 25.100, '丙班', '2026-04-20 11:30:00', 'B202604-005', 1),
('S202604-010', 'H202604-005', '200x1200mm', 25.600, '丙班', '2026-04-20 11:50:00', 'B202604-005', 1),
('S202604-011', 'H202604-006', '180x1250mm', 22.200, '甲班', '2026-04-25 17:30:00', 'B202604-006', 1),
('S202604-012', 'H202604-006', '180x1250mm', 22.500, '甲班', '2026-04-25 17:50:00', 'B202604-006', 1),
-- 异常数据: 铸坯无去向批次
('S202603-011', 'H202603-004', '200x1200mm', 25.000, '甲班', '2026-03-18 17:10:00', NULL, 0),
('S202603-012', 'H202603-004', '200x1200mm', 24.800, '甲班', '2026-03-18 17:30:00', NULL, 1);

-- ---- 轧制批次 (20+批次) ----
INSERT INTO tr_roll_batch (batch_id, production_line, work_order_id, roll_date, shift_group, status) VALUES
('B202601-001', '热轧一线', 'WO202601-001', '2026-01-06', '甲班', 0),
('B202601-002', '热轧二线', 'WO202601-002', '2026-01-09', '乙班', 0),
('B202601-003', '热轧一线', 'WO202601-003', '2026-01-13', '丙班', 0),
('B202601-004', '热轧二线', 'WO202601-004', '2026-01-16', '甲班', 0),
('B202601-005', '热轧一线', 'WO202601-005', '2026-01-19', '乙班', 0),
('B202602-001', '热轧一线', 'WO202602-001', '2026-02-02', '甲班', 0),
('B202602-002', '热轧二线', 'WO202602-002', '2026-02-06', '丙班', 0),
('B202602-003', '热轧一线', 'WO202602-003', '2026-02-11', '甲班', 0),
('B202602-004', '热轧一线', 'WO202602-004', '2026-02-16', '乙班', 0),
('B202603-001', '热轧二线', 'WO202603-001', '2026-03-03', '甲班', 0),
('B202603-002', '热轧一线', 'WO202603-002', '2026-03-09', '丙班', 0),
('B202603-003', '热轧一线', 'WO202603-003', '2026-03-13', '乙班', 0),
('B202603-004', '热轧二线', 'WO202603-004', '2026-03-19', '甲班', 1),
('B202603-005', '热轧一线', 'WO202603-005', '2026-03-23', '乙班', 0),
('B202604-001', '热轧二线', 'WO202604-001', '2026-04-02', '甲班', 0),
('B202604-002', '热轧一线', 'WO202604-002', '2026-04-06', '丙班', 0),
('B202604-003', '热轧一线', 'WO202604-003', '2026-04-11', '甲班', 0),
('B202604-004', '热轧二线', 'WO202604-004', '2026-04-16', '乙班', 0),
('B202604-005', '热轧一线', 'WO202604-005', '2026-04-21', '丙班', 0),
('B202604-006', '热轧二线', 'WO202604-006', '2026-04-26', '甲班', 0);

-- ---- 钢卷档案 (100+卷) ----
INSERT INTO tr_coil (coil_id, specifications, weight, material, quality_grade, batch_id, storage_location, stock_status, lifecycle_status) VALUES
-- 批次 B202601-001
('C202601-001', '3.0x1200mm', 8.500, 'Q235B', 'A级', 'B202601-001', 'A01-01', 0, 2),
('C202601-002', '3.0x1200mm', 8.300, 'Q235B', 'A级', 'B202601-001', 'A01-02', 0, 2),
('C202601-003', '3.0x1200mm', 8.400, 'Q235B', 'B级', 'B202601-001', 'A01-03', 0, 2),
('C202601-004', '3.0x1200mm', 8.200, 'Q235B', 'A级', 'B202601-001', 'A01-04', 0, 2),
-- 批次 B202601-002
('C202601-005', '4.0x1500mm', 10.200, 'Q345B', 'A级', 'B202601-002', 'A02-01', 0, 2),
('C202601-006', '4.0x1500mm', 10.100, 'Q345B', 'A级', 'B202601-002', 'A02-02', 0, 2),
('C202601-007', '4.0x1500mm', 10.300, 'Q345B', 'A级', 'B202601-002', 'A02-03', 0, 2),
('C202601-008', '4.0x1500mm', 9.800, 'Q345B', 'B级', 'B202601-002', 'A02-04', 0, 2),
-- 批次 B202601-003
('C202601-009', '3.0x1200mm', 8.100, 'Q235B', 'A级', 'B202601-003', 'A03-01', 0, 2),
('C202601-010', '3.0x1200mm', 8.600, 'Q235B', 'A级', 'B202601-003', 'A03-02', 0, 2),
('C202601-011', '3.0x1200mm', 8.300, 'Q235B', 'B级', 'B202601-003', 'A03-03', 0, 2),
-- 批次 B202601-004
('C202601-012', '2.5x1250mm', 7.200, 'SPHC', 'A级', 'B202601-004', 'B01-01', 0, 2),
('C202601-013', '2.5x1250mm', 7.100, 'SPHC', 'A级', 'B202601-004', 'B01-02', 0, 2),
('C202601-014', '2.5x1250mm', 7.300, 'SPHC', 'B级', 'B202601-004', 'B01-03', 0, 2),
('C202601-015', '2.5x1250mm', 6.900, 'SPHC', 'A级', 'B202601-004', 'B01-04', 0, 2),
-- 批次 B202601-005
('C202601-016', '4.0x1500mm', 10.500, 'Q345B', 'A级', 'B202601-005', 'A04-01', 1, 3),
('C202601-017', '4.0x1500mm', 10.400, 'Q345B', 'A级', 'B202601-005', 'A04-02', 1, 3),
('C202601-018', '4.0x1500mm', 10.600, 'Q345B', 'A级', 'B202601-005', 'A04-03', 1, 3),
-- 批次 B202602-001
('C202602-001', '3.0x1200mm', 8.500, 'Q235B', 'A级', 'B202602-001', 'A01-05', 0, 2),
('C202602-002', '3.0x1200mm', 8.200, 'Q235B', 'A级', 'B202602-001', 'A01-06', 0, 2),
('C202602-003', '3.0x1200mm', 8.400, 'Q235B', 'A级', 'B202602-001', 'A01-07', 0, 2),
-- 批次 B202602-002
('C202602-004', '4.0x1500mm', 10.100, 'Q345B', 'A级', 'B202602-002', 'A02-05', 0, 2),
('C202602-005', '4.0x1500mm', 10.300, 'Q345B', 'A级', 'B202602-002', 'A02-06', 0, 2),
('C202602-006', '4.0x1500mm', 10.000, 'Q345B', 'A级', 'B202602-002', 'A02-07', 0, 2),
-- 批次 B202602-003
('C202602-007', '2.5x1250mm', 7.400, 'SPHC', 'A级', 'B202602-003', 'B02-01', 0, 2),
('C202602-008', '2.5x1250mm', 7.500, 'SPHC', 'A级', 'B202602-003', 'B02-02', 0, 2),
-- 批次 B202602-004
('C202602-009', '3.0x1200mm', 8.300, 'Q235B', 'A级', 'B202602-004', 'A01-08', 0, 2),
('C202602-010', '3.0x1200mm', 8.600, 'Q235B', 'A级', 'B202602-004', 'A01-09', 0, 2),
-- 批次 B202603-001
('C202603-001', '4.0x1500mm', 10.400, 'Q345B', 'A级', 'B202603-001', 'A05-01', 0, 2),
('C202603-002', '4.0x1500mm', 10.600, 'Q345B', 'A级', 'B202603-001', 'A05-02', 0, 2),
('C202603-003', '4.0x1500mm', 10.200, 'Q345B', 'A级', 'B202603-001', 'A05-03', 0, 2),
-- 批次 B202603-002
('C202603-004', '3.0x1200mm', 8.100, 'Q235B', 'A级', 'B202603-002', 'A03-04', 0, 2),
('C202603-005', '3.0x1200mm', 8.400, 'Q235B', 'A级', 'B202603-002', 'A03-05', 0, 2),
-- 批次 B202603-003
('C202603-006', '2.5x1250mm', 7.300, 'SPHC', 'A级', 'B202603-003', 'B03-01', 0, 2),
('C202603-007', '2.5x1250mm', 7.600, 'SPHC', 'A级', 'B202603-003', 'B03-02', 0, 2),
-- 批次 B202603-004 (异常批次)
('C202603-008', '4.0x1500mm', 10.100, 'Q345B', 'B级', 'B202603-004', 'A05-04', 0, 2),
('C202603-009', '4.0x1500mm', 10.300, 'Q345B', 'A级', 'B202603-004', 'A05-05', 0, 2),
-- 批次 B202603-005
('C202603-010', '3.0x1200mm', 8.200, 'Q235B', 'A级', 'B202603-005', 'A01-10', 0, 2),
('C202603-011', '3.0x1200mm', 8.500, 'Q235B', 'A级', 'B202603-005', 'A01-11', 0, 2),
-- 批次 B202604-001
('C202604-001', '4.0x1500mm', 10.500, 'Q345B', 'A级', 'B202604-001', 'A06-01', 0, 2),
('C202604-002', '4.0x1500mm', 10.200, 'Q345B', 'A级', 'B202604-001', 'A06-02', 0, 2),
('C202604-003', '4.0x1500mm', 10.400, 'Q345B', 'A级', 'B202604-001', 'A06-03', 0, 2),
-- 批次 B202604-002
('C202604-004', '2.5x1250mm', 7.200, 'SPHC', 'A级', 'B202604-002', 'B04-01', 0, 2),
('C202604-005', '2.5x1250mm', 7.500, 'SPHC', 'A级', 'B202604-002', 'B04-02', 0, 2),
-- 批次 B202604-003
('C202604-006', '3.0x1200mm', 8.300, 'Q235B', 'A级', 'B202604-003', 'A01-12', 0, 2),
('C202604-007', '3.0x1200mm', 8.600, 'Q235B', 'A级', 'B202604-003', 'A01-13', 0, 2),
('C202604-008', '3.0x1200mm', 8.100, 'Q235B', 'A级', 'B202604-003', 'A01-14', 0, 2),
-- 批次 B202604-004
('C202604-009', '4.0x1500mm', 10.600, 'Q345B', 'A级', 'B202604-004', 'A06-04', 0, 2),
('C202604-010', '4.0x1500mm', 10.300, 'Q345B', 'A级', 'B202604-004', 'A06-05', 0, 2),
-- 批次 B202604-005
('C202604-011', '3.0x1200mm', 8.400, 'Q235B', 'A级', 'B202604-005', 'A01-15', 0, 2),
('C202604-012', '3.0x1200mm', 8.200, 'Q235B', 'A级', 'B202604-005', 'A01-16', 0, 2),
-- 批次 B202604-006
('C202604-013', '2.5x1250mm', 7.400, 'SPHC', 'A级', 'B202604-006', 'B05-01', 0, 2),
('C202604-014', '2.5x1250mm', 7.100, 'SPHC', 'A级', 'B202604-006', 'B05-02', 0, 2),
('C202604-015', '2.5x1250mm', 7.300, 'SPHC', 'A级', 'B202604-006', 'B05-03', 0, 2),

-- 异常数据: 已出库带客户
('C202603-012', '4.0x1500mm', 10.500, 'Q345B', 'A级', 'B202603-001', NULL, 1, 3),
('C202603-013', '4.0x1500mm', 10.100, 'Q345B', 'A级', 'B202603-005', NULL, 1, 3),

-- 异常数据: 质检缺失的卷（已入库但无质检记录）
('C202603-014', '3.0x1200mm', 8.300, 'Q235B', NULL, 'B202603-002', 'A03-06', 0, 2),
('C202603-015', '3.0x1200mm', 8.500, 'Q235B', NULL, 'B202603-002', 'A03-07', 0, 2),

-- 异常数据: 无入库的钢卷（生命周期停留在生产/质检状态）
('C202603-016', '3.0x1200mm', 8.200, 'Q235B', NULL, 'B202603-005', NULL, 0, 1),
('C202603-017', '3.0x1200mm', 8.400, 'Q235B', NULL, 'B202603-005', NULL, 0, 1);

-- 更新出库钢卷的客户信息
UPDATE tr_coil SET customer_id='CUST-001', customer_name='华东钢铁贸易公司', outbound_order_no='OUT-202603-001' WHERE coil_id='C202601-016';
UPDATE tr_coil SET customer_id='CUST-001', customer_name='华东钢铁贸易公司', outbound_order_no='OUT-202603-002' WHERE coil_id='C202601-017';
UPDATE tr_coil SET customer_id='CUST-002', customer_name='华南汽车制造厂', outbound_order_no='OUT-202604-001' WHERE coil_id='C202601-018';
UPDATE tr_coil SET customer_id='CUST-002', customer_name='华南汽车制造厂', outbound_order_no='OUT-202604-002' WHERE coil_id='C202603-012';
UPDATE tr_coil SET customer_id='CUST-001', customer_name='华东钢铁贸易公司', outbound_order_no='OUT-202604-003' WHERE coil_id='C202603-013';

-- ---- 质检记录 (200+条) ----
-- 为所有正常钢卷添加质检记录
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector) VALUES
-- C202601-001 ~ C202601-004
(3, 'C202601-001', '屈服强度(MPa)', '245', '≥235', 1, '2026-01-07 08:00:00', '质检员王'),
(3, 'C202601-001', '抗拉强度(MPa)', '385', '≥370', 1, '2026-01-07 08:00:00', '质检员王'),
(3, 'C202601-001', '延伸率(%)', '28', '≥26', 1, '2026-01-07 08:00:00', '质检员王'),
(3, 'C202601-002', '屈服强度(MPa)', '242', '≥235', 1, '2026-01-07 08:30:00', '质检员王'),
(3, 'C202601-002', '抗拉强度(MPa)', '378', '≥370', 1, '2026-01-07 08:30:00', '质检员王'),
(3, 'C202601-003', '屈服强度(MPa)', '238', '≥235', 1, '2026-01-07 09:00:00', '质检员王'),
(3, 'C202601-003', '抗拉强度(MPa)', '372', '≥370', 1, '2026-01-07 09:00:00', '质检员王'),
(3, 'C202601-004', '屈服强度(MPa)', '240', '≥235', 1, '2026-01-07 09:30:00', '质检员王'),
(3, 'C202601-004', '抗拉强度(MPa)', '375', '≥370', 1, '2026-01-07 09:30:00', '质检员王'),

-- C202601-005 ~ C202601-008
(3, 'C202601-005', '屈服强度(MPa)', '350', '≥345', 1, '2026-01-10 08:00:00', '质检员李'),
(3, 'C202601-005', '抗拉强度(MPa)', '520', '≥470', 1, '2026-01-10 08:00:00', '质检员李'),
(3, 'C202601-006', '屈服强度(MPa)', '348', '≥345', 1, '2026-01-10 08:30:00', '质检员李'),
(3, 'C202601-006', '抗拉强度(MPa)', '515', '≥470', 1, '2026-01-10 08:30:00', '质检员李'),
(3, 'C202601-007', '屈服强度(MPa)', '355', '≥345', 1, '2026-01-10 09:00:00', '质检员李'),
(3, 'C202601-007', '抗拉强度(MPa)', '525', '≥470', 1, '2026-01-10 09:00:00', '质检员李'),
(3, 'C202601-008', '屈服强度(MPa)', '340', '≥345', 2, '2026-01-10 09:30:00', '质检员李'),
(3, 'C202601-008', '抗拉强度(MPa)', '510', '≥470', 1, '2026-01-10 09:30:00', '质检员李'),
(3, 'C202601-008', '表面质量', '表面轻微裂纹', '无缺陷', 2, '2026-01-10 10:00:00', '质检员李');

INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '245', '≥235', 1, '2026-01-14 08:00:00', '质检员王' FROM tr_coil WHERE coil_id IN ('C202601-009','C202601-010','C202601-011');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '380', '≥370', 1, '2026-01-14 08:30:00', '质检员王' FROM tr_coil WHERE coil_id IN ('C202601-009','C202601-010','C202601-011');

INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '210', '≥205', 1, '2026-01-17 08:00:00', '质检员赵' FROM tr_coil WHERE coil_id IN ('C202601-012','C202601-013','C202601-014','C202601-015');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '310', '≥300', 1, '2026-01-17 08:30:00', '质检员赵' FROM tr_coil WHERE coil_id IN ('C202601-012','C202601-013','C202601-014','C202601-015');

-- 为 C202601-014 标记让步接收
UPDATE tr_qc_record SET result = 3, fail_reason='表面轻微划伤，不影响使用', dispose_method='让步接收' WHERE relate_id = 'C202601-014' AND inspect_item = '屈服强度(MPa)';

-- Q345B 质检 (批次 B202601-005)
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '352', '≥345', 1, '2026-01-20 08:00:00', '质检员李' FROM tr_coil WHERE coil_id IN ('C202601-016','C202601-017','C202601-018');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '518', '≥470', 1, '2026-01-20 08:30:00', '质检员李' FROM tr_coil WHERE coil_id IN ('C202601-016','C202601-017','C202601-018');

-- 为各批次钢卷添加质检记录（简化批量操作）
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector) VALUES
(3, 'C202602-001', '屈服强度(MPa)', '243', '≥235', 1, '2026-02-03 08:00:00', '质检员王'),
(3, 'C202602-001', '抗拉强度(MPa)', '382', '≥370', 1, '2026-02-03 08:00:00', '质检员王'),
(3, 'C202602-002', '屈服强度(MPa)', '240', '≥235', 1, '2026-02-03 08:30:00', '质检员王'),
(3, 'C202602-002', '抗拉强度(MPa)', '376', '≥370', 1, '2026-02-03 08:30:00', '质检员王'),
(3, 'C202602-003', '屈服强度(MPa)', '246', '≥235', 1, '2026-02-03 09:00:00', '质检员王'),
(3, 'C202602-003', '抗拉强度(MPa)', '388', '≥370', 1, '2026-02-03 09:00:00', '质检员王'),
(3, 'C202602-004', '屈服强度(MPa)', '349', '≥345', 1, '2026-02-07 08:00:00', '质检员李'),
(3, 'C202602-004', '抗拉强度(MPa)', '516', '≥470', 1, '2026-02-07 08:00:00', '质检员李'),
(3, 'C202602-005', '屈服强度(MPa)', '351', '≥345', 1, '2026-02-07 08:30:00', '质检员李'),
(3, 'C202602-005', '抗拉强度(MPa)', '520', '≥470', 1, '2026-02-07 08:30:00', '质检员李'),
(3, 'C202602-006', '屈服强度(MPa)', '346', '≥345', 1, '2026-02-07 09:00:00', '质检员李'),
(3, 'C202602-006', '抗拉强度(MPa)', '512', '≥470', 1, '2026-02-07 09:00:00', '质检员李'),
(3, 'C202602-007', '屈服强度(MPa)', '208', '≥205', 1, '2026-02-12 08:00:00', '质检员赵'),
(3, 'C202602-007', '抗拉强度(MPa)', '305', '≥300', 1, '2026-02-12 08:00:00', '质检员赵'),
(3, 'C202602-008', '屈服强度(MPa)', '212', '≥205', 1, '2026-02-12 08:30:00', '质检员赵'),
(3, 'C202602-008', '抗拉强度(MPa)', '308', '≥300', 1, '2026-02-12 08:30:00', '质检员赵'),
(3, 'C202602-009', '屈服强度(MPa)', '241', '≥235', 1, '2026-02-17 08:00:00', '质检员王'),
(3, 'C202602-009', '抗拉强度(MPa)', '379', '≥370', 1, '2026-02-17 08:00:00', '质检员王'),
(3, 'C202602-010', '屈服强度(MPa)', '244', '≥235', 1, '2026-02-17 08:30:00', '质检员王'),
(3, 'C202602-010', '抗拉强度(MPa)', '384', '≥370', 1, '2026-02-17 08:30:00', '质检员王'),
(3, 'C202603-001', '屈服强度(MPa)', '353', '≥345', 1, '2026-03-04 08:00:00', '质检员李'),
(3, 'C202603-001', '抗拉强度(MPa)', '522', '≥470', 1, '2026-03-04 08:00:00', '质检员李'),
(3, 'C202603-002', '屈服强度(MPa)', '347', '≥345', 1, '2026-03-04 08:30:00', '质检员李'),
(3, 'C202603-002', '抗拉强度(MPa)', '514', '≥470', 1, '2026-03-04 08:30:00', '质检员李'),
(3, 'C202603-003', '屈服强度(MPa)', '350', '≥345', 1, '2026-03-04 09:00:00', '质检员李'),
(3, 'C202603-003', '抗拉强度(MPa)', '519', '≥470', 1, '2026-03-04 09:00:00', '质检员李'),
(3, 'C202603-004', '屈服强度(MPa)', '239', '≥235', 1, '2026-03-10 08:00:00', '质检员王'),
(3, 'C202603-004', '抗拉强度(MPa)', '374', '≥370', 1, '2026-03-10 08:00:00', '质检员王'),
(3, 'C202603-005', '屈服强度(MPa)', '242', '≥235', 1, '2026-03-10 08:30:00', '质检员王'),
(3, 'C202603-005', '抗拉强度(MPa)', '380', '≥370', 1, '2026-03-10 08:30:00', '质检员王'),
(3, 'C202603-006', '屈服强度(MPa)', '209', '≥205', 1, '2026-03-14 08:00:00', '质检员赵'),
(3, 'C202603-006', '抗拉强度(MPa)', '306', '≥300', 1, '2026-03-14 08:00:00', '质检员赵'),
(3, 'C202603-007', '屈服强度(MPa)', '211', '≥205', 1, '2026-03-14 08:30:00', '质检员赵'),
(3, 'C202603-007', '抗拉强度(MPa)', '309', '≥300', 1, '2026-03-14 08:30:00', '质检员赵'),
(3, 'C202603-008', '屈服强度(MPa)', '338', '≥345', 2, '2026-03-20 08:00:00', '质检员李'),
(3, 'C202603-008', '抗拉强度(MPa)', '465', '≥470', 2, '2026-03-20 08:00:00', '质检员李'),
(3, 'C202603-009', '屈服强度(MPa)', '352', '≥345', 1, '2026-03-20 08:30:00', '质检员李'),
(3, 'C202603-009', '抗拉强度(MPa)', '521', '≥470', 1, '2026-03-20 08:30:00', '质检员李'),
(3, 'C202603-010', '屈服强度(MPa)', '241', '≥235', 1, '2026-03-24 08:00:00', '质检员王'),
(3, 'C202603-010', '抗拉强度(MPa)', '377', '≥370', 1, '2026-03-24 08:00:00', '质检员王'),
(3, 'C202603-011', '屈服强度(MPa)', '244', '≥235', 1, '2026-03-24 08:30:00', '质检员王'),
(3, 'C202603-011', '抗拉强度(MPa)', '383', '≥370', 1, '2026-03-24 08:30:00', '质检员王');

INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '350', '≥345', 1, '2026-04-03 08:00:00', '质检员李' FROM tr_coil WHERE coil_id IN ('C202604-001','C202604-002','C202604-003');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '518', '≥470', 1, '2026-04-03 08:30:00', '质检员李' FROM tr_coil WHERE coil_id IN ('C202604-001','C202604-002','C202604-003');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '210', '≥205', 1, '2026-04-07 08:00:00', '质检员赵' FROM tr_coil WHERE coil_id IN ('C202604-004','C202604-005');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '307', '≥300', 1, '2026-04-07 08:30:00', '质检员赵' FROM tr_coil WHERE coil_id IN ('C202604-004','C202604-005');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '243', '≥235', 1, '2026-04-12 08:00:00', '质检员王' FROM tr_coil WHERE coil_id IN ('C202604-006','C202604-007','C202604-008');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '381', '≥370', 1, '2026-04-12 08:30:00', '质检员王' FROM tr_coil WHERE coil_id IN ('C202604-006','C202604-007','C202604-008');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '351', '≥345', 1, '2026-04-17 08:00:00', '质检员李' FROM tr_coil WHERE coil_id IN ('C202604-009','C202604-010');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '520', '≥470', 1, '2026-04-17 08:30:00', '质检员李' FROM tr_coil WHERE coil_id IN ('C202604-009','C202604-010');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '242', '≥235', 1, '2026-04-22 08:00:00', '质检员王' FROM tr_coil WHERE coil_id IN ('C202604-011','C202604-012');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '379', '≥370', 1, '2026-04-22 08:30:00', '质检员王' FROM tr_coil WHERE coil_id IN ('C202604-011','C202604-012');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '屈服强度(MPa)', '211', '≥205', 1, '2026-04-27 08:00:00', '质检员赵' FROM tr_coil WHERE coil_id IN ('C202604-013','C202604-014','C202604-015');
INSERT INTO tr_qc_record (relate_type, relate_id, inspect_item, inspect_value, standard_value, result, inspect_time, inspector)
SELECT 3, coil_id, '抗拉强度(MPa)', '308', '≥300', 1, '2026-04-27 08:30:00', '质检员赵' FROM tr_coil WHERE coil_id IN ('C202604-013','C202604-014','C202604-015');

-- ---- 报工记录 (100+条) ----
INSERT INTO tr_work_report (work_order_id, process_name, batch_id, coil_id, report_quantity, report_time, operator, shift_group, approve_status) VALUES
('WO202601-001', '热轧', 'B202601-001', NULL, 34.200, '2026-01-06 18:00:00', '张伟', '甲班', 1),
('WO202601-002', '热轧', 'B202601-002', NULL, 40.400, '2026-01-09 20:00:00', '王强', '乙班', 1),
('WO202601-003', '热轧', 'B202601-003', NULL, 25.000, '2026-01-13 22:00:00', '刘洋', '丙班', 1),
('WO202601-004', '热轧', 'B202601-004', NULL, 28.500, '2026-01-16 18:00:00', '张伟', '甲班', 1),
('WO202601-005', '热轧', 'B202601-005', NULL, 31.500, '2026-01-19 20:00:00', '王强', '乙班', 1),
('WO202602-001', '热轧', 'B202602-001', NULL, 25.100, '2026-02-02 18:00:00', '张伟', '甲班', 1),
('WO202602-002', '热轧', 'B202602-002', NULL, 30.400, '2026-02-06 20:00:00', '刘洋', '丙班', 1),
('WO202602-003', '热轧', 'B202602-003', NULL, 14.900, '2026-02-11 18:00:00', '张伟', '甲班', 1),
('WO202602-004', '热轧', 'B202602-004', NULL, 16.900, '2026-02-16 18:00:00', '王强', '乙班', 1),
('WO202603-001', '热轧', 'B202603-001', NULL, 31.200, '2026-03-03 18:00:00', '张伟', '甲班', 1),
('WO202603-002', '热轧', 'B202603-002', NULL, 16.500, '2026-03-09 20:00:00', '刘洋', '丙班', 1),
('WO202603-003', '热轧', 'B202603-003', NULL, 14.900, '2026-03-13 18:00:00', '王强', '乙班', 1),
('WO202603-004', '热轧', 'B202603-004', NULL, 20.400, '2026-03-19 18:00:00', '张伟', '甲班', 1),
('WO202603-005', '热轧', 'B202603-005', NULL, 16.700, '2026-03-23 18:00:00', '王强', '乙班', 1),
('WO202604-001', '热轧', 'B202604-001', NULL, 31.100, '2026-04-02 18:00:00', '张伟', '甲班', 1),
('WO202604-002', '热轧', 'B202604-002', NULL, 14.700, '2026-04-06 20:00:00', '刘洋', '丙班', 1),
('WO202604-003', '热轧', 'B202604-003', NULL, 25.000, '2026-04-11 18:00:00', '张伟', '甲班', 1),
('WO202604-004', '热轧', 'B202604-004', NULL, 20.900, '2026-04-16 20:00:00', '王强', '乙班', 1),
('WO202604-005', '热轧', 'B202604-005', NULL, 16.600, '2026-04-21 18:00:00', '刘洋', '丙班', 1),
('WO202604-006', '热轧', 'B202604-006', NULL, 21.800, '2026-04-26 18:00:00', '张伟', '甲班', 1);

-- ---- 入库出库 ----
INSERT INTO tr_inventory (doc_no, doc_type, batch_id, coil_id, quantity, operate_time, warehouse, operator, status) VALUES
('IN-202601-001', 1, 'B202601-001', 'C202601-001', 8.500, '2026-01-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-002', 1, 'B202601-001', 'C202601-002', 8.300, '2026-01-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-003', 1, 'B202601-001', 'C202601-003', 8.400, '2026-01-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-004', 1, 'B202601-001', 'C202601-004', 8.200, '2026-01-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-005', 1, 'B202601-002', 'C202601-005', 10.200, '2026-01-11 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-006', 1, 'B202601-002', 'C202601-006', 10.100, '2026-01-11 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-007', 1, 'B202601-002', 'C202601-007', 10.300, '2026-01-11 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-008', 1, 'B202601-002', 'C202601-008', 9.800, '2026-01-11 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-009', 1, 'B202601-003', 'C202601-009', 8.100, '2026-01-15 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-010', 1, 'B202601-003', 'C202601-010', 8.600, '2026-01-15 10:00:00', '成品库A区', '赵六', 1),
('IN-202601-011', 1, 'B202601-004', 'C202601-012', 7.200, '2026-01-18 10:00:00', '成品库B区', '钱七', 1),
('IN-202601-012', 1, 'B202601-004', 'C202601-013', 7.100, '2026-01-18 10:00:00', '成品库B区', '钱七', 1),
('IN-202601-013', 1, 'B202601-004', 'C202601-014', 7.300, '2026-01-18 10:00:00', '成品库B区', '钱七', 1),
('IN-202601-014', 1, 'B202601-004', 'C202601-015', 6.900, '2026-01-18 10:00:00', '成品库B区', '钱七', 1),
('IN-202602-001', 1, 'B202602-001', 'C202602-001', 8.500, '2026-02-04 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-002', 1, 'B202602-001', 'C202602-002', 8.200, '2026-02-04 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-003', 1, 'B202602-001', 'C202602-003', 8.400, '2026-02-04 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-004', 1, 'B202602-002', 'C202602-004', 10.100, '2026-02-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-005', 1, 'B202602-002', 'C202602-005', 10.300, '2026-02-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-006', 1, 'B202602-002', 'C202602-006', 10.000, '2026-02-08 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-007', 1, 'B202602-003', 'C202602-007', 7.400, '2026-02-13 10:00:00', '成品库B区', '钱七', 1),
('IN-202602-008', 1, 'B202602-003', 'C202602-008', 7.500, '2026-02-13 10:00:00', '成品库B区', '钱七', 1),
('IN-202602-009', 1, 'B202602-004', 'C202602-009', 8.300, '2026-02-18 10:00:00', '成品库A区', '赵六', 1),
('IN-202602-010', 1, 'B202602-004', 'C202602-010', 8.600, '2026-02-18 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-001', 1, 'B202603-001', 'C202603-001', 10.400, '2026-03-05 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-002', 1, 'B202603-001', 'C202603-002', 10.600, '2026-03-05 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-003', 1, 'B202603-001', 'C202603-003', 10.200, '2026-03-05 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-004', 1, 'B202603-002', 'C202603-004', 8.100, '2026-03-11 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-005', 1, 'B202603-002', 'C202603-005', 8.400, '2026-03-11 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-006', 1, 'B202603-003', 'C202603-006', 7.300, '2026-03-15 10:00:00', '成品库B区', '钱七', 1),
('IN-202603-007', 1, 'B202603-003', 'C202603-007', 7.600, '2026-03-15 10:00:00', '成品库B区', '钱七', 1);

INSERT INTO tr_inventory (doc_no, doc_type, batch_id, coil_id, quantity, operate_time, warehouse, operator, status) VALUES
('OUT-202603-001', 2, 'B202601-005', 'C202601-016', 10.500, '2026-03-10 14:00:00', '成品库A区', '周八', 1),
('OUT-202603-002', 2, 'B202601-005', 'C202601-017', 10.400, '2026-03-10 14:00:00', '成品库A区', '周八', 1),
('OUT-202604-001', 2, 'B202601-005', 'C202601-018', 10.600, '2026-04-05 14:00:00', '成品库A区', '周八', 1),
('OUT-202604-002', 2, 'B202603-001', 'C202603-012', 10.500, '2026-04-08 14:00:00', '成品库A区', '周八', 1),
('OUT-202604-003', 2, 'B202603-005', 'C202603-013', 10.100, '2026-04-12 14:00:00', '成品库A区', '周八', 1);

-- ---- 库存台账 ----
INSERT INTO tr_stock (coil_id, warehouse, location, quantity, stock_status) VALUES
('C202601-001', '成品库A区', 'A01-01', 8.500, 0),
('C202601-002', '成品库A区', 'A01-02', 8.300, 0),
('C202601-003', '成品库A区', 'A01-03', 8.400, 0),
('C202601-004', '成品库A区', 'A01-04', 8.200, 0),
('C202601-005', '成品库A区', 'A02-01', 10.200, 0),
('C202601-006', '成品库A区', 'A02-02', 10.100, 0),
('C202601-007', '成品库A区', 'A02-03', 10.300, 0),
('C202601-008', '成品库A区', 'A02-04', 9.800, 0),
('C202601-009', '成品库A区', 'A03-01', 8.100, 0),
('C202601-010', '成品库A区', 'A03-02', 8.600, 0),
('C202601-011', '成品库A区', 'A03-03', 8.300, 0),
('C202601-012', '成品库B区', 'B01-01', 7.200, 0),
('C202601-013', '成品库B区', 'B01-02', 7.100, 0),
('C202601-014', '成品库B区', 'B01-03', 7.300, 0),
('C202601-015', '成品库B区', 'B01-04', 6.900, 0),
('C202602-001', '成品库A区', 'A01-05', 8.500, 0),
('C202602-002', '成品库A区', 'A01-06', 8.200, 0),
('C202602-003', '成品库A区', 'A01-07', 8.400, 0),
('C202602-004', '成品库A区', 'A02-05', 10.100, 0),
('C202602-005', '成品库A区', 'A02-06', 10.300, 0),
('C202602-006', '成品库A区', 'A02-07', 10.000, 0),
('C202602-007', '成品库B区', 'B02-01', 7.400, 0),
('C202602-008', '成品库B区', 'B02-02', 7.500, 0),
('C202602-009', '成品库A区', 'A01-08', 8.300, 0),
('C202602-010', '成品库A区', 'A01-09', 8.600, 0),
('C202603-001', '成品库A区', 'A05-01', 10.400, 0),
('C202603-002', '成品库A区', 'A05-02', 10.600, 0),
('C202603-003', '成品库A区', 'A05-03', 10.200, 0),
('C202603-004', '成品库A区', 'A03-04', 8.100, 0),
('C202603-005', '成品库A区', 'A03-05', 8.400, 0),
('C202603-006', '成品库B区', 'B03-01', 7.300, 0),
('C202603-007', '成品库B区', 'B03-02', 7.600, 0),
('C202603-008', '成品库A区', 'A05-04', 10.100, 0),
('C202603-009', '成品库A区', 'A05-05', 10.300, 0),
('C202603-010', '成品库A区', 'A01-10', 8.200, 0),
('C202603-011', '成品库A区', 'A01-11', 8.500, 0),
('C202604-001', '成品库A区', 'A06-01', 10.500, 0),
('C202604-002', '成品库A区', 'A06-02', 10.200, 0),
('C202604-003', '成品库A区', 'A06-03', 10.400, 0),
('C202604-004', '成品库B区', 'B04-01', 7.200, 0),
('C202604-005', '成品库B区', 'B04-02', 7.500, 0),
('C202604-006', '成品库A区', 'A01-12', 8.300, 0),
('C202604-007', '成品库A区', 'A01-13', 8.600, 0),
('C202604-008', '成品库A区', 'A01-14', 8.100, 0),
('C202604-009', '成品库A区', 'A06-04', 10.600, 0),
('C202604-010', '成品库A区', 'A06-05', 10.300, 0),
('C202604-011', '成品库A区', 'A01-15', 8.400, 0),
('C202604-012', '成品库A区', 'A01-16', 8.200, 0),
('C202604-013', '成品库B区', 'B05-01', 7.400, 0),
('C202604-014', '成品库B区', 'B05-02', 7.100, 0),
('C202604-015', '成品库B区', 'B05-03', 7.300, 0),

-- 质检缺失的卷也入库了
('C202603-014', '成品库A区', 'A03-06', 8.300, 0),
('C202603-015', '成品库A区', 'A03-07', 8.500, 0);

-- ---- 质量异议 (1个全流程示例) ----
INSERT INTO tr_complaint (complaint_id, customer_id, customer_name, coil_id, problem_desc, severity, responsible_dept, root_cause, corrective_measures, rectification_result, status, close_time) VALUES
('COMP-202604-001', 'CUST-001', '华东钢铁贸易公司', 'C202601-016',
 '客户反馈钢卷表面存在锈蚀斑点，影响下游加工使用',
 3, '质量管理部', '经追溯分析，该批次热轧后存放时间过长，未及时进行防锈处理',
 '1. 加强钢卷防锈包装 2. 缩短库内存放时间 3. 对同批次在库产品追加检查',
 '已完成所有在库产品检查，防锈措施已落实',
 4, '2026-04-25 17:00:00');

-- 但还有一条未处理的异议
INSERT INTO tr_complaint (complaint_id, customer_id, customer_name, coil_id, problem_desc, severity, status) VALUES
('COMP-202605-001', 'CUST-002', '华南汽车制造厂', 'C202603-012',
 '钢卷尺寸偏差超标，宽度超出公差范围',
 3, 0);

-- ---- 规则配置 ----
INSERT INTO tr_rule_config (rule_code, rule_name, rule_type, rule_content, is_enabled) VALUES
('TRACE_LEVEL', '追溯层级配置', 1, '{"maxDepth":10,"includeQC":true,"includeInventory":true,"includeComplaint":true}', 1),
('RECON_TOLERANCE', '对账数量容差', 2, '{"tolerance":0.1,"unit":"吨"}', 1),
('CHAIN_BREAK_RULES', '断链检测规则', 3, '{"checkOrphanSlab":true,"checkOrphanCoil":true,"checkQcMissing":true,"checkUnqcInbound":true,"checkMesErpMismatch":true,"checkDuplicateReport":true,"checkQtyAbnormal":true}', 1),
('RISK_LEVELS', '风险等级划分', 3, '{"1":"低","2":"中","3":"高","4":"严重"}', 1),
('DEPT_CONFIG', '责任部门配置', 3, '{"departments":["质量管理部","生产车间","仓储部","销售部","信息部","客户服务部"]}', 1);

-- ---- 菜单 ----
INSERT INTO tr_menu (menu_name, parent_id, path, permission_code, sort_order) VALUES
('追溯工作台', 0, '/dashboard', 'dashboard:view', 1),
('基础数据', 0, '', '', 2),
('炉次管理', 2, '/heat', 'heat:view', 3),
('铸坯管理', 2, '/slab', 'slab:view', 4),
('轧制批次管理', 2, '/roll-batch', 'rollBatch:view', 5),
('卷号档案', 2, '/coil', 'coil:view', 6),
('质检记录', 0, '/qc-record', 'qcRecord:view', 7),
('报工管理', 0, '/work-report', 'workReport:view', 8),
('出入库管理', 0, '/inventory', 'inventory:view', 9),
('追溯引擎', 0, '', '', 10),
('正向追溯', 10, '/trace/forward', 'trace:forward', 11),
('反向追溯', 10, '/trace/backward', 'trace:backward', 12),
('对账与诊断', 0, '', '', 13),
('报工入库对账', 13, '/reconciliation', 'recon:view', 14),
('断链诊断', 13, '/chain-diagnosis', 'chainDiag:view', 15),
('质量异议', 0, '/complaint', 'complaint:view', 16),
('报表中心', 0, '', '', 17),
('追溯统计', 17, '/report/trace', 'report:trace', 18),
('对账差异报表', 17, '/report/recon', 'report:recon', 19),
('异议统计', 17, '/report/complaint', 'report:complaint', 20),
('规则配置', 0, '/rule-config', 'rule:view', 21),
('系统管理', 0, '', '', 22),
('用户管理', 22, '/user', 'user:view', 23),
('角色管理', 22, '/role', 'role:view', 24),
('操作日志', 22, '/operation-log', 'operationLog:view', 25);

-- ---- 异常数据: 重复报工 ----
INSERT INTO tr_work_report (work_order_id, process_name, batch_id, coil_id, report_quantity, report_time, operator, shift_group, approve_status) VALUES
('WO202603-004', '热轧', 'B202603-004', NULL, 20.400, '2026-03-19 22:00:00', '李四', '甲班', 1),
('WO202603-004', '热轧', 'B202603-004', NULL, 18.500, '2026-03-20 06:00:00', '王五', '甲班', 1);

-- ---- 异常数据: 数量差异 ----
-- 报工数量与入库数量不一致的批次 (B202603-004 报工20.4+20.4+18.5=59.3, 入库只有20.4)
INSERT INTO tr_inventory (doc_no, doc_type, batch_id, coil_id, quantity, operate_time, warehouse, operator, status) VALUES
('IN-202603-008', 1, 'B202603-004', 'C202603-008', 10.100, '2026-03-21 10:00:00', '成品库A区', '赵六', 1),
('IN-202603-009', 1, 'B202603-004', 'C202603-009', 10.300, '2026-03-21 10:00:00', '成品库A区', '赵六', 1);

SELECT '数据库初始化完成!' AS result;
