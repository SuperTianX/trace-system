<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="工单号"><el-input v-model="query.workOrderId" placeholder="工单号" clearable /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="query.batchId" placeholder="批次号" clearable /></el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="query.approveStatus" placeholder="审批状态" clearable style="width:120px">
            <el-option label="待审" :value="0" />
            <el-option label="通过" :value="1" />
            <el-option label="驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <ag-grid-vue
        class="ag-theme-quartz"
        style="height:500px"
        :rowData="rowData"
        :columnDefs="columnDefs"
        :defaultColDef="defaultColDef"
        :pagination="true"
        :paginationPageSize="20"
        @grid-ready="onGridReady"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑报工记录' : '新增报工记录'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderId"><el-input v-model="form.workOrderId" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工序名称"><el-input v-model="form.processName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="钢卷号"><el-input v-model="form.coilId" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="报工数量"><el-input-number v-model="form.reportQuantity" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作工"><el-input v-model="form.operator" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="班组"><el-input v-model="form.shiftGroup" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="报工时间"><el-date-picker v-model="form.reportTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="审批状态">
          <el-select v-model="form.approveStatus" style="width:200px">
            <el-option label="待审" :value="0" />
            <el-option label="通过" :value="1" />
            <el-option label="驳回" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef } from 'ag-grid-community'
import { getWorkReportPage, getWorkReport, createWorkReport, updateWorkReport, deleteWorkReport, approveWorkReport, rejectWorkReport, cancelWorkReport } from '../../api/modules/workReport'

const rowData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const saving = ref(false)
let gridApi: any = null

const query = reactive({ workOrderId: '', batchId: '', approveStatus: undefined as number | undefined, page: 1, size: 20 })
const form = reactive<any>({ workOrderId: '', processName: '', batchId: '', coilId: '', reportQuantity: null, reportTime: '', operator: '', shiftGroup: '', approveStatus: 0 })
const rules = { workOrderId: [{ required: true, message: '工单号不能为空' }] }

function approveStatusLabel(val: number): string {
  const map: Record<number, string> = { 0: '待审', 1: '通过', 2: '驳回' }
  return map[val] ?? '未知'
}
function approveStatusTag(val: number): string {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[val] ?? ''
}

const defaultColDef = { sortable: true, resizable: true, filter: true, minWidth: 100 }
const columnDefs = ref<ColDef[]>([
  { field: 'workOrderId', headerName: '工单号', pinned: 'left', width: 150 },
  { field: 'processName', headerName: '工序名称', width: 100 },
  { field: 'batchId', headerName: '批次号', width: 140 },
  { field: 'coilId', headerName: '钢卷号', width: 150 },
  { field: 'reportQuantity', headerName: '报工数量', width: 100 },
  {
    field: 'approveStatus', headerName: '审批状态', width: 100,
    cellRenderer: (p: any) => {
      const colorMap: Record<number, string> = { 0: '#909399', 1: '#67c23a', 2: '#f56c6c' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${approveStatusLabel(p.value)}</span>`
    },
    cellStyle: (p: any) => {
      const bgMap: Record<number, string> = { 0: '#f4f4f5', 1: '#f0f9eb', 2: '#fef0f0' }
      return { backgroundColor: bgMap[p.value] ?? undefined }
    },
  },
  { field: 'reportTime', headerName: '报工时间', width: 170 },
  { field: 'operator', headerName: '操作工', width: 100 },
  { field: 'shiftGroup', headerName: '班组', width: 100 },
  {
    headerName: '操作', width: 320, pinned: 'right', sortable: false, filter: false,
    cellRenderer: (p: any) => {
      const status = p.data.approveStatus
      let btns = `<button class="action-btn primary" data-action="edit">编辑</button>`
      if (status === 0) {
        btns += `<button class="action-btn success" data-action="approve">通过</button>`
        btns += `<button class="action-btn danger" data-action="reject">驳回</button>`
        btns += `<button class="action-btn warning" data-action="cancel">取消</button>`
      }
      btns += `<button class="action-btn danger" data-action="delete">删除</button>`
      return btns
    },
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      if (action === 'edit') { showEdit(p.data.id) }
      else if (action === 'approve') { handleApprove(p.data.id) }
      else if (action === 'reject') { handleReject(p.data.id) }
      else if (action === 'cancel') { handleCancel(p.data.id) }
      else if (action === 'delete') { handleDelete(p.data.id) }
    },
  },
])

function onGridReady(params: any) { gridApi = params.api; loadData() }

async function loadData() {
  try {
    const res = await getWorkReportPage({ ...query, page: query.page, size: query.size })
    rowData.value = res.data.records
  } catch {}
}
function resetQuery() { Object.assign(query, { workOrderId: '', batchId: '', approveStatus: undefined }); loadData() }
function showAdd() {
  isEdit.value = false; editingId.value = ''
  Object.assign(form, { workOrderId: '', processName: '', batchId: '', coilId: '', reportQuantity: null, reportTime: '', operator: '', shiftGroup: '', approveStatus: 0 })
  dialogVisible.value = true
}
async function showEdit(id: string) {
  isEdit.value = true; editingId.value = id
  try {
    const res = await getWorkReport(id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch {}
}
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await updateWorkReport(editingId.value, form); ElMessage.success('修改成功') }
    else { await createWorkReport(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch {} finally { saving.value = false }
}
async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该报工记录？')
    await deleteWorkReport(id); ElMessage.success('删除成功'); loadData()
  } catch {}
}
async function handleApprove(id: string) {
  try {
    await approveWorkReport(id)
    ElMessage.success('审批通过')
    loadData()
  } catch {}
}
async function handleReject(id: string) {
  try {
    await rejectWorkReport(id)
    ElMessage.success('已驳回')
    loadData()
  } catch {}
}
async function handleCancel(id: string) {
  try {
    await cancelWorkReport(id)
    ElMessage.success('已取消')
    loadData()
  } catch {}
}
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
:deep(.ag-theme-quartz) { --ag-header-height: 40px; --ag-row-height: 38px; font-size: 13px; }
</style>
