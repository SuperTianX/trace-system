import request from '../request'
import type { ApiResult, PageResult, Complaint } from '../../types'

export function getComplaintPage(params: any) {
  return request.get<any, ApiResult<PageResult<Complaint>>>('/complaint/page', { params })
}

export function getComplaint(id: string) {
  return request.get<any, ApiResult<Complaint>>(`/complaint/${id}`)
}

export function createComplaint(data: any) {
  return request.post<any, ApiResult<Complaint>>('/complaint', data)
}

export function traceComplaint(id: string) {
  return request.post<any, ApiResult<void>>(`/complaint/${id}/trace`)
}

export function assignComplaintResponsible(id: string, data: { responsibleDept: string }) {
  return request.put<any, ApiResult<void>>(`/complaint/${id}/responsible`, data)
}

export function updateComplaintMeasure(id: string, data: { correctiveMeasures: string }) {
  return request.put<any, ApiResult<void>>(`/complaint/${id}/measure`, data)
}

export function updateComplaintRectification(id: string, data: { rectificationResult: string }) {
  return request.put<any, ApiResult<void>>(`/complaint/${id}/rectification`, data)
}

export function reviewComplaint(id: string, data: { reviewOpinion: string }) {
  return request.put<any, ApiResult<void>>(`/complaint/${id}/review`, data)
}

export function closeComplaint(id: string) {
  return request.put<any, ApiResult<void>>(`/complaint/${id}/close`)
}

export function archiveComplaint(id: string) {
  return request.put<any, ApiResult<void>>(`/complaint/${id}/archive`)
}
