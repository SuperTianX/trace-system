<template>
  <div class="report-view">
    <el-card shadow="hover">
      <el-form inline>
        <el-form-item label="报表类型">
          <el-select v-model="reportType" style="width:180px">
            <el-option label="追溯统计" value="trace" />
            <el-option label="断链分析" value="chainBreak" />
            <el-option label="对账差异" value="reconDiff" />
            <el-option label="质量异议统计" value="complaint" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width:240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">查询</el-button>
          <el-button @click="handleExport">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="hover" style="margin-top:16px">
      <template #header>
        <span style="font-weight:600">{{ reportLabel }}</span>
      </template>
      <div ref="chartRef" style="height:400px"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getTraceStat, getChainBreakStat, getReconDiffStat, getComplaintStat } from '../../api/modules/report'

const reportType = ref('trace')
const dateRange = ref<string[]>([])
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const reportLabel = computed(() => {
  const map: Record<string, string> = {
    trace: '追溯统计',
    chainBreak: '断链分析',
    reconDiff: '对账差异',
    complaint: '质量异议统计',
  }
  return map[reportType.value] || '报表'
})

async function loadReport() {
  await nextTick()
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  try {
    const params: any = {}
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    let api: any
    switch (reportType.value) {
      case 'trace': api = getTraceStat; break
      case 'chainBreak': api = getChainBreakStat; break
      case 'reconDiff': api = getReconDiffStat; break
      case 'complaint': api = getComplaintStat; break
    }
    const res = await api(params)
    const data = res.data || []

    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: data.map((d: any) => d.name || d.label || ''),
        axisLabel: { rotate: data.length > 8 ? 45 : 0 },
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          type: 'bar',
          data: data.map((d: any) => d.value ?? d.count ?? 0),
          itemStyle: {
            color: '#1a73e8',
            borderRadius: [4, 4, 0, 0],
          },
          barMaxWidth: 50,
        },
      ],
      backgroundColor: '#fff',
    })
  } catch {
    if (chart) {
      chart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: { color: '#999', fontSize: 16 },
        },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [],
      })
    }
  }
}

function handleExport() {
  ElMessage.info('导出功能开发中')
}

function handleResize() {
  chart?.resize()
}

import { onUnmounted } from 'vue'
onMounted(() => {
  loadReport()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.report-view {
  padding: 0;
}
</style>
