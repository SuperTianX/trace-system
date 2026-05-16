<template>
  <div class="reconciliation-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="批次号">
          <el-input v-model="filter.batchId" placeholder="批次号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="差异类型">
          <el-select v-model="filter.diffType" placeholder="全部" clearable style="width:130px">
            <el-option label="数量不符" :value="1" />
            <el-option label="缺少质检" :value="2" />
            <el-option label="缺少入库" :value="3" />
            <el-option label="库存不符" :value="4" />
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
          <el-button type="primary" :loading="executing" @click="handleExecute">执行对账</el-button>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef, GridApi, GridOptions } from 'ag-grid-community'
import dayjs from 'dayjs'
import { executeReconciliation, getReconciliationDiffPage, assignReconciliationDiff, closeReconciliationDiff } from '../../api/modules/reconciliation'
import type { ReconDiff } from '../../types'

const loading = ref(false)
const executing = ref(false)
const rowData = ref<ReconDiff[]>([])
let gridApi: GridApi | null = null

const filter = ref({
  batchId: '',
  diffType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const defaultColDef: ColDef = {
  sortable: true,
  resizable: true,
  filter: true,
}

const columnDefs = ref<ColDef[]>([
  { field: 'batchId', headerName: '批次号', width: 140 },
  { field: 'coilId', headerName: '卷号', width: 140 },
  { field: 'diffType', headerName: '差异类型', width: 110,
    valueFormatter: (p) => {
      const map: Record<number, string> = { 1: '数量不符', 2: '缺少质检', 3: '缺少入库', 4: '库存不符' }
      return map[p.value] || '未知'
    },
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 1: '数量不符', 2: '缺少质检', 3: '缺少入库', 4: '库存不符' }
      const typeMap: Record<number, string> = { 1: 'default', 2: 'warning', 3: 'danger', 4: 'info' }
      return `<span class="ag-tag ag-tag-${typeMap[p.value] || 'default'}">${map[p.value] || '未知'}</span>`
    },
  },
  { field: 'workReportQty', headerName: '报工数', width: 90 },
  { field: 'qcPassQty', headerName: '质检通过', width: 90 },
  { field: 'erpInboundQty', headerName: 'ERP入库', width: 90 },
  { field: 'stockQty', headerName: '库存数', width: 80 },
  { field: 'description', headerName: '差异描述', width: 200, flex: 1 },
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
      const data = p.data as ReconDiff
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
      const data = p.data as ReconDiff
      if (action === 'assign') {
        currentAssignId = data.id
        assignForm.value = { responsibleDept: '' }
        assignVisible.value = true
      } else if (action === 'close') {
        try {
          await ElMessageBox.confirm('确认关闭该差异记录？', '提示')
          await closeReconciliationDiff(String(data.id))
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
    if (filter.value.batchId) params.batchId = filter.value.batchId
    if (filter.value.diffType !== undefined) params.diffType = filter.value.diffType
    if (filter.value.status !== undefined) params.status = filter.value.status
    const res = await getReconciliationDiffPage(params)
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
  filter.value = { batchId: '', diffType: undefined, status: undefined }
  loadData()
}

async function handleExecute() {
  executing.value = true
  try {
    await executeReconciliation()
    ElMessage.success('对账执行成功')
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

// Register global callbacks for AG Grid cell renderers
const win = window as any
win.__assign = (id: number) => {
  currentAssignId = id
  assignForm.value = { responsibleDept: '' }
  assignVisible.value = true
}
win.__close = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认关闭该差异记录？', '提示')
    await closeReconciliationDiff(String(id))
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
    await assignReconciliationDiff(String(currentAssignId), { responsibleDept: assignForm.value.responsibleDept })
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
.reconciliation-view {
  padding: 0;
}
</style>
