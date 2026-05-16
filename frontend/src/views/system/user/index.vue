<template>
  <div class="user-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="用户名">
          <el-input v-model="filter.username" placeholder="用户名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filter.realName" placeholder="姓名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openAdd">新增用户</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="角色分配">
          <el-select v-model="form.roleIds" multiple style="width:100%" placeholder="请选择角色">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AgGridVue } from 'ag-grid-vue3'
import type { ColDef, GridApi } from 'ag-grid-community'
import dayjs from 'dayjs'
import { getUserPage, createUser, updateUser, deleteUser } from '../../../api/modules/user'
import { getRoleList } from '../../../api/modules/role'
import type { Role } from '../../../api/modules/role'

interface UserRecord {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  enabled: number
  createTime: string
  roleIds?: number[]
}

const loading = ref(false)
const rowData = ref<UserRecord[]>([])
let gridApi: GridApi | null = null

const filter = ref({
  username: '',
  realName: '',
})

const defaultColDef: ColDef = {
  sortable: true,
  resizable: true,
  filter: true,
}

const columnDefs = ref<ColDef[]>([
  { field: 'username', headerName: '用户名', width: 130 },
  { field: 'realName', headerName: '姓名', width: 110 },
  { field: 'email', headerName: '邮箱', width: 180 },
  { field: 'phone', headerName: '手机号', width: 130 },
  {
    field: 'enabled',
    headerName: '启用',
    width: 80,
    cellRenderer: (p: any) => {
      return `<el-switch :model-value="${p.value === 1}" disabled />`
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
    width: 160,
    pinned: 'right',
    cellRenderer: (p: any) => `
      <button class="action-btn primary" data-action="edit">编辑</button>
      <button class="action-btn danger" data-action="delete">删除</button>`,
    onCellClicked: (p: any) => {
      const action = p.event.target?.dataset?.action
      const id = p.data.id
      if (action === 'edit') { win.__userEdit(id) }
      else if (action === 'delete') { win.__userDelete(id) }
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
    if (filter.value.username) params.username = filter.value.username
    if (filter.value.realName) params.realName = filter.value.realName
    const res = await getUserPage(params)
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
  filter.value = { username: '', realName: '' }
  loadData()
}

// Role options
const roleOptions = ref<Role[]>([])

onMounted(async () => {
  try {
    const res = await getRoleList()
    roleOptions.value = res.data || []
  } catch {
    roleOptions.value = []
  }
})

// Add / Edit dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const currentId = ref<number | null>(null)

const form = ref({
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  enabled: 1 as number,
  roleIds: [] as number[],
})

function openAdd() {
  isEdit.value = false
  currentId.value = null
  form.value = { username: '', realName: '', email: '', phone: '', password: '', enabled: 1, roleIds: [] }
  dialogVisible.value = true
}

const win = window as any

win.__userEdit = async (id: number) => {
  isEdit.value = true
  currentId.value = id
  // Try to get user detail, fallback to grid data
  try {
    const { getUser } = await import('../../../api/modules/user')
    const res = await getUser(String(id))
    const data = res.data
    form.value = {
      username: data.username,
      realName: data.realName || '',
      email: (data as any).email || '',
      phone: (data as any).phone || '',
      password: '',
      enabled: (data as any).enabled ?? 1,
      roleIds: data.roles?.map((r: any) => (typeof r === 'object' ? r.id : r)) || [],
    }
  } catch {
    ElMessage.error('获取用户信息失败')
    return
  }
  dialogVisible.value = true
}

win.__userDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该用户？', '提示')
    await deleteUser(String(id))
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // cancelled or error
  }
}

async function handleSave() {
  if (!form.value.username || !form.value.realName) {
    ElMessage.warning('请填写用户名和姓名')
    return
  }
  saving.value = true
  try {
    if (isEdit.value && currentId.value) {
      const { username, password, ...updateData } = form.value
      await updateUser(String(currentId.value), updateData)
      ElMessage.success('更新成功')
    } else {
      await createUser(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.user-view {
  padding: 0;
}
</style>
