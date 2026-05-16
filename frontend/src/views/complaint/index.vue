<template>
  <div class="complaint-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="投诉单号">
          <el-input v-model="filter.complaintId" placeholder="投诉单号" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="filter.customerName" placeholder="客户名称" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width:120px">
            <el-option label="登记" :value="0" />
            <el-option label="追溯中" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="复检中" :value="3" />
            <el-option label="已关闭" :value="4" />
            <el-option label="已归档" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addVisible = true">新增投诉</el-button>
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

    <!-- Add Complaint Dialog -->
    <el-dialog v-model="addVisible" title="新增投诉" width="500px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="投诉单号" required>
          <el-input v-model="addForm.complaintId" placeholder="自动生成或手动输入" />
        </el-form-item>
        <el-form-item label="客户名称" required>
          <el-input v-model="addForm.customerName" placeholder="客户名称" />
        </el-form-item>
        <el-form-item label="卷号">
          <el-input v-model="addForm.coilId" placeholder="关联卷号" />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="addForm.severity" style="width:100%">
            <el-option label="一般" :value="1" />
            <el-option label="较重" :value="2" />
            <el-option label="严重" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="addForm.problemDesc" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="handleAdd">确认</el-button>
      </template>
    </el-dialog>

    <!-- Assign Responsible Dialog -->
    <el-dialog v-model="assignVisible" title="责任判定" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="责任部门">
          <el-input v-model="assignForm.responsibleDept" placeholder="请输入责任部门" />
        </el-form-item>
        <el-form-item label="根本原因">
          <el-input v-model="assignForm.rootCause" type="textarea" :rows="3" placeholder="请输入根本原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确认</el-button>
      </template>
    </el-dialog>

    <!-- Measures Dialog -->
    <el-dialog v-model="measureVisible" title="措施制定" width="500px">
      <el-form :model="measureForm" label-width="100px">
        <el-form-item label="纠正措施">
          <el-input v-model="measureForm.correctiveMeasures" type="textarea" :rows="4" placeholder="请输入纠正措施" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="measureVisible = false">取消</el-button>
        <el-button type="primary" :loading="measuring" @click="handleMeasure">确认</el-button>
      </template>
    </el-dialog>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewVisible" title="复核" width="500px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="复核意见">
          <el-input v-model="reviewForm.reviewOpinion" type="textarea" :rows="4" placeholder="请输入复核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="handleReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef, GridApi } from 'ag-grid-community'
import dayjs from 'dayjs'
import {
  getComplaintPage,
  createComplaint,
  traceComplaint,
  assignComplaintResponsible,
  updateComplaintMeasure,
  reviewComplaint,
  closeComplaint,
  archiveComplaint,
} from '../../api/modules/complaint'
import type { Complaint } from '../../types'

const loading = ref(false)
const rowData = ref<Complaint[]>([])
let gridApi: GridApi | null = null

const filter = ref({
  complaintId: '',
  customerName: '',
  status: undefined as number | undefined,
})

const defaultColDef: ColDef = {
  sortable: true,
  resizable: true,
  filter: true,
}

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '登记', type: 'info' },
  1: { label: '追溯中', type: 'primary' },
  2: { label: '处理中', type: 'warning' },
  3: { label: '复检中', type: '' },
  4: { label: '已关闭', type: 'success' },
  5: { label: '已归档', type: '' },
}

const columnDefs = ref<ColDef[]>([
  { field: 'complaintId', headerName: '投诉单号', width: 140 },
  { field: 'customerName', headerName: '客户名称', width: 130 },
  { field: 'coilId', headerName: '卷号', width: 130 },
  {
    field: 'severity',
    headerName: '严重程度',
    width: 90,
    cellRenderer: (p: any) => {
      const map: Record<number, string> = { 1: '一般', 2: '较重', 3: '严重' }
      const typeMap: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger' }
      return `<span class="ag-tag ag-tag-${typeMap[p.value] || 'default'}">${map[p.value] || '未知'}</span>`
    },
  },
  {
    field: 'status',
    headerName: '状态',
    width: 180,
    cellRenderer: (p: any) => {
      const status = p.value as number
      const steps = [0, 1, 2, 3, 4, 5]
      return steps
        .map((s) => {
          const info = statusMap[s]
          const active = s <= status
          const current = s === status
          return `<span style="display:inline-flex;align-items:center;margin-right:2px">
            <span style="display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:2px;${
              current ? 'background:#1a73e8;border:2px solid #1a73e8;' : active ? 'background:#67c23a;' : 'background:#ddd;'
            }"></span>
            <span style="font-size:12px;${current ? 'color:#1a73e8;font-weight:600' : active ? 'color:#67c23a' : 'color:#999'}">${info.label}</span>
          </span>`
        })
        .join('')
    },
  },
  {
    field: 'createTime',
    headerName: '创建时间',
    width: 170,
    valueFormatter: (p) => (p.value ? dayjs(p.value).format('YYYY-MM-DD HH:mm') : ''),
  },
  {
    headerName: '操作',
    width: 250,
    pinned: 'right',
    cellRenderer: (p: any) => {
      const status = p.data.status
      let btns = ''
      if (status === 0) {
        btns += `<button class="action-btn primary" data-action="trace">追溯</button> `
      }
      if (status === 1) {
        btns += `<button class="action-btn warning" data-action="assign">责任判定</button> `
      }
      if (status === 2) {
        btns += `<button class="action-btn" data-action="measure">措施制定</button> `
      }
      if (status === 3) {
        btns += `<button class="action-btn primary" data-action="review">复核</button> `
      }
      if (status === 4) {
        btns += `<button class="action-btn" data-action="archive">归档</button>`
      }
      return btns
    },
    onCellClicked: async (p: any) => {
      const action = p.event.target?.dataset?.action
      const data = p.data as Complaint
      const id = data.id
      if (action === 'trace') {
        try {
          await traceComplaint(String(id))
          ElMessage.success('追溯任务已触发')
          loadData()
        } catch {}
      } else if (action === 'assign') {
        currentComplaintId = id
        assignForm.value = { responsibleDept: '', rootCause: '' }
        assignVisible.value = true
      } else if (action === 'measure') {
        currentComplaintId = id
        measureForm.value = { correctiveMeasures: '' }
        measureVisible.value = true
      } else if (action === 'review') {
        currentComplaintId = id
        reviewForm.value = { reviewOpinion: '' }
        reviewVisible.value = true
      } else if (action === 'archive') {
        try {
          await ElMessageBox.confirm('确认归档该投诉？归档后不可修改。', '提示')
          await archiveComplaint(String(id))
          ElMessage.success('归档成功')
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
    if (filter.value.complaintId) params.complaintId = filter.value.complaintId
    if (filter.value.customerName) params.customerName = filter.value.customerName
    if (filter.value.status !== undefined) params.status = filter.value.status
    const res = await getComplaintPage(params)
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
  filter.value = { complaintId: '', customerName: '', status: undefined }
  loadData()
}

// Add complaint
const addVisible = ref(false)
const addForm = ref({
  complaintId: '',
  customerName: '',
  coilId: '',
  severity: 1,
  problemDesc: '',
})
const adding = ref(false)

async function handleAdd() {
  if (!addForm.value.complaintId || !addForm.value.customerName) {
    ElMessage.warning('请填写投诉单号和客户名称')
    return
  }
  adding.value = true
  try {
    await createComplaint(addForm.value)
    ElMessage.success('新增成功')
    addVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  } finally {
    adding.value = false
  }
}

// Assign responsible
const assignVisible = ref(false)
const assignForm = ref({ responsibleDept: '', rootCause: '' })
const assigning = ref(false)
let currentComplaintId: number | null = null

// Measures
const measureVisible = ref(false)
const measureForm = ref({ correctiveMeasures: '' })
const measuring = ref(false)

// Review
const reviewVisible = ref(false)
const reviewForm = ref({ reviewOpinion: '' })
const reviewing = ref(false)

const win = window as any

win.__complaintTrace = async (id: number) => {
  try {
    await traceComplaint(String(id))
    ElMessage.success('追溯任务已触发')
    loadData()
  } catch {
    // handled by interceptor
  }
}

win.__complaintAssign = (id: number) => {
  currentComplaintId = id
  assignForm.value = { responsibleDept: '', rootCause: '' }
  assignVisible.value = true
}

win.__complaintMeasure = (id: number) => {
  currentComplaintId = id
  measureForm.value = { correctiveMeasures: '' }
  measureVisible.value = true
}

win.__complaintReview = (id: number) => {
  currentComplaintId = id
  reviewForm.value = { reviewOpinion: '' }
  reviewVisible.value = true
}

win.__complaintArchive = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认归档该投诉？归档后不可修改。', '提示')
    await archiveComplaint(String(id))
    ElMessage.success('归档成功')
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
    await assignComplaintResponsible(String(currentComplaintId), assignForm.value)
    ElMessage.success('责任判定完成')
    assignVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  } finally {
    assigning.value = false
  }
}

async function handleMeasure() {
  if (!measureForm.value.correctiveMeasures) {
    ElMessage.warning('请输入纠正措施')
    return
  }
  measuring.value = true
  try {
    await updateComplaintMeasure(String(currentComplaintId), measureForm.value)
    ElMessage.success('措施制定完成')
    measureVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  } finally {
    measuring.value = false
  }
}

async function handleReview() {
  if (!reviewForm.value.reviewOpinion) {
    ElMessage.warning('请输入复核意见')
    return
  }
  reviewing.value = true
  try {
    await reviewComplaint(String(currentComplaintId), reviewForm.value)
    ElMessage.success('复核完成')
    reviewVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  } finally {
    reviewing.value = false
  }
}
</script>

<style scoped>
.complaint-view {
  padding: 0;
}
</style>
