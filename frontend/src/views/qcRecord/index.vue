<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="关联类型">
          <el-select v-model="query.relateType" placeholder="关联类型" clearable style="width:120px">
            <el-option label="炉次" :value="1" />
            <el-option label="铸坯" :value="2" />
            <el-option label="钢卷" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联ID"><el-input v-model="query.relateId" placeholder="关联ID" clearable /></el-form-item>
        <el-form-item label="检验项目"><el-input v-model="query.inspectItem" placeholder="检验项目" clearable /></el-form-item>
        <el-form-item label="检验结果">
          <el-select v-model="query.result" placeholder="检验结果" clearable style="width:120px">
            <el-option label="合格" :value="1" />
            <el-option label="不合格" :value="2" />
            <el-option label="让步接收" :value="3" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑质检记录' : '新增质检记录'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联类型" prop="relateType">
              <el-select v-model="form.relateType" style="width:100%">
                <el-option label="炉次" :value="1" />
                <el-option label="铸坯" :value="2" />
                <el-option label="钢卷" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联ID" prop="relateId"><el-input v-model="form.relateId" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="检验项目"><el-input v-model="form.inspectItem" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检验值"><el-input v-model="form.inspectValue" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="标准值"><el-input v-model="form.standardValue" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="检验结果" prop="result">
              <el-select v-model="form.result" style="width:100%">
                <el-option label="合格" :value="1" />
                <el-option label="不合格" :value="2" />
                <el-option label="让步接收" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="检验员"><el-input v-model="form.inspector" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检验时间"><el-date-picker v-model="form.inspectTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="不合格原因"><el-input v-model="form.failReason" type="textarea" /></el-form-item>
        <el-form-item label="处置方式"><el-input v-model="form.disposeMethod" type="textarea" /></el-form-item>
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
import { getQcRecordPage, getQcRecord, createQcRecord, updateQcRecord, deleteQcRecord } from '../../api/modules/qcRecord'

const rowData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const saving = ref(false)
let gridApi: any = null

const query = reactive({ relateType: undefined as number | undefined, relateId: '', inspectItem: '', result: undefined as number | undefined, page: 1, size: 20 })
const form = reactive<any>({ relateType: 1, relateId: '', inspectItem: '', inspectValue: '', standardValue: '', result: 1, inspectTime: '', inspector: '', failReason: '', disposeMethod: '' })
const rules = {
  relateType: [{ required: true, message: '关联类型不能为空' }],
  relateId: [{ required: true, message: '关联ID不能为空' }],
  result: [{ required: true, message: '检验结果不能为空' }],
}

function relateTypeLabel(val: number): string {
  const map: Record<number, string> = { 1: '炉次', 2: '铸坯', 3: '钢卷' }
  return map[val] ?? '未知'
}
function resultLabel(val: number): string {
  const map: Record<number, string> = { 1: '合格', 2: '不合格', 3: '让步接收' }
  return map[val] ?? '未知'
}
function resultTag(val: number): string {
  const map: Record<number, string> = { 1: 'success', 2: 'danger', 3: 'warning' }
  return map[val] ?? ''
}

const defaultColDef = { sortable: true, resizable: true, filter: true, minWidth: 100 }
const columnDefs = ref<ColDef[]>([
  {
    field: 'relateType', headerName: '关联类型', width: 100,
    cellRenderer: (p: any) => relateTypeLabel(p.value),
  },
  { field: 'relateId', headerName: '关联ID', width: 160 },
  { field: 'inspectItem', headerName: '检验项目', width: 120 },
  { field: 'inspectValue', headerName: '检验值', width: 110 },
  {
    field: 'result', headerName: '检验结果', width: 110,
    cellRenderer: (p: any) => {
      const colorMap: Record<number, string> = { 1: '#67c23a', 2: '#f56c6c', 3: '#e6a23c' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${resultLabel(p.value)}</span>`
    },
    cellStyle: (p: any) => {
      const bgMap: Record<number, string> = { 1: '#f0f9eb', 2: '#fef0f0', 3: '#fdf6ec' }
      return { backgroundColor: bgMap[p.value] ?? undefined }
    },
  },
  { field: 'inspector', headerName: '检验员', width: 100 },
  { field: 'inspectTime', headerName: '检验时间', width: 170 },
  {
    headerName: '操作', width: 180, pinned: 'right', sortable: false, filter: false,
    cellRenderer: (p: any) => `<button class="action-btn primary" data-action="edit">编辑</button><button class="action-btn danger" data-action="delete">删除</button>`,
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      if (action === 'edit') { showEdit(p.data.id) }
      else if (action === 'delete') { handleDelete(p.data.id) }
    },
  },
])

function onGridReady(params: any) { gridApi = params.api; loadData() }

async function loadData() {
  try {
    const res = await getQcRecordPage({ ...query, page: query.page, size: query.size })
    rowData.value = res.data.records
  } catch {}
}
function resetQuery() { Object.assign(query, { relateType: undefined, relateId: '', inspectItem: '', result: undefined }); loadData() }
function showAdd() {
  isEdit.value = false; editingId.value = ''
  Object.assign(form, { relateType: 1, relateId: '', inspectItem: '', inspectValue: '', standardValue: '', result: 1, inspectTime: '', inspector: '', failReason: '', disposeMethod: '' })
  dialogVisible.value = true
}
async function showEdit(id: string) {
  isEdit.value = true; editingId.value = id
  try {
    const res = await getQcRecord(id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch {}
}
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await updateQcRecord(editingId.value, form); ElMessage.success('修改成功') }
    else { await createQcRecord(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch {} finally { saving.value = false }
}
async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该质检记录？')
    await deleteQcRecord(id); ElMessage.success('删除成功'); loadData()
  } catch {}
}
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
:deep(.ag-theme-quartz) { --ag-header-height: 40px; --ag-row-height: 38px; font-size: 13px; }
</style>
