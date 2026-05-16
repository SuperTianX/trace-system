<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="出入库单据" name="inventory">
          <template #label>
            <span><el-icon style="vertical-align:-2px;margin-right:4px"><Document /></el-icon>出入库单据</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="库存台账" name="stock">
          <template #label>
            <span><el-icon style="vertical-align:-2px;margin-right:4px"><List /></el-icon>库存台账</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- Inventory Tab Filters -->
      <el-form v-if="activeTab === 'inventory'" :model="invQuery" inline style="margin-top:12px">
        <el-form-item label="单据号"><el-input v-model="invQuery.docNo" placeholder="单据号" clearable /></el-form-item>
        <el-form-item label="单据类型">
          <el-select v-model="invQuery.docType" placeholder="单据类型" clearable style="width:120px">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号"><el-input v-model="invQuery.batchId" placeholder="批次号" clearable /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadInventory">查询</el-button>
          <el-button @click="resetInvQuery">重置</el-button>
          <el-button type="success" @click="showAdd">新增</el-button>
        </el-form-item>
      </el-form>

      <!-- Stock Tab Filters -->
      <el-form v-if="activeTab === 'stock'" :model="stockQuery" inline style="margin-top:12px">
        <el-form-item label="钢卷号"><el-input v-model="stockQuery.coilId" placeholder="钢卷号" clearable /></el-form-item>
        <el-form-item label="仓库"><el-input v-model="stockQuery.warehouse" placeholder="仓库" clearable /></el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="stockQuery.stockStatus" placeholder="库存状态" clearable style="width:120px">
            <el-option label="在库" :value="0" />
            <el-option label="已出库" :value="1" />
            <el-option label="锁定" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStock">查询</el-button>
          <el-button @click="resetStockQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <!-- Inventory AG Grid -->
      <ag-grid-vue
        v-if="activeTab === 'inventory'"
        class="ag-theme-quartz"
        style="height:500px"
        :rowData="invRowData"
        :columnDefs="invColumnDefs"
        :defaultColDef="defaultColDef"
        :pagination="true"
        :paginationPageSize="20"
        @grid-ready="onInvGridReady"
      />

      <!-- Stock AG Grid -->
      <ag-grid-vue
        v-if="activeTab === 'stock'"
        class="ag-theme-quartz"
        style="height:500px"
        :rowData="stockRowData"
        :columnDefs="stockColumnDefs"
        :defaultColDef="defaultColDef"
        :pagination="true"
        :paginationPageSize="20"
        @grid-ready="onStockGridReady"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增单据" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单据号" prop="docNo"><el-input v-model="form.docNo" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单据类型" prop="docType">
              <el-select v-model="form.docType" style="width:100%">
                <el-option label="入库" :value="1" />
                <el-option label="出库" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="钢卷号"><el-input v-model="form.coilId" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="数量"><el-input-number v-model="form.quantity" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库"><el-input v-model="form.warehouse" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="操作员"><el-input v-model="form.operator" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作时间"><el-date-picker v-model="form.operateTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
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
import { ElMessage } from 'element-plus'
import { Document, List } from '@element-plus/icons-vue'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef } from 'ag-grid-community'
import { getInventoryPage, createInventory, approveInventory, voidInventory, getStockPage } from '../../api/modules/inventory'

const activeTab = ref('inventory')

// Inventory state
const invRowData = ref([])
let invGridApi: any = null
const invQuery = reactive({ docNo: '', docType: undefined as number | undefined, batchId: '', page: 1, size: 20 })

// Stock state
const stockRowData = ref([])
let stockGridApi: any = null
const stockQuery = reactive({ coilId: '', warehouse: '', stockStatus: undefined as number | undefined, page: 1, size: 20 })

// Dialog state
const dialogVisible = ref(false)
const saving = ref(false)

const form = reactive<any>({ docNo: '', docType: 1, batchId: '', coilId: '', quantity: null, warehouse: '', operator: '', operateTime: '' })
const rules = {
  docNo: [{ required: true, message: '单据号不能为空' }],
  docType: [{ required: true, message: '单据类型不能为空' }],
}

const defaultColDef = { sortable: true, resizable: true, filter: true, minWidth: 100 }

function docTypeLabel(val: number): string {
  const map: Record<number, string> = { 1: '入库', 2: '出库' }
  return map[val] ?? '未知'
}

const invColumnDefs = ref<ColDef[]>([
  { field: 'docNo', headerName: '单据号', pinned: 'left', width: 160 },
  {
    field: 'docType', headerName: '单据类型', width: 100,
    cellRenderer: (p: any) => {
      const colorMap: Record<number, string> = { 1: '#67c23a', 2: '#e6a23c' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${docTypeLabel(p.value)}</span>`
    },
    cellStyle: (p: any) => {
      const bgMap: Record<number, string> = { 1: '#f0f9eb', 2: '#fdf6ec' }
      return { backgroundColor: bgMap[p.value] ?? undefined }
    },
  },
  { field: 'batchId', headerName: '批次号', width: 140 },
  { field: 'coilId', headerName: '钢卷号', width: 150 },
  { field: 'quantity', headerName: '数量', width: 90 },
  { field: 'warehouse', headerName: '仓库', width: 100 },
  {
    field: 'status', headerName: '状态', width: 90,
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 0: '草稿', 1: '已审核', 2: '已作废' }
      const colorMap: Record<number, string> = { 0: '#909399', 1: '#67c23a', 2: '#f56c6c' }
      return `<span style="color:${colorMap[p.value] ?? '#333'}">${map[p.value] ?? '未知'}</span>`
    },
  },
  { field: 'operator', headerName: '操作员', width: 100 },
  { field: 'operateTime', headerName: '操作时间', width: 170 },
  {
    headerName: '操作', width: 220, pinned: 'right', sortable: false, filter: false,
    cellRenderer: (p: any) => {
      const status = p.data.status
      let btns = ''
      if (status === 0) {
        btns += `<button class="action-btn success" data-action="approve">审核</button>`
        btns += `<button class="action-btn warning" data-action="void">作废</button>`
      }
      return btns
    },
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      if (action === 'approve') { handleApprove(p.data.id) }
      else if (action === 'void') { handleVoid(p.data.id) }
    },
  },
])

const stockColumnDefs = ref<ColDef[]>([
  { field: 'coilId', headerName: '钢卷号', pinned: 'left', width: 150 },
  { field: 'warehouse', headerName: '仓库', width: 100 },
  { field: 'location', headerName: '库位', width: 120 },
  { field: 'quantity', headerName: '数量', width: 90 },
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
])

function onInvGridReady(params: any) { invGridApi = params.api; loadInventory() }
function onStockGridReady(params: any) { stockGridApi = params.api; loadStock() }

function onTabChange() {
  if (activeTab.value === 'inventory') {
    if (invGridApi) loadInventory()
  } else {
    if (stockGridApi) loadStock()
  }
}

async function loadInventory() {
  try {
    const res = await getInventoryPage({ ...invQuery, page: invQuery.page, size: invQuery.size })
    invRowData.value = res.data.records
  } catch {}
}
function resetInvQuery() { Object.assign(invQuery, { docNo: '', docType: undefined, batchId: '' }); loadInventory() }

async function loadStock() {
  try {
    const res = await getStockPage({ ...stockQuery, page: stockQuery.page, size: stockQuery.size })
    stockRowData.value = res.data.records
  } catch {}
}
function resetStockQuery() { Object.assign(stockQuery, { coilId: '', warehouse: '', stockStatus: undefined }); loadStock() }

function showAdd() {
  Object.assign(form, { docNo: '', docType: 1, batchId: '', coilId: '', quantity: null, warehouse: '', operator: '', operateTime: '' })
  dialogVisible.value = true
}
async function handleSave() {
  saving.value = true
  try {
    await createInventory(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    loadInventory()
  } catch {} finally { saving.value = false }
}
async function handleApprove(id: string) {
  try {
    await approveInventory(id)
    ElMessage.success('审核通过')
    loadInventory()
  } catch {}
}
async function handleVoid(id: string) {
  try {
    await voidInventory(id)
    ElMessage.success('已作废')
    loadInventory()
  } catch {}
}
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
:deep(.ag-theme-quartz) { --ag-header-height: 40px; --ag-row-height: 38px; font-size: 13px; }
:deep(.el-tabs__header) { margin-bottom: 0; }
</style>
