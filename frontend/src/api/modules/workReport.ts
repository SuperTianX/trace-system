import request from '../request'
import type { ApiResult, PageResult, WorkReport } from '../../types'

export function getWorkReportPage(params: any) {
  return request.get<any, ApiResult<PageResult<WorkReport>>>('/work-report/page', { params })
}

export function getWorkReport(id: string) {
  return request.get<any, ApiResult<WorkReport>>(`/work-report/${id}`)
}

export function createWorkReport(data: any) {
  return request.post<any, ApiResult<WorkReport>>('/work-report', data)
}

export function updateWorkReport(id: string, data: any) {
  return request.put<any, ApiResult<WorkReport>>(`/work-report/${id}`, data)
}

export function deleteWorkReport(id: string) {
  return request.delete<any, ApiResult<void>>(`/work-report/${id}`)
}

export function approveWorkReport(id: string) {
  return request.put<any, ApiResult<void>>(`/work-report/${id}/approve`)
}

export function rejectWorkReport(id: string) {
  return request.put<any, ApiResult<void>>(`/work-report/${id}/reject`)
}

export function cancelWorkReport(id: string) {
  return request.put<any, ApiResult<void>>(`/work-report/${id}/cancel`)
}
