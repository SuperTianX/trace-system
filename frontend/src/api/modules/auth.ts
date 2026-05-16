import request from '../request'
import type { ApiResult, LoginResult } from '../../types'

export function login(data: { username: string; password: string }) {
  return request.post<any, ApiResult<LoginResult>>('/auth/login', data)
}

export function logout() {
  return request.post<any, ApiResult<void>>('/auth/logout')
}
