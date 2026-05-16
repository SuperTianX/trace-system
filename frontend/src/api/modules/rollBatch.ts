import request from '../request'
import type { ApiResult, PageResult, RollBatch } from '../../types'

export function getRollBatchPage(params: any) {
  return request.get<any, ApiResult<PageResult<RollBatch>>>('/roll-batch/page', { params })
}

export function getRollBatch(id: string) {
  return request.get<any, ApiResult<RollBatch>>(`/roll-batch/${id}`)
}

export function createRollBatch(data: any) {
  return request.post<any, ApiResult<RollBatch>>('/roll-batch', data)
}

export function updateRollBatch(id: string, data: any) {
  return request.put<any, ApiResult<RollBatch>>(`/roll-batch/${id}`, data)
}

export function deleteRollBatch(id: string) {
  return request.delete<any, ApiResult<void>>(`/roll-batch/${id}`)
}

export function mergeRollBatch(id: string, ids: string[]) {
  return request.put<any, ApiResult<void>>(`/roll-batch/${id}/merge`, null, { params: { ids: ids.join(',') } })
}

export function splitRollBatch(id: string) {
  return request.post<any, ApiResult<void>>(`/roll-batch/${id}/split`)
}
