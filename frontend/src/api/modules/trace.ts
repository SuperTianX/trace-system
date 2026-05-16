import request from '../request'
import type { ApiResult, TraceResult } from '../../types'

export function traceForward(data: { inputType: string; inputValue: string }) {
  return request.post<any, ApiResult<TraceResult>>('/trace/forward', data)
}

export function traceBackward(data: { inputType: string; inputValue: string }) {
  return request.post<any, ApiResult<TraceResult>>('/trace/backward', data)
}
