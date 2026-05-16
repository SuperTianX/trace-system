<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="铸坯号"><el-input v-model="query.slabId" placeholder="铸坯号" clearable /></el-form-item>
        <el-form-item label="炉号"><el-input v-model="query.heatId" placeholder="炉号" clearable /></el-form-item>
        <el-form-item label="质量状态">
          <el-select v-model="query.qualityStatus" placeholder="质量状态" clearable style="width:120px">
            <el-option label="待检" :value="0" />
            <el-option label="合格" :value="1" />
            <el-option label="不合格" :value="2" />
            <el-option label="改判" :value="3" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑铸坯' : '新增铸坯'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="铸坯号" prop="slabId"><el-input v-model="form.slabId" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="炉号" prop="heatId"><el-input v-model="form.heatId" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="规格"><el-input v-model="form.specifications" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="重量(吨)"><el-input-number v-model="form.weight" :precision="3" :step="0.1" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="浇次班组"><el-input v-model="form.castShift" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="轧制批次"><el-input v-model="form.rollBatchId" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="质量状态">
          <el-select v-model="form.qualityStatus" style="width:200px">
            <el-option label="待检" :value="0" />
            <el-option label="合格" :value="1" />
            <el-option label="不合格" :value="2" />
            <el-option label="改判" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="修改质量状态" width="400px">
      <el-form label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="qualityStatusTag(form.currentStatus)">{{ qualityStatusLabel(form.currentStatus) }}</el-tag>
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="form.qualityStatus" style="width:100%">
            <el-option label="待检" :value="0" />
            <el-option label="合格" :value="1" />
            <el-option label="不合格" :value="2" />
            <el-option label="改判" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateStatus">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef } from 'ag-grid-community'
import { getSlabPage, getSlab, createSlab, updateSlab, deleteSlab, updateSlabStatus } from '../../api/modules/slab'

const rowData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const saving = ref(false)
const statusDialogVisible = ref(false)
const updatingId = ref('')
let gridApi: any = null

const query = reactive({ slabId: '', heatId: '', qualityStatus: undefined as number | undefined, page: 1, size: 20 })
const form = reactive<any>({ slabId: '', heatId: '', specifications: '', weight: null, castShift: '', rollBatchId: '', qualityStatus: 0 })
const rules = {
  slabId: [{ required: true, message: '铸坯号不能为空' }],
  heatId: [{ required: true, message: '炉号不能为空' }],
}

function qualityStatusLabel(val: number): string {
  const map: Record<number, string> = { 0: '待检', 1: '合格', 2: '不合格', 3: '改判' }
  return map[val] ?? '未知'
}
function qualityStatusTag(val: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[val] ?? ''
}

const defaultColDef = { sortable: true, resizable: true, filter: true, minWidth: 100 }
const columnDefs = ref<ColDef[]>([
  { field: 'slabId', headerName: '铸坯号', pinned: 'left', width: 150 },
  { field: 'heatId', headerName: '炉号', width: 140 },
  { field: 'specifications', headerName: '规格', width: 120 },
  { field: 'weight', headerName: '重量(吨)', width: 110 },
  {
    field: 'qualityStatus', headerName: '质量状态', width: 100,
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 0: '待检', 1: '合格', 2: '不合格', 3: '改判' }
      const colorMap: Record<number, string> = { 0: '#e6a23c', 1: '#67c23a', 2: '#f56c6c', 3: '#909399' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${map[p.value] ?? '未知'}</span>`
    },
    cellStyle: (p: any) => {
      const bgMap: Record<number, string> = { 0: '#fdf6ec', 1: '#f0f9eb', 2: '#fef0f0', 3: '#f4f4f5' }
      return { backgroundColor: bgMap[p.value] ?? undefined }
    },
  },
  { field: 'rollBatchId', headerName: '轧制批次', width: 140 },
  { field: 'castTime', headerName: '浇铸时间', width: 160 },
  {
    headerName: '操作', width: 240, pinned: 'right', sortable: false, filter: false,
    cellRenderer: (p: any) =>
      `<button class="action-btn primary" data-action="edit">编辑</button>` +
      `<button class="action-btn warning" data-action="rejudge">改判</button>` +
      `<button class="action-btn danger" data-action="delete">删除</button>`,
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      if (action === 'edit') { showEdit(p.data.slabId) }
      else if (action === 'rejudge') { showStatusDialog(p.data.slabId, p.data.qualityStatus) }
      else if (action === 'delete') { handleDelete(p.data.slabId) }
    },
  },
])

function onGridReady(params: any) { gridApi = params.api; loadData() }

async function loadData() {
  try {
    const res = await getSlabPage({ ...query, page: query.page, size: query.size })
    rowData.value = res.data.records
  } catch {}
}
function resetQuery() { Object.assign(query, { slabId: '', heatId: '', qualityStatus: undefined }); loadData() }
function showAdd() {
  isEdit.value = false; editingId.value = ''
  Object.assign(form, { slabId: '', heatId: '', specifications: '', weight: null, castShift: '', rollBatchId: '', qualityStatus: 0 })
  dialogVisible.value = true
}
async function showEdit(id: string) {
  isEdit.value = true; editingId.value = id
  try {
    const res = await getSlab(id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch {}
}
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await updateSlab(editingId.value, form); ElMessage.success('修改成功') }
    else { await createSlab(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch {} finally { saving.value = false }
}
async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该铸坯？')
    await deleteSlab(id); ElMessage.success('删除成功'); loadData()
  } catch {}
}
function showStatusDialog(id: string, currentStatus: number) {
  updatingId.value = id
  form.currentStatus = currentStatus
  form.qualityStatus = currentStatus
  statusDialogVisible.value = true
}
async function handleUpdateStatus() {
  try {
    await updateSlabStatus(updatingId.value, form.qualityStatus)
    ElMessage.success('状态修改成功')
    statusDialogVisible.value = false
    loadData()
  } catch {}
}
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
:deep(.ag-theme-quartz) { --ag-header-height: 40px; --ag-row-height: 38px; font-size: 13px; }
</style>
