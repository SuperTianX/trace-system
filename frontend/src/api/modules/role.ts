import request from '../request'
import type { ApiResult, PageResult } from '../../types'

export interface Role {
  id?: number
  roleCode: string
  roleName: string
  description?: string
  status: number
}

export function getRolePage(params: any) {
  return request.get<any, ApiResult<PageResult<Role>>>('/role/page', { params })
}

export function getRoleList() {
  return request.get<any, ApiResult<Role[]>>('/role')
}

export function createRole(data: any) {
  return request.post<any, ApiResult<Role>>('/role', data)
}

export function updateRole(id: string, data: any) {
  return request.put<any, ApiResult<Role>>(`/role/${id}`, data)
}

export function deleteRole(id: string) {
  return request.delete<any, ApiResult<void>>(`/role/${id}`)
}

export interface MenuItem {
  id: number
  menuName: string
  parentId: number
  path: string
  permissionCode: string
  sortOrder: number
}

/** 获取所有菜单（权限树） */
export function getAllMenus() {
  return request.get<any, ApiResult<MenuItem[]>>('/role/menus/tree')
}

/** 获取角色已分配的菜单ID */
export function getRoleMenus(roleId: number) {
  return request.get<any, ApiResult<number[]>>(`/role/${roleId}/menus`)
}

/** 分配角色菜单权限 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.put<any, ApiResult<void>>(`/role/${roleId}/menus`, menuIds)
}
