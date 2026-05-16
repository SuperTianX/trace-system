<template>
  <div class="view-container">
    <el-card shadow="hover" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="规则编码"><el-input v-model="query.ruleCode" placeholder="规则编码" clearable /></el-form-item>
        <el-form-item label="规则名称"><el-input v-model="query.ruleName" placeholder="规则名称" clearable /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" style="margin-top:16px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="ruleCode" label="规则编码" min-width="160" />
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column prop="ruleType" label="规则类型" width="100" />
        <el-table-column prop="threshold" label="阈值" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="启用" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              :loading="row._toggling"
              @change="(val: boolean) => handleToggle(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination
          v-model:page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则编码" prop="ruleCode"><el-input v-model="form.ruleCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName"><el-input v-model="form.ruleName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="规则类型"><el-input v-model="form.ruleType" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="阈值"><el-input v-model="form.threshold" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRuleConfigPage, getRuleConfig, createRuleConfig, updateRuleConfig, deleteRuleConfig, toggleRuleConfig } from '../../api/modules/ruleConfig'
import type { RuleConfig } from '../../api/modules/ruleConfig'

const tableData = ref<RuleConfig[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | string>('')
const saving = ref(false)

const query = reactive({ ruleCode: '', ruleName: '', page: 1, size: 20 })
const form = reactive<any>({ ruleCode: '', ruleName: '', ruleType: '', threshold: '', description: '', enabled: true })
const rules = {
  ruleCode: [{ required: true, message: '规则编码不能为空' }],
  ruleName: [{ required: true, message: '规则名称不能为空' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRuleConfigPage({ ...query, page: query.page, size: query.size })
    tableData.value = (res.data.records || []).map((r: any) => ({ ...r, _toggling: false }))
    total.value = res.data.total
  } catch {} finally { loading.value = false }
}
function resetQuery() { Object.assign(query, { ruleCode: '', ruleName: '' }); loadData() }
function showAdd() {
  isEdit.value = false; editingId.value = ''
  Object.assign(form, { ruleCode: '', ruleName: '', ruleType: '', threshold: '', description: '', enabled: true })
  dialogVisible.value = true
}
function showEdit(row: RuleConfig) {
  isEdit.value = true; editingId.value = row.id!
  Object.assign(form, { ruleCode: row.ruleCode, ruleName: row.ruleName, ruleType: row.ruleType || '', threshold: row.threshold || '', description: row.description || '', enabled: row.enabled })
  dialogVisible.value = true
}
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) {
      await updateRuleConfig(String(editingId.value), form)
      ElMessage.success('修改成功')
    } else {
      await createRuleConfig(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally { saving.value = false }
}
async function handleDelete(row: RuleConfig) {
  try {
    await ElMessageBox.confirm('确认删除该规则配置？')
    await deleteRuleConfig(String(row.id))
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}
async function handleToggle(row: any, val: boolean) {
  row._toggling = true
  try {
    await toggleRuleConfig(String(row.id))
    row.enabled = val
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {} finally { row._toggling = false }
}

onMounted(() => { loadData() })
</script>
<style scoped>
.view-container { padding: 0; }
.search-card { margin-bottom: 0; }
</style>
