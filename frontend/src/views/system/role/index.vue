<template>
  <div class="role-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="角色名称">
          <el-input v-model="filter.roleName" placeholder="角色名称" clearable style="width:150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openAdd">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openPermission(row)">权限分配</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="450px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" placeholder="角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="form.roleCode" placeholder="角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="角色描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Permission Assignment Dialog -->
    <el-dialog v-model="permVisible" title="权限分配" width="500px">
      <el-tree
        ref="treeRef"
        :data="permTreeData"
        :props="{ label: 'label', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedPermKeys"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSaving" @click="handleSavePerm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElTree } from 'element-plus'
import { getRolePage, createRole, updateRole, deleteRole, getAllMenus, getRoleMenus, assignRoleMenus } from '../../../api/modules/role'
import type { MenuItem } from '../../../api/modules/role'

interface RoleRecord {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
}

const loading = ref(false)
const tableData = ref<RoleRecord[]>([])

const filter = ref({
  roleName: '',
})

async function loadData() {
  loading.value = true
  try {
    const params: any = { page: 1, size: 50 }
    if (filter.value.roleName) params.roleName = filter.value.roleName
    const res = await getRolePage(params)
    tableData.value = res.data?.records || []
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function handleReset() {
  filter.value = { roleName: '' }
  loadData()
}

// Add / Edit dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const currentId = ref<number | null>(null)

const form = ref({
  roleName: '',
  roleCode: '',
  description: '',
  status: 1 as number,
})

function openAdd() {
  isEdit.value = false
  currentId.value = null
  form.value = { roleName: '', roleCode: '', description: '', status: 1 }
  dialogVisible.value = true
}

function openEdit(row: RoleRecord) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    roleName: row.roleName,
    roleCode: row.roleCode,
    description: row.description || '',
    status: row.status,
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.roleName || !form.value.roleCode) {
    ElMessage.warning('请填写角色名称和编码')
    return
  }
  saving.value = true
  try {
    if (isEdit.value && currentId.value) {
      await updateRole(String(currentId.value), {
        roleName: form.value.roleName,
        description: form.value.description,
        status: form.value.status,
      })
      ElMessage.success('更新成功')
    } else {
      await createRole({
        roleName: form.value.roleName,
        roleCode: form.value.roleCode,
        description: form.value.description,
        status: form.value.status,
      })
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

async function handleDelete(row: RoleRecord) {
  try {
    await ElMessageBox.confirm('确认删除该角色？', '提示')
    await deleteRole(String(row.id))
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // cancelled or error
  }
}

// Permission tree
const permVisible = ref(false)
const permSaving = ref(false)
const treeRef = ref<InstanceType<typeof ElTree>>()
const checkedPermKeys = ref<number[]>([])
let currentPermRoleId: number | null = null
const permTreeData = ref<any[]>([])

/** 将扁平菜单列表转换为树形结构 */
function buildMenuTree(menus: MenuItem[]): any[] {
  const map = new Map<number, any>()
  const roots: any[] = []
  menus.forEach(m => {
    map.set(m.id, { id: m.id, label: m.menuName, children: [] })
  })
  menus.forEach(m => {
    const node = map.get(m.id)
    if (m.parentId === 0) {
      roots.push(node)
    } else {
      const parent = map.get(m.parentId)
      if (parent) {
        parent.children.push(node)
      } else {
        roots.push(node)
      }
    }
  })
  // 移除没有子节点的 children 数组
  function clean(node: any) {
    if (node.children.length === 0) {
      delete node.children
    } else {
      node.children.forEach(clean)
    }
    return node
  }
  return roots.map(clean)
}

async function openPermission(row: RoleRecord) {
  currentPermRoleId = row.id
  permVisible.value = true
  try {
    // 加载所有菜单
    const menuRes = await getAllMenus()
    const flatMenus = menuRes.data || []
    permTreeData.value = buildMenuTree(flatMenus)
    // 加载角色已有权限
    const permRes = await getRoleMenus(row.id)
    checkedPermKeys.value = permRes.data || []
    await nextTick()
    treeRef.value?.setCheckedKeys(checkedPermKeys.value)
  } catch {
    permTreeData.value = []
    checkedPermKeys.value = []
  }
}

async function handleSavePerm() {
  if (!currentPermRoleId || !treeRef.value) return
  permSaving.value = true
  try {
    const checkedKeys = treeRef.value.getCheckedKeys(false) as number[]
    await assignRoleMenus(currentPermRoleId, checkedKeys)
    ElMessage.success('权限分配成功')
    permVisible.value = false
  } catch {
    // handled by interceptor
  } finally {
    permSaving.value = false
  }
}

import { nextTick } from 'vue'
loadData()
</script>

<style scoped>
.role-view {
  padding: 0;
}
</style>
