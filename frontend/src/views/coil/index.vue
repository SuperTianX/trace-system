<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="钢卷号"><el-input v-model="query.coilId" placeholder="钢卷号" clearable /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="query.batchId" placeholder="批次号" clearable /></el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="query.stockStatus" placeholder="库存状态" clearable style="width:120px">
            <el-option label="在库" :value="0" />
            <el-option label="已出库" :value="1" />
            <el-option label="锁定" :value="2" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑钢卷' : '新增钢卷'" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="钢卷号" prop="coilId"><el-input v-model="form.coilId" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格"><el-input v-model="form.specifications" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="重量(吨)"><el-input-number v-model="form.weight" :precision="3" :step="0.1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="材质"><el-input v-model="form.material" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="质量等级"><el-input v-model="form.qualityGrade" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchId" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="库位"><el-input v-model="form.storageLocation" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入库单号"><el-input v-model="form.inboundOrderNo" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="出库单号"><el-input v-model="form.outboundOrderNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customerName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存状态">
              <el-select v-model="form.stockStatus" style="width:100%">
                <el-option label="在库" :value="0" />
                <el-option label="已出库" :value="1" />
                <el-option label="锁定" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生命周期">
              <el-select v-model="form.lifecycleStatus" style="width:100%">
                <el-option label="生产" :value="0" />
                <el-option label="质检" :value="1" />
                <el-option label="入库" :value="2" />
                <el-option label="出库" :value="3" />
                <el-option label="异议" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="客户ID"><el-input v-model="form.customerId" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="钢卷详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="钢卷号">{{ detailData.coilId }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ detailData.specifications }}</el-descriptions-item>
        <el-descriptions-item label="重量(吨)">{{ detailData.weight }}</el-descriptions-item>
        <el-descriptions-item label="材质">{{ detailData.material }}</el-descriptions-item>
        <el-descriptions-item label="质量等级">{{ detailData.qualityGrade }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ detailData.batchId }}</el-descriptions-item>
        <el-descriptions-item label="库位">{{ detailData.storageLocation }}</el-descriptions-item>
        <el-descriptions-item label="入库单号">{{ detailData.inboundOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="出库单号">{{ detailData.outboundOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="库存状态">
          <el-tag :type="stockStatusTag(detailData.stockStatus)" size="small">{{ stockStatusLabel(detailData.stockStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生命周期">
          <el-tag :type="lifecycleStatusTag(detailData.lifecycleStatus)" size="small">{{ lifecycleStatusLabel(detailData.lifecycleStatus) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">生命周期轨迹</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in lifecycleTimeline"
          :key="index"
          :timestamp="item.time"
          :type="item.type"
          :hollow="item.hollow"
          placement="top"
        >
          {{ item.label }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef } from 'ag-grid-community'
import { getCoilPage, getCoil, createCoil, updateCoil, deleteCoil } from '../../api/modules/coil'

const rowData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const saving = ref(false)
const detailVisible = ref(false)
const detailData = ref<any>({})
let gridApi: any = null

const query = reactive({ coilId: '', batchId: '', stockStatus: undefined as number | undefined, page: 1, size: 20 })
const form = reactive<any>({
  coilId: '', specifications: '', weight: null, material: '', qualityGrade: '',
  batchId: '', storageLocation: '', inboundOrderNo: '', outboundOrderNo: '',
  customerId: '', customerName: '', stockStatus: 0, lifecycleStatus: 0,
})
const rules = { coilId: [{ required: true, message: '钢卷号不能为空' }] }

function stockStatusLabel(val: number): string {
  const map: Record<number, string> = { 0: '在库', 1: '已出库', 2: '锁定' }
  return map[val] ?? '未知'
}
function stockStatusTag(val: number): string {
  const map: Record<number, string> = { 0: 'success', 1: 'info', 2: 'danger' }
  return map[val] ?? ''
}
function lifecycleStatusLabel(val: number): string {
  const map: Record<number, string> = { 0: '生产', 1: '质检', 2: '入库', 3: '出库', 4: '异议' }
  return map[val] ?? '未知'
}
function lifecycleStatusTag(val: number): string {
  const map: Record<number, string> = { 0: '', 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }
  return map[val] ?? ''
}

const lifecycleTimeline = computed(() => {
  const status = detailData.value.lifecycleStatus
  const stages = [
    { label: '生产阶段', time: detailData.value.inboundOrderNo ? '已入库' : '进行中', type: '' as const, hollow: status < 0 },
    { label: '质检阶段', time: detailData.value.qualityGrade ? '已质检' : '-', type: 'primary' as const, hollow: status < 1 },
    { label: '入库阶段', time: detailData.value.inboundOrderNo ? detailData.value.inboundOrderNo : '-', type: 'success' as const, hollow: status < 2 },
    { label: '出库阶段', time: detailData.value.outboundOrderNo ? detailData.value.outboundOrderNo : '-', type: 'info' as const, hollow: status < 3 },
    { label: '异议阶段', time: detailData.value.customerName ? detailData.value.customerName : '-', type: 'danger' as const, hollow: status < 4 },
  ]
  return stages.filter((_, i) => i <= status)
})

const defaultColDef = { sortable: true, resizable: true, filter: true, minWidth: 100 }
const columnDefs = ref<ColDef[]>([
  {
    field: 'coilId', headerName: '钢卷号', pinned: 'left', width: 150,
    cellRenderer: (p: any) => `<a style="color:#409eff;cursor:pointer;text-decoration:none">${p.value}</a>`,
    onCellClicked: (p: any) => { showDetail(p.data) },
  },
  { field: 'specifications', headerName: '规格', width: 120 },
  { field: 'weight', headerName: '重量(吨)', width: 110 },
  { field: 'material', headerName: '材质', width: 100 },
  { field: 'batchId', headerName: '批次号', width: 140 },
  {
    field: 'stockStatus', headerName: '库存状态', width: 100,
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 0: '在库', 1: '已出库', 2: '锁定' }
      const colorMap: Record<number, string> = { 0: '#67c23a', 1: '#909399', 2: '#f56c6c' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${map[p.value] ?? '未知'}</span>`
    },
    cellStyle: (p: any) => {
      const bgMap: Record<number, string> = { 0: '#f0f9eb', 1: '#f4f4f5', 2: '#fef0f0' }
      return { backgroundColor: bgMap[p.value] ?? undefined }
    },
  },
  {
    field: 'lifecycleStatus', headerName: '生命周期', width: 100,
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 0: '生产', 1: '质检', 2: '入库', 3: '出库', 4: '异议' }
      const colorMap: Record<number, string> = { 0: '#909399', 1: '#e6a23c', 2: '#67c23a', 3: '#409eff', 4: '#f56c6c' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${map[p.value] ?? '未知'}</span>`
    },
  },
  {
    headerName: '操作', width: 180, pinned: 'right', sortable: false, filter: false,
    cellRenderer: (p: any) => `<button class="action-btn primary" data-action="edit">编辑</button><button class="action-btn danger" data-action="delete">删除</button>`,
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      if (action === 'edit') { showEdit(p.data.coilId) }
      else if (action === 'delete') { handleDelete(p.data.coilId) }
    },
  },
])

function onGridReady(params: any) { gridApi = params.api; loadData() }

async function loadData() {
  try {
    const res = await getCoilPage({ ...query, page: query.page, size: query.size })
    rowData.value = res.data.records
  } catch {}
}
function resetQuery() { Object.assign(query, { coilId: '', batchId: '', stockStatus: undefined }); loadData() }
function showAdd() {
  isEdit.value = false; editingId.value = ''
  Object.assign(form, {
    coilId: '', specifications: '', weight: null, material: '', qualityGrade: '',
    batchId: '', storageLocation: '', inboundOrderNo: '', outboundOrderNo: '',
    customerId: '', customerName: '', stockStatus: 0, lifecycleStatus: 0,
  })
  dialogVisible.value = true
}
async function showEdit(id: string) {
  isEdit.value = true; editingId.value = id
  try {
    const res = await getCoil(id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch {}
}
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await updateCoil(editingId.value, form); ElMessage.success('修改成功') }
    else { await createCoil(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch {} finally { saving.value = false }
}
async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该钢卷？')
    await deleteCoil(id); ElMessage.success('删除成功'); loadData()
  } catch {}
}
async function showDetail(data: any) {
  try {
    const res = await getCoil(data.coilId)
    detailData.value = res.data
    detailVisible.value = true
  } catch {}
}
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
:deep(.ag-theme-quartz) { --ag-header-height: 40px; --ag-row-height: 38px; font-size: 13px; }
.el-divider { margin: 16px 0; }
</style>
