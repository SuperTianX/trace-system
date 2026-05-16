# AI 编程过程简介

## 一、需求与设计阶段

### 1.1 需求获取
以一份详细的 `requirements.md` 为起点，描述了钢铁行业炉批卷全链路追溯与报工入库对账的完整业务流程。需求涵盖了从炉次生产、铸坯浇铸、轧制批次、钢卷档案、质检、报工、出入库到质量异议的全生命周期管理，以及追溯引擎、对账引擎、断链诊断等核心能力。

### 1.2 技术方案设计
在需求基础上，由 AI 生成了完整的技术方案文档，定义了：

- **技术栈选型**: Java 21 + Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8.0 + Vue 3 + AG Grid + ECharts
- **前后端分离架构**：`backend/`（Spring Boot）+ `frontend/`（Vite + Vue 3）
- **14 张核心业务表**：涵盖 heat → slab → roll_batch → coil → qc_record → work_report → inventory → stock 等
- **模块划分**：12 个功能模块 + 3 个引擎（追溯/对账/诊断）+ 仪表盘与报表
- **多环境部署**：Docker 容器化方案，MySQL 8.0 + Spring Boot + Nginx

### 1.3 实施计划
AI 生成了分 12 个 Phase 的实施计划（`bright-mesa-owl.md`），按依赖关系排序：

```
Phase 1 脚手架 → Phase 2 CRUD → Phase 3 日志+测试 → Phase 4 追溯
→ Phase 5 对账 → Phase 6 断链 → Phase 7 质量异议 → Phase 8 仪表盘
→ Phase 9 前端（并行开发）→ Phase 10 模拟数据 → Phase 11 测试报告
→ Phase 12 README
```

---

## 二、AI 编程模式

整个开发过程采用 **AI 辅助全栈开发** 模式，人类开发者提出需求、审查代码、决策方向，AI 负责代码生成、重构、调试和测试。

### 2.1 核心交互模式

```
用户需求输入
    ↓
AI 理解并规划 → 使用 EnterPlanMode 生成实施计划
    ↓
用户审批计划 → 使用 ExitPlanMode 确认
    ↓
AI 编码实施 → 使用 Read/Edit/Write 工具操作文件
    ↓
即时验证 → 使用 Bash 工具启动服务、运行 API 测试
    ↓
用户反馈 → 识别问题、提出新需求
    ↓
[循环迭代]
```

### 2.2 关键 AI 能力运用

| 能力 | 应用场景 |
|------|---------|
| **代码生成** | 从零生成完整的 Spring Boot 后端（95+ Java 文件）+ Vue 3 前端 |
| **代码理解与重构** | 理解已有代码结构，前后端联调时快速定位路径不匹配、字段缺失等问题 |
| **调试与排错** | 分析 403/404/500 错误、编码乱码、组件渲染异常等 |
| **测试自动化** | 编写自动化测试脚本（test-all.sh），覆盖 68 个 API 测试点 |
| **文档生成** | 生成 TEST_REPORT.md、README.md、技术方案文档 |

---

## 三、开发过程中使用的 Skill

### 3.1 核心工具链

| Skill/工具 | 用途 |
|-----------|------|
| **Plan（架构师代理）** | 生成实施计划，分析技术选型，规划实施顺序 |
| **Explore（代码探索代理）** | 快速搜索文件、查找关键字、理解代码结构 |
| **Read** | 读取文件内容，理解现有代码 |
| **Write** | 创建新文件（实体、Mapper、Controller、Vue 页面等） |
| **Edit** | 修改现有代码（重构、修复 bug、调整路径） |
| **Bash** | 运行 Maven 构建、启动服务、执行 Docker/SQL 命令、运行测试 |
| **Glob** | 按模式搜索文件路径 |
| **Grep** | 在代码中搜索关键字、定位问题 |
| **WebSearch** | 查询技术文档（如 AG Grid 配置、EasyExcel 用法） |
| **TodoWrite** | 跟踪任务进度、分步骤管理复杂工作 |

### 3.2 开发模式

**迭代式开发**：每轮聚焦一个模块或一个问题，完成后立即验证。

典型的一次迭代（以修复角色管理为例）：
```
1. 用户报告 bug："系统管理菜单报错 No static resource"
2. Read 前端 API 模块 → 发现请求路径 /api/v1/role/page
3. Read 后端 Controller → 发现路径为 /api/v1/auth/role，缺少 page 端点
4. 修改 Controller：路径 + 新增 page/update/delete 端点
5. Read 实体 → 发现缺少 description/status 字段
6. 修改实体 + ALTER TABLE
7. 重启后端 → 用 curl 测试 API
8. 确认修复完成
```

**并行验证**：修改后端代码后立即用 curl 测试 API，确保改动正确，避免积压问题。

---

## 四、测试策略

### 4.1 分层测试

| 测试层次 | 方式 | 数量 |
|---------|------|------|
| Maven 单元测试 | JUnit 5 + Mockito，验证 Service 层核心逻辑 | 30 个 |
| API 集成测试 | curl 脚本批量调用所有 REST 端点，验证响应码 | 68 个 |
| 前端加载测试 | curl 验证前端页面和静态资源可访问 | 2 个 |

### 4.2 自动化测试脚本

`test-all.sh` 是一个自包含的 Bash 脚本，实现：
- 自动登录获取 JWT token
- 按模块组织测试用例
- 绿色 ✓ / 红色 ✗ 可视化输出
- 失败时打印实际响应便于调试
- 最终统计通过/失败汇总

---

## 五、开发过程中的典型问题模式

在本次开发中，AI 识别并修复了以下典型问题：

| 问题模式 | 案例 | 根因 | 修复策略 |
|---------|------|------|---------|
| 路径不匹配 | `/auth/role` vs `/role` | 前后端约定不一致 | 统一后端路径 |
| 缺少端点 | 有 page 无 getById | CRUD 不完整 | 补充缺失端点 |
| 参数类型不匹配 | `@RequestParam` vs JSON body | REST 设计不一致 | 统一为 `@RequestBody` |
| 编码问题 | 中文乱码 | MySQL/HTTP 编码配置 | 三层修复（server.encoding + connection-init-sql + utf8mb4） |
| 安全配置遗漏 | refresh 接口未放行 | permitAll 列表不完整 | 加入白名单 |
| 实体字段不匹配 | 前端发 enabled 后端有 status | 字段命名不一致 | @JsonProperty 映射 |
| 组件不兼容 | el-button 在 AG Grid 中不渲染 | Vue 组件在纯 HTML 渲染器中无效 | 替换为原生 button |

---

## 六、总结

本次 AI 辅助开发实践表明：

1. **AI 擅长全栈代码生成**：从零搭建 Spring Boot + Vue 3 项目，生成 120+ 源文件
2. **AI 能有效进行前后端联调**：快速定位路径、参数、字段不匹配等集成问题
3. **迭代式开发 + 即时验证** 是高效模式：每轮改动后立即用 curl 测试，避免问题积压
4. **自动化测试是关键保障**：68 个 API 测试点确保回归安全
5. **人类 + AI 协作最佳实践**：人类做决策和验收，AI 做生成和调试
