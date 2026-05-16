import request from '../request'
import type { ApiResult, PageResult, QcRecord } from '../../types'

export function getQcRecordPage(params: any) {
  return request.get<any, ApiResult<PageResult<QcRecord>>>('/qc-record/page', { params })
}

export function getQcRecord(id: string) {
  return request.get<any, ApiResult<QcRecord>>(`/qc-record/${id}`)
}

export function createQcRecord(data: any) {
  return request.post<any, ApiResult<QcRecord>>('/qc-record', data)
}

export function updateQcRecord(id: string, data: any) {
  return request.put<any, ApiResult<QcRecord>>(`/qc-record/${id}`, data)
}

export function deleteQcRecord(id: string) {
  return request.delete<any, ApiResult<void>>(`/qc-record/${id}`)
}
