#!/bin/bash
# 炉批卷追溯系统 - 前后端联调测试脚本
# Usage: bash test-all.sh [token]

BASE_URL="http://localhost:8080/api/v1"
PASS=0
FAIL=0
ERRORS=""

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# 登录获取token
if [ -z "$1" ]; then
  LOGIN=$(curl -s "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}')
  TOKEN=$(echo "$LOGIN" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
else
  TOKEN="$1"
fi

AUTH="Authorization: Bearer $TOKEN"

assert() {
  local desc="$1" expected="$2" actual="$3"
  if echo "$actual" | grep -q "$expected"; then
    echo -e "  ${GREEN}✓${NC} $desc"
    PASS=$((PASS+1))
  else
    echo -e "  ${RED}✗${NC} $desc"
    echo "    Expected: $expected"
    echo "    Got: $(echo "$actual" | head -c 200)"
    FAIL=$((FAIL+1))
    ERRORS="$ERRORS\n  FAIL: $desc"
  fi
}

echo "============================================"
echo "  炉批卷追溯系统 - 前后端联调测试报告"
echo "  测试时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"
echo ""

# =========================================================
echo "【1. 认证模块】"
# =========================================================

echo "  1.1 登录 (admin/123456)"
R=$(curl -s "$BASE_URL/auth/login" -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}')
assert "登录成功返回200" '"code":200' "$R"
assert "返回token" '"token"' "$R"
assert "返回用户信息" '"realName"' "$R"

echo "  1.2 登录-错误密码"
R=$(curl -s "$BASE_URL/auth/login" -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"wrong"}')
assert "错误密码返回401" '"code":401' "$R"

echo "  1.3 获取当前用户"
R=$(curl -s "$BASE_URL/auth/me" -H "$AUTH")
assert "获取用户信息成功" '"code":200' "$R"

echo "  1.4 Token刷新"
LOGIN_R=$(curl -s "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}')
REFRESH=$(echo "$LOGIN_R" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)
printf '{"refreshToken":"%s"}' "$REFRESH" > /tmp/refresh.json
R=$(curl -s -X POST "$BASE_URL/auth/refresh" -H "Content-Type: application/json" --data-binary @/tmp/refresh.json)
assert "刷新token成功" '"code":200' "$R"

echo "  1.5 无token访问受保护接口"
R=$(curl -s "$BASE_URL/user/page" -o /dev/null -w "%{http_code}")
assert "无token返回403" "403" "$R"

echo ""
# =========================================================
echo "【2. 系统管理-角色】"
# =========================================================

echo "  2.1 角色列表"
R=$(curl -s "$BASE_URL/role" -H "$AUTH")
assert "获取角色列表成功" '"code":200' "$R"
assert "包含管理员角色" "ROLE_ADMIN" "$R"

echo "  2.2 角色分页"
R=$(curl -s "$BASE_URL/role/page?page=1&size=20" -H "$AUTH")
assert "角色分页查询成功" '"code":200' "$R"

TS=$(date +%s)

echo "  2.3 新增角色"
ROLE_CODE="ROLE_TEST_$TS"
R=$(printf '{"roleCode":"%s","roleName":"Test Role","description":"test","status":1}' "$ROLE_CODE" | curl -s -X POST "$BASE_URL/role" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
NEW_ROLE_ID=$(echo "$R" | grep -o '"data":{[^}]*}' | grep -o '"id":[0-9]*' | cut -d: -f2)
assert "新增角色成功" '"code":200' "$R"

echo "  2.4 修改角色"
R=$(printf '{"roleName":"Test Role Updated","description":"updated"}' | curl -s -X PUT "$BASE_URL/role/${NEW_ROLE_ID:-4}" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
assert "修改角色成功" '"code":200' "$R"

echo "  2.5 删除角色"
R=$(curl -s -X DELETE "$BASE_URL/role/${NEW_ROLE_ID:-4}" -H "$AUTH")
assert "删除角色成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【3. 系统管理-用户】"
# =========================================================

echo "  3.1 用户分页"
R=$(curl -s "$BASE_URL/user/page?page=1&size=20" -H "$AUTH")
assert "用户分页查询成功" '"code":200' "$R"
assert "返回用户列表" '"records"' "$R"

echo "  3.2 用户详情"
R=$(curl -s "$BASE_URL/user/1" -H "$AUTH")
assert "用户详情查询成功" '"code":200' "$R"
assert "返回角色信息" '"roles"' "$R"

echo "  3.3 新增用户"
TEST_USER="testuser_$TS"
R=$(printf '{"username":"%s","realName":"TestUser","password":"123456","email":"test@test.com","enabled":1,"roleIds":[1]}' "$TEST_USER" | curl -s -X POST "$BASE_URL/user" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
NEW_USER_ID=$(echo "$R" | grep -o '"data":{[^}]*}' | grep -o '"id":[0-9]*' | cut -d: -f2)
[ -z "$NEW_USER_ID" ] && NEW_USER_ID="4"
assert "新增用户成功" '"code":200' "$R"

echo "  3.4 修改用户"
R=$(printf '{"realName":"TestUserUpdated","email":"updated@test.com","enabled":1,"roleIds":[1,2]}' | curl -s -X PUT "$BASE_URL/user/${NEW_USER_ID}" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
assert "修改用户成功" '"code":200' "$R"

echo "  3.5 删除用户"
R=$(curl -s -X DELETE "$BASE_URL/user/${NEW_USER_ID}" -H "$AUTH")
assert "删除用户成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【4. 炉次管理】"
# =========================================================

echo "  4.1 炉次分页"
R=$(curl -s "$BASE_URL/heat/page?page=1&size=20" -H "$AUTH")
assert "炉次分页查询成功" '"code":200' "$R"

echo "  4.2 炉次详情"
R=$(curl -s "$BASE_URL/heat/H202601-001" -H "$AUTH")
assert "炉次详情查询成功" '"code":200' "$R"

echo "  4.3 炉次列表(无参数)"
R=$(curl -s "$BASE_URL/heat/list" -H "$AUTH")
assert "炉次列表查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【5. 铸坯管理】"
# =========================================================

echo "  5.1 铸坯分页"
R=$(curl -s "$BASE_URL/slab/page?page=1&size=20" -H "$AUTH")
assert "铸坯分页查询成功" '"code":200' "$R"

echo "  5.2 铸坯详情"
R=$(curl -s "$BASE_URL/slab/S202601-001" -H "$AUTH")
assert "铸坯详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【6. 轧制批次管理】"
# =========================================================

echo "  6.1 批次分页"
R=$(curl -s "$BASE_URL/roll-batch/page?page=1&size=20" -H "$AUTH")
assert "批次分页查询成功" '"code":200' "$R"

echo "  6.2 批次详情"
R=$(curl -s "$BASE_URL/roll-batch/B202601-001" -H "$AUTH")
assert "批次详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【7. 卷号档案管理】"
# =========================================================

echo "  7.1 卷号分页"
R=$(curl -s "$BASE_URL/coil/page?page=1&size=20" -H "$AUTH")
assert "卷号分页查询成功" '"code":200' "$R"

echo "  7.2 卷号详情"
R=$(curl -s "$BASE_URL/coil/C202601-001" -H "$AUTH")
assert "卷号详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【8. 质检记录管理】"
# =========================================================

echo "  8.1 质检分页"
R=$(curl -s "$BASE_URL/qc-record/page?page=1&size=20" -H "$AUTH")
assert "质检分页查询成功" '"code":200' "$R"

echo "  8.2 质检详情"
R=$(curl -s "$BASE_URL/qc-record/1" -H "$AUTH")
assert "质检详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【9. 报工管理】"
# =========================================================

echo "  9.1 报工分页"
R=$(curl -s "$BASE_URL/work-report/page?page=1&size=20" -H "$AUTH")
assert "报工分页查询成功" '"code":200' "$R"

echo "  9.2 报工详情"
R=$(curl -s "$BASE_URL/work-report/1" -H "$AUTH")
assert "报工详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【10. 出入库管理】"
# =========================================================

echo " 10.1 出入库分页"
R=$(curl -s "$BASE_URL/inventory/page?page=1&size=20" -H "$AUTH")
assert "出入库分页查询成功" '"code":200' "$R"

echo " 10.2 库存台账(分页)"
R=$(curl -s "$BASE_URL/inventory/stock/page?page=1&size=20" -H "$AUTH")
assert "库存台账查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【11. 追溯引擎】"
# =========================================================

echo " 11.1 正向追溯(炉次)"
R=$(printf '{"inputType":"heat","inputValue":"H202601-001"}' | curl -s -X POST "$BASE_URL/trace/forward" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
assert "正向追溯查询成功" '"code":200' "$R"

echo " 11.2 反向追溯(卷号)"
R=$(printf '{"inputType":"coil","inputValue":"C202601-001"}' | curl -s -X POST "$BASE_URL/trace/backward" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
assert "反向追溯查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【12. 对账管理】"
# =========================================================

echo " 12.1 对账差异列表"
R=$(curl -s "$BASE_URL/reconciliation/diff/page?page=1&size=20" -H "$AUTH")
assert "对账差异列表查询成功" '"code":200' "$R"

echo " 12.2 执行对账"
R=$(curl -s -X POST "$BASE_URL/reconciliation/execute" -H "$AUTH")
assert "执行对账成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【13. 断链诊断】"
# =========================================================

echo " 13.1 断链列表"
R=$(curl -s "$BASE_URL/chain-diagnosis/result/page?page=1&size=20" -H "$AUTH")
assert "断链列表查询成功" '"code":200' "$R"

echo " 13.2 执行诊断"
R=$(curl -s -X POST "$BASE_URL/chain-diagnosis/execute" -H "$AUTH")
assert "执行诊断成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【14. 质量异议】"
# =========================================================

echo " 14.1 异议分页"
R=$(curl -s "$BASE_URL/complaint/page?page=1&size=20" -H "$AUTH")
assert "异议分页查询成功" '"code":200' "$R"

echo " 14.2 异议详情"
R=$(curl -s "$BASE_URL/complaint/1" -H "$AUTH")
assert "异议详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【15. 仪表盘】"
# =========================================================

echo " 15.1 工作台概览"
R=$(curl -s "$BASE_URL/dashboard/overview" -H "$AUTH")
assert "工作台概览查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【16. 报表中心】"
# =========================================================

echo " 16.1 追溯统计"
R=$(curl -s "$BASE_URL/report/trace-stat" -H "$AUTH")
assert "追溯统计查询成功" '"code":200' "$R"

echo " 16.2 断链分析"
R=$(curl -s "$BASE_URL/report/chain-break-stat" -H "$AUTH")
assert "断链分析查询成功" '"code":200' "$R"

echo " 16.3 对账差异"
R=$(curl -s "$BASE_URL/report/recon-diff-stat" -H "$AUTH")
assert "对账差异查询成功" '"code":200' "$R"

echo " 16.4 质量异议统计"
R=$(curl -s "$BASE_URL/report/complaint-stat" -H "$AUTH")
assert "质量异议统计查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【17. 规则配置】"
# =========================================================

echo " 17.1 规则分页"
R=$(curl -s "$BASE_URL/rule-config/page?page=1&size=20" -H "$AUTH")
assert "规则分页查询成功" '"code":200' "$R"

echo " 17.2 规则详情"
R=$(curl -s "$BASE_URL/rule-config/1" -H "$AUTH")
assert "规则详情查询成功" '"code":200' "$R"

echo " 17.3 切换规则状态"
R=$(curl -s -X PUT "$BASE_URL/rule-config/1/toggle" -H "$AUTH")
assert "切换规则状态成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【18. 操作日志】"
# =========================================================

echo " 18.1 操作日志分页"
R=$(curl -s "$BASE_URL/operation-log/page?page=1&size=20" -H "$AUTH")
assert "操作日志分页查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【19. 前端页面加载】"
# =========================================================

echo " 19.1 前端首页"
R=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3002)
assert "前端页面可访问" "200" "$R"

echo " 19.2 前端JS资源"
R=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3002/src/main.ts)
assert "前端资源加载" "200" "$R"

echo ""
# =========================================================
echo "【20. 报工工作流】"
# =========================================================

echo " 20.1 报工审核通过"
R=$(curl -s -X PUT "$BASE_URL/work-report/1/approve" -H "$AUTH")
assert "报工审核通过成功" '"code":200' "$R"

echo " 20.2 报工驳回"
R=$(curl -s -X PUT "$BASE_URL/work-report/2/reject" -H "$AUTH")
assert "报工驳回成功" '"code":200' "$R"

echo " 20.3 报工撤销"
R=$(curl -s -X PUT "$BASE_URL/work-report/3/cancel" -H "$AUTH")
assert "报工撤销成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【21. 出入库工作流】"
# =========================================================

echo " 21.1 出入库单据审核"
R=$(curl -s -X PUT "$BASE_URL/inventory/IN-202601-001/approve" -H "$AUTH")
assert "出入库审核成功" '"code":200' "$R"

echo " 21.2 出入库单据作废"
R=$(curl -s -X PUT "$BASE_URL/inventory/IN-202601-002/void" -H "$AUTH")
assert "出入库作废成功" '"code":200' "$R"

echo " 21.3 出入库详情"
R=$(curl -s "$BASE_URL/inventory/1" -H "$AUTH")
assert "出入库详情查询成功" '"code":200' "$R"

echo ""
# =========================================================
echo "【22. 对账差异处理】"
# =========================================================

echo " 22.1 对账差异查询"
R=$(curl -s "$BASE_URL/reconciliation/diff/page?page=1&size=20" -H "$AUTH")
FIRST_DIFF_ID=$(echo "$R" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
assert "对账差异列表可查询" '"code":200' "$R"

echo " 22.2 分配责任"
if [ -n "$FIRST_DIFF_ID" ]; then
  R=$(printf '{"responsibleDept":"质量管理部"}' | curl -s -X PUT "$BASE_URL/reconciliation/diff/${FIRST_DIFF_ID}/assign" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
  assert "分配责任成功" '"code":200' "$R"
fi

echo ""
# =========================================================
echo "【23. 断链诊断处理】"
# =========================================================

echo " 23.1 断链列表查询"
R=$(curl -s "$BASE_URL/chain-diagnosis/result/page?page=1&size=20" -H "$AUTH")
FIRST_BREAK_ID=$(echo "$R" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
assert "断链列表可查询" '"code":200' "$R"

echo " 23.2 分配责任"
if [ -n "$FIRST_BREAK_ID" ]; then
  R=$(printf '{"responsibleDept":"生产车间"}' | curl -s -X PUT "$BASE_URL/chain-diagnosis/result/${FIRST_BREAK_ID}/assign" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
  assert "分配责任成功" '"code":200' "$R"
fi

echo ""
# =========================================================
echo "【24. 质量异议全流程】"
# =========================================================

echo " 24.1 新增异议"
COMPLAINT_DATA=$(printf '{"complaintId":"COMP_%s","customerName":"Test Corp","coilId":"C202601-001","problemDesc":"Test complaint","severity":1}' "$TS")
R=$(echo "$COMPLAINT_DATA" | curl -s -X POST "$BASE_URL/complaint" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
NEW_COMPLAINT_ID=$(echo "$R" | grep -o '"data":{[^}]*}' | grep -o '"id":[0-9]*' | cut -d: -f2)
assert "新增异议成功" '"code":200' "$R"

echo " 24.2 异议详情"
if [ -n "$NEW_COMPLAINT_ID" ]; then
  R=$(curl -s "$BASE_URL/complaint/${NEW_COMPLAINT_ID}" -H "$AUTH")
  assert "异议详情查询成功" '"code":200' "$R"
fi

echo ""
# =========================================================
echo "【25. 新建业务数据】"
# =========================================================

echo " 25.1 新增炉次"
HEAT_DATA=$(printf '{"heatId":"H_TEST_%s","steelGrade":"Q235B","smeltDate":"2026-05-16","status":0}' "$TS")
R=$(echo "$HEAT_DATA" | curl -s -X POST "$BASE_URL/heat" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
assert "新增炉次成功" '"code":200' "$R"

echo " 25.2 新增规则配置"
RULE_DATA=$(printf '{"ruleCode":"TEST_%s","ruleName":"Test Rule","ruleType":1,"enabled":true}' "$TS")
R=$(echo "$RULE_DATA" | curl -s -X POST "$BASE_URL/rule-config" -H "$AUTH" -H "Content-Type: application/json" --data-binary @-)
assert "新增规则配置成功" '"code":200' "$R"

echo ""
echo "============================================"
echo -e "  测试结果汇总"
echo "============================================"
echo -e "  通过: ${GREEN}$PASS${NC}"
echo -e "  失败: ${RED}$FAIL${NC}"
echo -e "  总计: $((PASS+FAIL))"
if [ -n "$ERRORS" ]; then
  echo -e "  错误列表:$ERRORS"
fi
echo "============================================"
