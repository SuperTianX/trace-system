import request from '../request'
import type { ApiResult, PageResult, Coil } from '../../types'

export function getCoilPage(params: any) {
  return request.get<any, ApiResult<PageResult<Coil>>>('/coil/page', { params })
}

export function getCoil(id: string) {
  return request.get<any, ApiResult<Coil>>(`/coil/${id}`)
}

export function createCoil(data: any) {
  return request.post<any, ApiResult<Coil>>('/coil', data)
}

export function updateCoil(id: string, data: any) {
  return request.put<any, ApiResult<Coil>>(`/coil/${id}`, data)
}

export function deleteCoil(id: string) {
  return request.delete<any, ApiResult<void>>(`/coil/${id}`)
}
