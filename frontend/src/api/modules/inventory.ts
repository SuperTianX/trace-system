import request from '../request'
import type { ApiResult, PageResult, Inventory, Stock } from '../../types'

export function getInventoryPage(params: any) {
  return request.get<any, ApiResult<PageResult<Inventory>>>('/inventory/page', { params })
}

export function getInventory(id: string) {
  return request.get<any, ApiResult<Inventory>>(`/inventory/${id}`)
}

export function createInventory(data: any) {
  return request.post<any, ApiResult<Inventory>>('/inventory', data)
}

export function approveInventory(id: string) {
  return request.put<any, ApiResult<void>>(`/inventory/${id}/approve`)
}

export function voidInventory(id: string) {
  return request.put<any, ApiResult<void>>(`/inventory/${id}/void`)
}

export function getStockPage(params: any) {
  return request.get<any, ApiResult<PageResult<Stock>>>('/inventory/stock/page', { params })
}
