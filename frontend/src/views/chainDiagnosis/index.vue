<template>
  <div class="chain-diagnosis-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="断链类型">
          <el-select v-model="filter.breakType" placeholder="全部" clearable style="width:150px">
            <el-option label="批次断链" :value="1" />
            <el-option label="质检缺失" :value="2" />
            <el-option label="未质检入库" :value="3" />
            <el-option label="MES/ERP不一致" :value="4" />
            <el-option label="重复报工" :value="5" />
            <el-option label="数量异常" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="filter.riskLevel" placeholder="全部" clearable style="width:130px">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="严重" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待处理" :value="0" />
            <el-option label="已分配" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="executing" @click="handleExecute">执行诊断</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <ag-grid-vue
        class="ag-theme-quartz"
        style="height:500px"
        :columnDefs="columnDefs"
        :rowData="rowData"
        :pagination="true"
        :paginationPageSize="20"
        :defaultColDef="defaultColDef"
        :loading="loading"
        @grid-ready="onGridReady"
      />
    </el-card>

    <!-- Assign Dialog -->
    <el-dialog v-model="assignVisible" title="分配责任部门" width="400px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="责任部门">
          <el-input v-model="assignForm.responsibleDept" placeholder="请输入责任部门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef, GridApi } from 'ag-grid-community'
import { executeChainDiagnosis, getChainBreakPage, assignChainBreak, closeChainBreak } from '../../api/modules/chainDiagnosis'
import type { ChainBreak } from '../../types'

const loading = ref(false)
const executing = ref(false)
const rowData = ref<ChainBreak[]>([])
let gridApi: GridApi | null = null

const filter = ref({
  breakType: undefined as number | undefined,
  riskLevel: undefined as number | undefined,
  status: undefined as number | undefined,
})

const defaultColDef: ColDef = {
  sortable: true,
  resizable: true,
  filter: true,
}

const breakTypeMap: Record<number, { label: string; type: string }> = {
  1: { label: '批次断链', type: '' },
  2: { label: '质检缺失', type: 'danger' },
  3: { label: '未质检入库', type: 'warning' },
  4: { label: 'MES/ERP不一致', type: '' },
  5: { label: '重复报工', type: 'info' },
  6: { label: '数量异常', type: 'danger' },
}

const riskLevelMap: Record<number, { label: string; type: string }> = {
  1: { label: '低', type: 'info' },
  2: { label: '中', type: 'warning' },
  3: { label: '高', type: 'danger' },
  4: { label: '严重', type: 'danger' },
}

const columnDefs = ref<ColDef[]>([
  {
    field: 'breakType',
    headerName: '断链类型',
    width: 150,
    cellRenderer: (p: any) => {
      const info = breakTypeMap[p.value] || { label: '未知', type: '' }
      return `<span class="ag-tag ag-tag-${info.type || 'default'}">${info.label}</span>`
    },
  },
  { field: 'heatId', headerName: '炉号', width: 130 },
  { field: 'slabId', headerName: '铸坯号', width: 130 },
  { field: 'batchId', headerName: '批次号', width: 130 },
  { field: 'coilId', headerName: '卷号', width: 130 },
  {
    field: 'riskLevel',
    headerName: '风险等级',
    width: 100,
    cellRenderer: (p: any) => {
      const info = riskLevelMap[p.value] || { label: '未知', type: '' }
      return `<span class="ag-tag ag-tag-${info.type || 'default'}">${info.label}</span>`
    },
  },
  { field: 'responsibleDept', headerName: '责任部门', width: 120 },
  {
    field: 'status',
    headerName: '状态',
    width: 100,
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 0: '待处理', 1: '已分配', 2: '已关闭' }
      const typeMap: Record<number, string> = { 0: 'danger', 1: 'warning', 2: 'success' }
      return `<span class="ag-tag ag-tag-${typeMap[p.value] || 'default'}">${map[p.value] || '未知'}</span>`
    },
  },
  {
    headerName: '操作',
    width: 180,
    pinned: 'right',
    cellRenderer: (p: any) => {
      const data = p.data as ChainBreak
      let btns = ''
      if (data.status === 0) {
        btns += `<button class="action-btn primary" data-action="assign">分配</button> `
      }
      if (data.status === 0 || data.status === 1) {
        btns += `<button class="action-btn" data-action="close">关闭</button>`
      }
      return btns
    },
    onCellClicked: async (p: any) => {
      const action = p.event.target?.dataset?.action
      const data = p.data as ChainBreak
      if (action === 'assign') {
        currentAssignId = data.id
        assignForm.value = { responsibleDept: '' }
        assignVisible.value = true
      } else if (action === 'close') {
        try {
          await ElMessageBox.confirm('确认关闭该断链记录？', '提示')
          await closeChainBreak(String(data.id))
          ElMessage.success('已关闭')
          loadData()
        } catch {}
      }
    },
  },
])

function onGridReady(params: any) {
  gridApi = params.api
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: any = { page: 1, size: 20 }
    if (filter.value.breakType !== undefined) params.breakType = filter.value.breakType
    if (filter.value.riskLevel !== undefined) params.riskLevel = filter.value.riskLevel
    if (filter.value.status !== undefined) params.status = filter.value.status
    const res = await getChainBreakPage(params)
    rowData.value = res.data?.records || []
  } catch {
    rowData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function handleReset() {
  filter.value = { breakType: undefined, riskLevel: undefined, status: undefined }
  loadData()
}

async function handleExecute() {
  executing.value = true
  try {
    await executeChainDiagnosis()
    ElMessage.success('诊断执行成功')
    loadData()
  } catch {
    // Error handled by interceptor
  } finally {
    executing.value = false
  }
}

// Assign dialog
const assignVisible = ref(false)
const assignForm = ref({ responsibleDept: '' })
const assigning = ref(false)
let currentAssignId: number | null = null

const win = window as any
win.__diagAssign = (id: number) => {
  currentAssignId = id
  assignForm.value = { responsibleDept: '' }
  assignVisible.value = true
}
win.__diagClose = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认关闭该断链记录？', '提示')
    await closeChainBreak(String(id))
    ElMessage.success('已关闭')
    loadData()
  } catch {
    // cancelled or error
  }
}

async function handleAssign() {
  if (!assignForm.value.responsibleDept) {
    ElMessage.warning('请输入责任部门')
    return
  }
  assigning.value = true
  try {
    await assignChainBreak(String(currentAssignId), { responsibleDept: assignForm.value.responsibleDept })
    ElMessage.success('分配成功')
    assignVisible.value = false
    loadData()
  } catch {
    // Error handled by interceptor
  } finally {
    assigning.value = false
  }
}
</script>

<style scoped>
.chain-diagnosis-view {
  padding: 0;
}
</style>
