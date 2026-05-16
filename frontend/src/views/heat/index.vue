<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="炉号"><el-input v-model="query.heatId" placeholder="炉号" clearable /></el-form-item>
        <el-form-item label="钢种"><el-input v-model="query.steelGrade" placeholder="钢种" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
            <el-option label="正常" :value="0" />
            <el-option label="异常" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showAdd">新增</el-button>
          <el-button type="warning" @click="importVisible = true">批量导入</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑炉次' : '新增炉次'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="炉号" prop="heatId"><el-input v-model="form.heatId" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="冶炼日期" prop="smeltDate"><el-date-picker v-model="form.smeltDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="钢种"><el-input v-model="form.steelGrade" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="班组"><el-input v-model="form.shiftGroup" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="碳含量"><el-input-number v-model="form.cContent" :precision="4" :step="0.01" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="硅含量"><el-input-number v-model="form.siContent" :precision="4" :step="0.01" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.status === 1" label="异常说明"><el-input v-model="form.abnormalDesc" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Batch Import Dialog -->
    <el-dialog v-model="importVisible" title="批量导入炉次" width="450px">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="onFileChange"
      >
        <el-icon :size="48" color="#999"><UploadFilled /></el-icon>
        <div style="margin-top:8px">将Excel文件拖到此处，或点击上传</div>
        <template #tip>
          <div style="font-size:12px;color:#999;margin-top:4px">
            仅支持 .xlsx / .xls 文件，表头需包含：炉号、冶炼日期、钢种、碳含量、硅含量等字段
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef } from 'ag-grid-community'
import { getHeatPage, getHeat, createHeat, updateHeat, deleteHeat, batchImportHeat } from '../../api/modules/heat'
import { UploadFilled } from '@element-plus/icons-vue'

const rowData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const saving = ref(false)
let gridApi: any = null

const query = reactive({ heatId: '', steelGrade: '', status: undefined as number | undefined, page: 1, size: 20 })
const form = reactive<any>({ heatId: '', smeltDate: '', steelGrade: '', shiftGroup: '', cContent: null, siContent: null, status: 0, abnormalDesc: '' })
const rules = { heatId: [{ required: true, message: '炉号不能为空' }], smeltDate: [{ required: true, message: '冶炼日期不能为空' }] }
const defaultColDef = { sortable: true, resizable: true, filter: true, minWidth: 100 }
const columnDefs = ref<ColDef[]>([
  { field: 'heatId', headerName: '炉号', pinned: 'left', width: 140 },
  { field: 'steelGrade', headerName: '钢种', width: 100 },
  { field: 'smeltDate', headerName: '冶炼日期', width: 120 },
  { field: 'shiftGroup', headerName: '班组', width: 100 },
  { field: 'cContent', headerName: '碳含量', width: 100 },
  { field: 'siContent', headerName: '硅含量', width: 100 },
  {
    field: 'status', headerName: '状态', width: 90,
    cellRenderer: (p: any) => p.value === 0 ? '<span style="color:#67c23a">正常</span>' : '<span style="color:#f56c6c">异常</span>',
    cellStyle: (p: any) => p.value === 1 ? { backgroundColor: '#fef0f0' } : undefined,
  },
  {
    headerName: '操作', width: 160, pinned: 'right', sortable: false, filter: false,
    cellRenderer: (p: any) => `<button class="action-btn primary" data-action="edit">编辑</button><button class="action-btn danger" data-action="delete">删除</button>`,
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      if (action === 'edit') showEdit(p.data.heatId)
      else if (action === 'delete') handleDelete(p.data.heatId)
    },
  },
])

function onGridReady(params: any) { gridApi = params.api; loadData() }

async function loadData() {
  try {
    const res = await getHeatPage({ ...query, page: query.page, size: query.size })
    rowData.value = res.data.records
  } catch {}
}
function resetQuery() { Object.assign(query, { heatId: '', steelGrade: '', status: undefined }); loadData() }
function showAdd() { isEdit.value = false; editingId.value = ''; Object.assign(form, { heatId: '', smeltDate: '', steelGrade: '', shiftGroup: '', cContent: null, siContent: null, status: 0, abnormalDesc: '' }); dialogVisible.value = true }
async function showEdit(id: string) {
  isEdit.value = true; editingId.value = id
  try {
    const res = await getHeat(id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch {}
}
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await updateHeat(editingId.value, form); ElMessage.success('修改成功') }
    else { await createHeat(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch {} finally { saving.value = false }
}
async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该炉次？')
    await deleteHeat(id); ElMessage.success('删除成功'); loadData()
  } catch {}
}

// Batch import
const importVisible = ref(false)
const importing = ref(false)
const uploadRef = ref<any>(null)
let importFile: File | null = null

function onFileChange(file: any) {
  importFile = file.raw
}

async function handleImport() {
  if (!importFile) { ElMessage.warning('请选择文件'); return }
  importing.value = true
  try {
    await batchImportHeat(importFile)
    ElMessage.success('导入成功')
    importVisible.value = false
    importFile = null
    loadData()
  } catch {
    // handled by interceptor
  } finally {
    importing.value = false
  }
}
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
:deep(.ag-theme-quartz) { --ag-header-height: 40px; --ag-row-height: 38px; font-size: 13px; }
</style>
