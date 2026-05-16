import request from '../request'
import type { ApiResult, PageResult, ChainBreak } from '../../types'

export function executeChainDiagnosis() {
  return request.post<any, ApiResult<void>>('/chain-diagnosis/execute')
}

export function getChainBreakPage(params: any) {
  return request.get<any, ApiResult<PageResult<ChainBreak>>>('/chain-diagnosis/result/page', { params })
}

export function assignChainBreak(id: string, data: { responsibleDept: string }) {
  return request.put<any, ApiResult<void>>(`/chain-diagnosis/result/${id}/assign`, data)
}

export function closeChainBreak(id: string) {
  return request.put<any, ApiResult<void>>(`/chain-diagnosis/result/${id}/close`)
}
