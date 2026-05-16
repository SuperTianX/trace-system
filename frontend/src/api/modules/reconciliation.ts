import request from '../request'
import type { ApiResult, PageResult, ReconDiff } from '../../types'

export function executeReconciliation() {
  return request.post<any, ApiResult<void>>('/reconciliation/execute')
}

export function getReconciliationDiffPage(params: any) {
  return request.get<any, ApiResult<PageResult<ReconDiff>>>('/reconciliation/diff/page', { params })
}

export function assignReconciliationDiff(id: string, data: { responsibleDept: string }) {
  return request.put<any, ApiResult<void>>(`/reconciliation/diff/${id}/assign`, data)
}

export function closeReconciliationDiff(id: string) {
  return request.put<any, ApiResult<void>>(`/reconciliation/diff/${id}/close`)
}
