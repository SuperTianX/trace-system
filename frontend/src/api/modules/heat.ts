import request from '../request'
import type { ApiResult, PageResult, Heat } from '../../types'

export function getHeatPage(params: any) {
  return request.get<any, ApiResult<PageResult<Heat>>>('/heat/page', { params })
}

export function getHeat(id: string) {
  return request.get<any, ApiResult<Heat>>(`/heat/${id}`)
}

export function createHeat(data: any) {
  return request.post<any, ApiResult<Heat>>('/heat', data)
}

export function updateHeat(id: string, data: any) {
  return request.put<any, ApiResult<Heat>>(`/heat/${id}`, data)
}

export function deleteHeat(id: string) {
  return request.delete<any, ApiResult<void>>(`/heat/${id}`)
}

export function batchImportHeat(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResult<void>>('/heat/batch-import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
