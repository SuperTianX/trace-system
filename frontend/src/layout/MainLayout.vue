<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo" @click="router.push('/dashboard')">
        <span v-if="!isCollapse">炉批卷追溯系统</span>
        <span v-else>追溯</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :router="true"
        background-color="#001529"
        text-color="#ffffffbf"
        active-text-color="#fff"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-sub-menu v-if="item.children" :index="item.path">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon style="cursor:pointer" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" style="margin-left:16px">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta?.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              {{ auth.userInfo?.realName || auth.userInfo?.username || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  Fold, Expand, ArrowDown, Odometer, OfficeBuilding, Grid, List, Document,
  Select, EditPen, Box, Connection, DataBoard, Warning, ChatDotSquare,
  DataAnalysis, Setting, Tools,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const isCollapse = ref(false)

interface MenuChild {
  path: string
  title: string
}

interface MenuItem {
  path: string
  title: string
  icon?: any
  children?: MenuChild[]
}

const menuItems: MenuItem[] = [
  { path: '/dashboard', title: '追溯工作台', icon: Odometer },
  { path: '/heat', title: '炉次管理', icon: OfficeBuilding },
  { path: '/slab', title: '铸坯管理', icon: Grid },
  { path: '/roll-batch', title: '轧制批次管理', icon: List },
  { path: '/coil', title: '卷号档案管理', icon: Document },
  { path: '/qc-record', title: '质检记录管理', icon: Select },
  { path: '/work-report', title: '报工管理', icon: EditPen },
  { path: '/inventory', title: '出入库管理', icon: Box },
  { path: '/trace', title: '追溯查询', icon: Connection },
  { path: '/reconciliation', title: '报工入库对账', icon: DataBoard },
  { path: '/chain-diagnosis', title: '断链诊断', icon: Warning },
  { path: '/complaint', title: '质量异议', icon: ChatDotSquare },
  { path: '/report', title: '报表中心', icon: DataAnalysis },
  { path: '/rule-config', title: '规则配置', icon: Setting },
  {
    path: '/system', title: '系统管理', icon: Tools,
    children: [
      { path: '/system/user', title: '用户管理' },
      { path: '/system/role', title: '角色管理' },
    ],
  },
]

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside { background: #001529; overflow-y: auto; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,.1); }
.layout-header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e4e7ed; padding: 0 16px; height: 50px; }
.header-left, .header-right { display: flex; align-items: center; }
.user-info { cursor: pointer; display: flex; align-items: center; gap: 4px; color: #333; }
.layout-main { background: #f0f2f5; padding: 16px; overflow-y: auto; }
</style>
