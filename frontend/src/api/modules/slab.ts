import request from '../request'
import type { ApiResult, PageResult, Slab } from '../../types'

export function getSlabPage(params: any) {
  return request.get<any, ApiResult<PageResult<Slab>>>('/slab/page', { params })
}

export function getSlab(id: string) {
  return request.get<any, ApiResult<Slab>>(`/slab/${id}`)
}

export function createSlab(data: any) {
  return request.post<any, ApiResult<Slab>>('/slab', data)
}

export function updateSlab(id: string, data: any) {
  return request.put<any, ApiResult<Slab>>(`/slab/${id}`, data)
}

export function deleteSlab(id: string) {
  return request.delete<any, ApiResult<void>>(`/slab/${id}`)
}

export function updateSlabStatus(id: string, status: number) {
  return request.put<any, ApiResult<void>>(`/slab/${id}/status`, null, { params: { status } })
}
