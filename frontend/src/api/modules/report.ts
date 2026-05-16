import request from '../request'
import type { ApiResult } from '../../types'

export function getTraceStat(params: any) {
  return request.get<any, ApiResult<any>>('/report/trace-stat', { params })
}

export function getChainBreakStat(params: any) {
  return request.get<any, ApiResult<any>>('/report/chain-break-stat', { params })
}

export function getReconDiffStat(params: any) {
  return request.get<any, ApiResult<any>>('/report/recon-diff-stat', { params })
}

export function getComplaintStat(params: any) {
  return request.get<any, ApiResult<any>>('/report/complaint-stat', { params })
}
