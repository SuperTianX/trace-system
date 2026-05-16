<template>
  <div class="dashboard">
    <div class="stat-cards">
      <el-card v-for="item in statCards" :key="item.label" class="stat-card" shadow="hover">
        <div class="stat-card-inner">
          <div :class="['stat-icon', item.color]"><el-icon :size="32"><component :is="item.icon" /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="section-card" shadow="hover">
      <template #header><span style="font-weight:600">关键指标</span></template>
      <el-row :gutter="20">
        <el-col v-for="item in metrics" :key="item.label" :span="6">
          <div class="metric-item">
            <div class="metric-value" :style="{ color: item.color }">{{ item.value }}</div>
            <div class="metric-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="section-card" shadow="hover">
      <template #header><span style="font-weight:600">今日待办</span></template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div style="font-size:14px;font-weight:500;margin-bottom:8px;color:#e6a23c">待处理断链 ({{ recentBreaks.length }})</div>
          <el-table :data="recentBreaks" size="small" max-height="240" stripe style="width:100%">
            <el-table-column prop="batchId" label="批次" width="130" />
            <el-table-column prop="breakType" label="类型" width="70">
              <template #default="{ row }">{{ breakTypeLabel(row.breakType) }}</template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险" width="60">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" size="small">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="breakDesc" label="描述" min-width="160" show-overflow-tooltip />
          </el-table>
          <div v-if="!recentBreaks.length" style="color:#999;font-size:13px;padding:16px 0;text-align:center">暂无待处理断链</div>
        </el-col>
        <el-col :span="12">
          <div style="font-size:14px;font-weight:500;margin-bottom:8px;color:#1a73e8">待处理对账差异 ({{ recentDiffs.length }})</div>
          <el-table :data="recentDiffs" size="small" max-height="240" stripe style="width:100%">
            <el-table-column prop="batchId" label="批次" width="130" />
            <el-table-column prop="diffType" label="类型" width="70">
              <template #default="{ row }">{{ row.diffType === 1 ? '数量差异' : '数据缺失' }}</template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          </el-table>
          <div v-if="!recentDiffs.length" style="color:#999;font-size:13px;padding:16px 0;text-align:center">暂无待处理差异</div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="section-card" shadow="hover">
      <template #header><span style="font-weight:600">快捷入口</span></template>
      <el-row :gutter="12">
        <el-col v-for="btn in quickActions" :key="btn.label" :span="4">
          <el-button :type="btn.type" style="width:100%" @click="router.push(btn.path)">
            {{ btn.label }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardOverview } from '../../api/modules/dashboard'
import { Warning, ChatDotSquare, DataBoard, Connection, Odometer, DataAnalysis, Setting } from '@element-plus/icons-vue'
import type { ChainBreak, ReconDiff } from '../../types'

const router = useRouter()

const statCards = ref([
  { label: '待处理异议', value: '—', icon: ChatDotSquare, color: 'red' },
  { label: '断链记录', value: '—', icon: Warning, color: 'orange' },
  { label: '对账差异', value: '—', icon: DataBoard, color: 'blue' },
  { label: '预警批次', value: '—', icon: Connection, color: 'purple' },
])

const metrics = ref([
  { label: '追溯次数', value: '—', color: '#1a73e8' },
  { label: '断链率', value: '—', color: '#e6a23c' },
  { label: '差异率', value: '—', color: '#f56c6c' },
  { label: '异议闭环率', value: '—', color: '#67c23a' },
])

const recentBreaks = ref<ChainBreak[]>([])
const recentDiffs = ref<ReconDiff[]>([])

const quickActions = [
  { label: '炉次管理', path: '/heat', type: 'primary' },
  { label: '铸坯管理', path: '/slab', type: 'success' },
  { label: '卷号档案', path: '/coil', type: 'warning' },
  { label: '追溯查询', path: '/trace', type: 'danger' },
  { label: '报工对账', path: '/reconciliation', type: 'info' },
  { label: '断链诊断', path: '/chain-diagnosis', type: 'primary' },
]

function breakTypeLabel(type: number) {
  const map: Record<number, string> = { 1: '断链', 2: '质检缺失', 3: '未检入库', 4: 'MES/ERP不符', 5: '重复报工', 6: '数量异常' }
  return map[type] || '未知'
}

function riskTagType(level: number) {
  return level >= 4 ? 'danger' : level >= 3 ? 'warning' : 'info'
}

function riskLabel(level: number) {
  const map: Record<number, string> = { 1: '低', 2: '中', 3: '高', 4: '严重' }
  return map[level] || '未知'
}

onMounted(async () => {
  try {
    const res = await getDashboardOverview()
    const d = res.data
    statCards.value[0].value = String(d.pendingComplaints ?? '—')
    statCards.value[1].value = String(d.pendingChainBreaks ?? '—')
    statCards.value[2].value = String(d.pendingReconDiffs ?? '—')
    statCards.value[3].value = String(d.warningBatches ?? '—')
    metrics.value[0].value = String(d.traceCount ?? '—')
    metrics.value[1].value = (d.chainBreakRate != null ? (d.chainBreakRate * 100).toFixed(1) : '—') + '%'
    metrics.value[2].value = (d.reconDiffRate != null ? (d.reconDiffRate * 100).toFixed(1) : '—') + '%'
    metrics.value[3].value = (d.complaintCloseRate != null ? (d.complaintCloseRate * 100).toFixed(1) : '—') + '%'
    if (d.recentChainBreaks) recentBreaks.value = d.recentChainBreaks
    if (d.recentReconDiffs) recentDiffs.value = d.recentReconDiffs
  } catch {}
})
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card {  }
.stat-card-inner { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-icon.red { background: #fef0f0; color: #f56c6c; }
.stat-icon.orange { background: #fdf6ec; color: #e6a23c; }
.stat-icon.blue { background: #ecf5ff; color: #1a73e8; }
.stat-icon.purple { background: #f5f0ff; color: #7c3aed; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #333; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.section-card { margin-bottom: 16px; }
.metric-item { text-align: center; padding: 16px 0; }
.metric-value { font-size: 24px; font-weight: 700; }
.metric-label { font-size: 13px; color: #999; margin-top: 4px; }
</style>
