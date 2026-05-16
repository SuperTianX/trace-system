import request from '../request'
import type { ApiResult, PageResult, UserInfo } from '../../types'

export function getUserPage(params: any) {
  return request.get<any, ApiResult<PageResult<UserInfo>>>('/user/page', { params })
}

export function getUser(id: string) {
  return request.get<any, ApiResult<UserInfo>>(`/user/${id}`)
}

export function createUser(data: any) {
  return request.post<any, ApiResult<UserInfo>>('/user', data)
}

export function updateUser(id: string, data: any) {
  return request.put<any, ApiResult<UserInfo>>(`/user/${id}`, data)
}

export function deleteUser(id: string) {
  return request.delete<any, ApiResult<void>>(`/user/${id}`)
}
