import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/index.vue'), meta: { title: '追溯工作台', icon: 'Odometer' } },
      { path: 'heat', name: 'Heat', component: () => import('../views/heat/index.vue'), meta: { title: '炉次管理', icon: 'FactoryBuilding' } },
      { path: 'slab', name: 'Slab', component: () => import('../views/slab/index.vue'), meta: { title: '铸坯管理', icon: 'Grid' } },
      { path: 'roll-batch', name: 'RollBatch', component: () => import('../views/rollBatch/index.vue'), meta: { title: '轧制批次管理', icon: 'List' } },
      { path: 'coil', name: 'Coil', component: () => import('../views/coil/index.vue'), meta: { title: '卷号档案管理', icon: 'Document' } },
      { path: 'qc-record', name: 'QcRecord', component: () => import('../views/qcRecord/index.vue'), meta: { title: '质检记录管理', icon: 'Select' } },
      { path: 'work-report', name: 'WorkReport', component: () => import('../views/workReport/index.vue'), meta: { title: '报工管理', icon: 'EditPen' } },
      { path: 'inventory', name: 'Inventory', component: () => import('../views/inventory/index.vue'), meta: { title: '出入库管理', icon: 'Box' } },
      { path: 'trace', name: 'Trace', component: () => import('../views/trace/index.vue'), meta: { title: '追溯查询', icon: 'Connection' } },
      { path: 'reconciliation', name: 'Reconciliation', component: () => import('../views/reconciliation/index.vue'), meta: { title: '报工入库对账', icon: 'DataBoard' } },
      { path: 'chain-diagnosis', name: 'ChainDiagnosis', component: () => import('../views/chainDiagnosis/index.vue'), meta: { title: '断链诊断', icon: 'Warning' } },
      { path: 'complaint', name: 'Complaint', component: () => import('../views/complaint/index.vue'), meta: { title: '质量异议', icon: 'ChatDotSquare' } },
      { path: 'report', name: 'Report', component: () => import('../views/report/index.vue'), meta: { title: '报表中心', icon: 'DataAnalysis' } },
      { path: 'rule-config', name: 'RuleConfig', component: () => import('../views/ruleConfig/index.vue'), meta: { title: '规则配置', icon: 'Setting' } },
      {
        path: 'system',
        meta: { title: '系统管理', icon: 'Tools' },
        children: [
          { path: 'user', name: 'User', component: () => import('../views/system/user/index.vue'), meta: { title: '用户管理' } },
          { path: 'role', name: 'Role', component: () => import('../views/system/role/index.vue'), meta: { title: '角色管理' } },
        ],
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
})

export default router
