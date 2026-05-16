import request from '../request'
import type { ApiResult, PageResult } from '../../types'

export interface RuleConfig {
  id?: number
  ruleCode: string
  ruleName: string
  ruleType: string
  threshold?: string
  enabled: boolean
  description?: string
}

export function getRuleConfigPage(params: any) {
  return request.get<any, ApiResult<PageResult<RuleConfig>>>('/rule-config/page', { params })
}

export function getRuleConfig(id: string) {
  return request.get<any, ApiResult<RuleConfig>>(`/rule-config/${id}`)
}

export function createRuleConfig(data: any) {
  return request.post<any, ApiResult<RuleConfig>>('/rule-config', data)
}

export function updateRuleConfig(id: string, data: any) {
  return request.put<any, ApiResult<RuleConfig>>(`/rule-config/${id}`, data)
}

export function deleteRuleConfig(id: string) {
  return request.delete<any, ApiResult<void>>(`/rule-config/${id}`)
}

export function toggleRuleConfig(id: string) {
  return request.put<any, ApiResult<void>>(`/rule-config/${id}/toggle`)
}
