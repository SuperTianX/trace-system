import request from '../request'
import type { ApiResult, DashboardVO } from '../../types'

export function getDashboardOverview() {
  return request.get<any, ApiResult<DashboardVO>>('/dashboard/overview')
}
