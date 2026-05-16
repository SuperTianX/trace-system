import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '../api/modules/auth'
import request from '../api/request'
import type { ApiResult, UserInfo } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('permissions') || '[]'))

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value?.roles || [])

  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', res.data.token)
    if (res.data.userInfo) {
      localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
    }
    // 获取权限码
    try {
      const permRes = await request.get<any, ApiResult<string[]>>('/auth/permissions')
      if (permRes.data) {
        permissions.value = permRes.data
        localStorage.setItem('permissions', JSON.stringify(permRes.data))
      }
    } catch {
      // 权限获取失败不影响登录
    }
  }

  function setPermissions(perms: string[]) {
    permissions.value = perms
    localStorage.setItem('permissions', JSON.stringify(perms))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
  }

  function initFromStorage() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try { userInfo.value = JSON.parse(stored) } catch {}
    }
    const perms = localStorage.getItem('permissions')
    if (perms) {
      try { permissions.value = JSON.parse(perms) } catch {}
    }
  }

  return { token, userInfo, permissions, isLoggedIn, roles, login, setPermissions, logout, initFromStorage }
})
