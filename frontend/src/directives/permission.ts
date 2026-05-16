import { useAuthStore } from '../stores/auth'

/**
 * v-permission 按钮级权限指令
 *
 * 用法: v-permission="'system:user'" 或 v-permission="['system:user', 'system:role']"
 *
 * 权限码存储在 auth store 的 permissions 数组中。
 * 管理员角色 (ROLE_ADMIN) 默认拥有所有权限。
 */
const permission = {
  mounted(el: HTMLElement, binding: any) {
    const authStore = useAuthStore()
    const requiredPerms = Array.isArray(binding.value) ? binding.value : [binding.value]
    const userPerms = authStore.permissions || []
    const roles = authStore.roles || []

    // 管理员拥有所有权限
    if (roles.includes('ROLE_ADMIN')) return

    // 检查用户是否拥有所需权限
    const hasPermission = requiredPerms.some((p: string) => userPerms.includes(p))

    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  },
}

export default permission
