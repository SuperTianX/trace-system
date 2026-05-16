<template>
  <div class="trace-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="方向">
          <el-radio-group v-model="direction">
            <el-radio value="forward">正向追溯（炉→客户）</el-radio>
            <el-radio value="backward">反向追溯（卷/异议→炉）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="入口类型">
          <el-select v-model="inputType" style="width:130px">
            <el-option v-if="direction==='forward'" label="炉号" value="heat" />
            <el-option v-if="direction==='forward'" label="铸坯号" value="slab" />
            <el-option v-if="direction==='forward'" label="批次号" value="batch" />
            <el-option v-if="direction==='backward'" label="卷号" value="coil" />
            <el-option v-if="direction==='backward'" label="投诉单号" value="complaint" />
            <el-option v-if="direction==='backward'" label="出库单号" value="outbound" />
          </el-select>
        </el-form-item>
        <el-form-item label="输入值">
          <el-input v-model="inputValue" placeholder="请输入" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleTrace">追溯</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result" shadow="hover" style="margin-top:16px">
      <template #header>
        <span style="font-weight:600">追溯结果</span>
        <el-tag v-if="result.isComplete" type="success" style="margin-left:12px">链路完整</el-tag>
        <el-tag v-else type="danger" style="margin-left:12px">{{ result.abnormalNodeCount }} 个异常节点</el-tag>
        <div style="float:right">
          <el-radio-group v-model="viewMode" size="small" @change="switchView">
            <el-radio-button value="tree">树图</el-radio-button>
            <el-radio-button value="flow">流程图</el-radio-button>
            <el-radio-button value="table">层级表</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-descriptions :column="3" border size="small" style="margin-bottom:16px">
        <el-descriptions-item label="输入值">{{ result.inputValue }}</el-descriptions-item>
        <el-descriptions-item label="节点数">{{ result.nodes?.length || 0 }}</el-descriptions-item>
        <el-descriptions-item label="链路状态">
          <el-tag :type="result.isComplete ? 'success' : 'danger'" size="small">
            {{ result.isComplete ? '完整' : '异常' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div v-show="viewMode !== 'table'" ref="chartRef" style="height:500px;width:100%"></div>
      <div v-show="viewMode === 'table'" style="max-height:500px;overflow-y:auto">
        <el-table :data="tableNodes" size="small" stripe border style="width:100%">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="type" label="节点类型" width="120" />
          <el-table-column prop="labelId" label="编号" min-width="160" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'abnormal' ? 'danger' : 'success'" size="small">
                {{ row.status === 'abnormal' ? '异常' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="extra" label="详情" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { traceForward, traceBackward } from '../../api/modules/trace'

const direction = ref('forward')
const inputType = ref('heat')
const inputValue = ref('')
const loading = ref(false)
const result = ref<any>(null)
const chartRef = ref<HTMLElement>()
const viewMode = ref('tree')

const tableNodes = ref<any[]>([])

let chart: echarts.ECharts | null = null

// Reset inputType when direction changes
watch(direction, (val) => {
  inputType.value = val === 'forward' ? 'heat' : 'coil'
})

function switchView(mode: string) {
  if (mode === 'table') {
    // Build table data
    const nodes = result.value?.nodes || []
    tableNodes.value = nodes.map((n: any) => {
      const dataObj = typeof n.data === 'object' ? n.data : {}
      const labelId = dataObj?.heatId || dataObj?.slabId || dataObj?.batchId || dataObj?.coilId || dataObj?.docNo || dataObj?.complaintId || n.id
      const extra = dataObj ? JSON.stringify(dataObj).substring(0, 100) : ''
      return { type: n.type, labelId, status: n.status || 'normal', extra }
    })
  } else {
    // Chart mode - need nextTick for the chartRef to become visible
    nextTick(() => renderChart())
  }
}

async function handleTrace() {
  if (!inputValue.value) { ElMessage.warning('请输入查询值'); return }
  loading.value = true
  try {
    const api = direction.value === 'forward' ? traceForward : traceBackward
    const res = await api({ inputType: inputType.value, inputValue: inputValue.value })
    result.value = res.data
    await nextTick()
    renderChart()
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || !result.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const nodes = result.value.nodes || []
  const edges = result.value.edges || []

  // Build node map
  const nodeMap = new Map<string, any>()
  nodes.forEach((n: any) => {
    const dataObj = typeof n.data === 'object' ? n.data : {}
    const labelId = dataObj?.heatId || dataObj?.slabId || dataObj?.batchId || dataObj?.coilId || dataObj?.docNo || dataObj?.complaintId || n.id
    const label = n.type + ': ' + labelId
    nodeMap.set(n.id, {
      id: n.id,
      name: label,
      label,
      status: n.status,
      itemStyle: n.status === 'abnormal' ? { color: '#f56c6c' } : undefined,
    })
  })

  if (viewMode.value === 'flow') {
    // Flow chart (graph LR layout)
    const graphNodes: any[] = []
    const graphLinks: any[] = []
    nodes.forEach((n: any) => {
      const dataObj = typeof n.data === 'object' ? n.data : {}
      const labelId = dataObj?.heatId || dataObj?.slabId || dataObj?.batchId || dataObj?.coilId || dataObj?.docNo || dataObj?.complaintId || n.id
      graphNodes.push({
        id: n.id,
        name: n.type + '\n' + labelId,
        symbolSize: 50,
        itemStyle: n.status === 'abnormal' ? { color: '#f56c6c' } : { color: '#1a73e8' },
      })
    })
    edges.forEach((e: any) => {
      graphLinks.push({ source: e.from, target: e.to, label: { show: true, formatter: e.relation || '', fontSize: 10 } })
    })
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'graph',
        layout: 'none',
        data: graphNodes,
        links: graphLinks,
        roam: true,
        draggable: true,
        left: 30,
        right: 30,
        top: 30,
        bottom: 30,
        label: { show: true, position: 'bottom', fontSize: 11 },
        edgeSymbol: ['none', 'arrow'],
        edgeSymbolSize: [0, 10],
        lineStyle: { color: '#999', width: 2, curveness: 0.3 },
      }],
    })
  } else {
    // Tree chart (default)
    const childrenMap = new Map<string, string[]>()
    const parentMap = new Map<string, string>()
    edges.forEach((e: any) => {
      if (!childrenMap.has(e.from)) childrenMap.set(e.from, [])
      childrenMap.get(e.from)!.push(e.to)
      parentMap.set(e.to, e.from)
    })

    // Find root (node without parent)
    let rootId: string | null = null
    for (const n of nodes) {
      if (!parentMap.has(n.id)) {
        rootId = n.id
        break
      }
    }
    if (!rootId && nodes.length > 0) rootId = nodes[0].id
    if (!rootId) return

    function buildTree(nodeId: string): any {
      const node = nodeMap.get(nodeId) || { id: nodeId, name: nodeId }
      const childIds = childrenMap.get(nodeId)
      const children = childIds?.map((childId: string) => buildTree(childId))
      return { ...node, children: children?.length ? children : undefined }
    }

    const treeData = buildTree(rootId)

    chart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: (p: any) => {
          const info = result.value.nodes?.find((n: any) => n.id === p.name)
          let extra = ''
          if (info && typeof info.data === 'object') {
            extra = '<br/>状态: ' + (info.status || 'normal')
          }
          return p.name + extra
        },
      },
      series: [
        {
          type: 'tree',
          data: [treeData],
          top: 20,
          bottom: 20,
          left: 30,
          right: 100,
          symbolSize: 10,
          label: {
            position: 'right',
            verticalAlign: 'middle',
            align: 'left',
            fontSize: 12,
          },
          leaves: {
            label: {
              position: 'right',
              verticalAlign: 'middle',
              align: 'left',
            },
          },
          expandAndCollapse: true,
          animationDuration: 550,
          lineStyle: {
            color: '#999',
            width: 1.5,
          },
        },
      ],
    })
  }
}

// Handle resize
function handleResize() {
  chart?.resize()
}

import { onMounted, onUnmounted } from 'vue'
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.trace-view {
  padding: 0;
}
</style>
