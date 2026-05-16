# 炉批卷追溯与报工入库对账系统

钢铁行业全链路追溯与报工入库对账系统，实现从炉次、铸坯、轧制批次到钢卷的全生命周期追溯，以及 MES 报工、质检合格、ERP 入库、库存台账四端自动对账与断链诊断。

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [数据模型](#数据模型)
- [核心规则](#核心规则)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [API 清单](#api-清单)
- [测试](#测试)
- [部署](#部署)

---

## 项目概述

### 业务痛点

- 追溯链路断裂：炉、坯、卷、工单、质检、入库、出库关系不完整
- 跨系统数据不一致：MES 报工、质检判定、ERP 入库、库存台账长期差异
- 批次管理混乱：重复报工、未质检入库、关联错误频发
- 质量异议闭环缺失：无法形成从登记到归档的全流程管理

### 核心功能

| 模块 | 功能 |
|------|------|
| 炉次管理 | CRUD、批量导入、状态跟踪 |
| 铸坯管理 | CRUD、质量状态变更（待检/合格/不合格/改判） |
| 轧制批次管理 | CRUD、批次合并/拆分 |
| 卷号档案管理 | CRUD、全生命周期状态跟踪 |
| 质检记录管理 | CRUD、判定管理 |
| 报工管理 | CRUD、审核流程 |
| 出入库管理 | 入库/出库、审核、库存台账 |
| 正向追溯 | 炉→坯→批次→卷→质检→入库→出库→客户 |
| 反向追溯 | 异议/卷/出库单→炉 |
| 报工入库对账 | 四端自动比对、差异清单、责任分配、闭环处理 |
| 断链诊断 | 6 种诊断规则、风险分级、影响分析 |
| 质量异议 | 全流程管理：登记→追溯→责任→措施→整改→复检→归档 |
| 仪表盘 | 关键指标、待办事项、快捷入口 |
| 报表中心 | 追溯统计、断链分析、对账差异、质量异议统计 |
| 规则配置 | 追溯层级、对账容差、断链规则等 |

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | JDK 21 LTS |
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.6 | ORM 数据访问 |
| MySQL | 8.0 | 数据库 |
| Spring Security | 6.x | JWT 无状态认证 |
| Knife4j | 4.5.0 | API 文档 |
| EasyExcel | 3.3.4 | Excel 导入导出 |
| Lombok | 1.18.40 | 代码简化 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Vite | 8.x | 构建工具 |
| TypeScript | 6.x | 类型安全 |
| AG Grid | 35.x | 数据表格 |
| Element Plus | 2.x | UI 组件库 |
| ECharts | 6.x | 图表可视化 |
| Pinia | 3.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |

---

## 数据模型

### 核心实体关系

```
炉次 (Heat) 1 ──── N 铸坯 (Slab) N ──── 1 轧制批次 (RollBatch) 1 ──── N 钢卷 (Coil)
                                                                        │
                                                        ┌───────────────┼───────────────┐
                                                        ▼               ▼               ▼
                                                    质检记录      报工记录         入库/出库
                                                    (QcRecord)   (WorkReport)    (Inventory)
                                                                        │
                                                                        ▼
                                                                    库存台账
                                                                    (Stock)
                                                                        │
                                                                        ▼
                                                                    质量异议
                                                                   (Complaint)
```

### 表结构

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| tr_heat | 炉次表 | heat_id, smelt_date, steel_grade, status |
| tr_slab | 铸坯表 | slab_id, heat_id, specifications, quality_status |
| tr_roll_batch | 轧制批次表 | batch_id, production_line, work_order_id |
| tr_coil | 钢卷档案表 | coil_id, batch_id, stock_status, lifecycle_status |
| tr_qc_record | 质检记录表 | relate_type, relate_id, inspect_item, result |
| tr_work_report | 报工记录表 | work_order_id, batch_id, coil_id, report_quantity |
| tr_inventory | 入库出库表 | doc_no, doc_type, coil_id, quantity |
| tr_stock | 库存台账表 | coil_id, warehouse, quantity, version |
| tr_recon_diff | 对账差异表 | batch_id, diff_type, status |
| tr_chain_break | 断链诊断表 | break_type, risk_level, status |
| tr_complaint | 质量异议表 | complaint_id, coil_id, severity, status |
| tr_trace_link | 追溯链路快照 | link_id, source_type, input_value, link_data |
| tr_rule_config | 规则配置表 | config_key, config_value, enabled |
| tr_user | 用户表 | username, password, real_name |
| tr_role | 角色表 | role_name, role_code |
| tr_user_role | 用户角色关联 | user_id, role_id |
| tr_menu | 菜单权限表 | menu_name, permission, parent_id |
| tr_operation_log | 操作日志表 | username, module, action, ip, execute_time |

---

## 核心规则

### 追溯规则

- **正向追溯**：从炉号/铸坯号/轧制批次号出发，沿 heat → slab → roll_batch → coil → qc → inventory 方向遍历
- **反向追溯**：从投诉单号/卷号/出库单号出发，沿 complaint → coil → roll_batch → slab → heat 逆向遍历
- **异常标记**：节点缺失标记为异常（红色），状态异常标记为警告（橙色）

### 对账规则

- 四端数据比对：
  - MES 报工数量（tr_work_report）
  - 质检合格数量（tr_qc_record WHERE result = 1）
  - ERP 入库数量（tr_inventory WHERE doc_type = 1）
  - 库存台账数量（tr_stock）
- 按批次聚合，逐批比对，超容差则写入对账差异
- 差异类型：数量不符、数据缺失

### 断链诊断规则

1. **批次断链**：铸坯无关联炉号、钢卷无关联批次、铸坯去向批次不存在
2. **质检缺失**：钢卷已入库但无合格质检记录
3. **未质检入库**：入库单关联的钢卷无质量合格记录
4. **MES/ERP 不一致**：报工存在但无对应入库记录
5. **重复报工**：同批次同工序存在多条报工记录
6. **数量异常**：上下游节点数量超出容差范围

风险等级：低(1) / 中(2) / 高(3) / 严重(4)

### 质量异议闭环规则

状态流转：登记(0) → 追溯中(1) → 处理中(2) → 复检中(3) → 已关闭(4) → 已归档(5)

每个状态转换需记录操作信息，关闭需客户确认，归档后不可修改。

---

## 项目结构

```
trace-system/
├── backend/                          # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/steel/trace/
│   │   │   │   ├── controller/       # REST API 控制器（16个）
│   │   │   │   ├── service/          # 业务逻辑层（15个）
│   │   │   │   ├── entity/           # 数据库实体（18个）
│   │   │   │   ├── mapper/           # MyBatis-Plus Mapper（17个）
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   │   ├── request/      # 请求参数
│   │   │   │   │   └── vo/           # 响应数据
│   │   │   │   ├── common/           # 通用组件
│   │   │   │   │   ├── result/       # 统一响应封装
│   │   │   │   │   ├── exception/    # 异常处理
│   │   │   │   │   ├── constant/     # 常量与枚举
│   │   │   │   │   └── util/         # 工具类
│   │   │   │   ├── config/           # 框架配置
│   │   │   │   └── job/              # 定时任务
│   │   │   └── resources/
│   │   │       ├── application.yml   # 开发环境配置
│   │   │       └── application-prod.yml  # 生产环境配置
│   │   └── test/
│   │       └── java/com/steel/trace/service/  # 单元测试
│   ├── sql/
│   │   └── init.sql                  # 数据库初始化脚本（含模拟数据）
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 请求层
│   │   │   ├── request.ts            # Axios 实例
│   │   │   └── modules/              # 模块 API（15个）
│   │   ├── components/               # 公共组件
│   │   ├── layout/                   # 布局组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── types/                    # TypeScript 类型定义
│   │   └── views/                    # 页面视图（15个模块）
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose（可选）
- MySQL 8.0（可使用 Docker）

### 1. 数据库初始化

```bash
# 使用 Docker MySQL
docker exec -i MySQL mysql -uroot -p123456 < backend/sql/init.sql

# 或直接连接 MySQL 执行
mysql -u root -p < backend/sql/init.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

启动后访问：http://localhost:8080/doc.html (Knife4j API 文档)

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

启动后访问：http://localhost:3000

默认账号：admin / 123456

### 4. Docker Compose 一键启动

```bash
docker-compose up -d
```

---

## API 清单

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/login | 用户登录 |
| POST | /api/v1/auth/logout | 退出登录 |

### 炉次管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/heat/page | 分页查询 |
| GET | /api/v1/heat/{heatId} | 查询详情 |
| POST | /api/v1/heat | 新增 |
| PUT | /api/v1/heat/{heatId} | 修改 |
| DELETE | /api/v1/heat/{heatId} | 作废 |
| POST | /api/v1/heat/batch-import | 批量导入 |
| GET | /api/v1/heat/export | 导出 Excel |

### 追溯

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/trace/forward | 正向追溯 |
| POST | /api/v1/trace/backward | 反向追溯 |

### 对账

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/reconciliation/execute | 执行对账 |
| GET | /api/v1/reconciliation/diff/page | 差异清单 |
| PUT | /api/v1/reconciliation/diff/{id}/assign | 责任分配 |
| PUT | /api/v1/reconciliation/diff/{id}/close | 关闭差异 |

### 断链诊断

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/chain-diagnosis/execute | 执行诊断 |
| GET | /api/v1/chain-diagnosis/result/page | 诊断结果 |
| PUT | /api/v1/chain-diagnosis/result/{id}/assign | 责任分配 |
| PUT | /api/v1/chain-diagnosis/result/{id}/close | 关闭 |

### 质量异议

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/complaint | 异议登记 |
| POST | /api/v1/complaint/{id}/trace | 追溯关联 |
| PUT | /api/v1/complaint/{id}/responsible | 责任判定 |
| PUT | /api/v1/complaint/{id}/measure | 措施制定 |
| PUT | /api/v1/complaint/{id}/rectification | 整改执行 |
| PUT | /api/v1/complaint/{id}/review | 复检复核 |
| PUT | /api/v1/complaint/{id}/close | 关闭 |
| PUT | /api/v1/complaint/{id}/archive | 归档 |

完整 API 清单请参见 Knife4j 文档：`http://localhost:8080/doc.html`

---

## 测试

### 后端测试

```bash
cd backend
mvn test
```

测试报告生成在：`backend/target/site/surefire-report.html`

### 测试覆盖范围

- Service 层核心业务逻辑单元测试（Mockito + JUnit 5）
- 覆盖模块：HeatService、CoilService、TraceService、ReconciliationService、ChainDiagnosisService、AuthService
- 场景包括：正常流程、重复数据校验、数据不存在异常、边界条件

### 前端构建验证

```bash
cd frontend
npm run build
```

---

## 部署

### Docker Compose

项目根目录的 `docker-compose.yml` 定义了三个服务：

1. **mysql** - MySQL 8.0 数据库
2. **backend** - Spring Boot 应用（端口 8080）
3. **frontend** - Vue 3 应用（端口 3000，Nginx）

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down
```

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_HOST | mysql | 数据库地址 |
| DB_PORT | 3306 | 数据库端口 |
| DB_USER | root | 数据库用户 |
| DB_PASS | root123 | 数据库密码 |
| JWT_SECRET | trace-system-secret-key | JWT 密钥 |

---

## License

Copyright © 2026
