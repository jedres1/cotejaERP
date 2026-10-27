import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Product Types ──────────────────────────────────────────
export interface ProductType {
  id: number
  name: string
  code: string
}

export const getProductTypes = async (): Promise<ProductType[]> => {
  const r = await api.get('/admin/inventario/tipos-producto')
  return r.data.data ?? []
}

// ── Warehouses ─────────────────────────────────────────────
export interface Warehouse {
  id: number
  name: string
  code: string
  location: string | null
}

export const getWarehouses = async (): Promise<Warehouse[]> => {
  const r = await api.get('/admin/inventario/bodegas')
  return r.data.data ?? []
}

// ── Movements ─────────────────────────────────────────────
export interface InventoryMovement {
  id: number
  movementType: string
  productTypeName: string
  productTypeId: number
  warehouseName: string
  warehouseId: number
  quantity: number
  unitCost: number
  totalCost: number
  movementDate: string
  notes?: string
}

export interface CreateMovementDto {
  movementType: string
  productTypeId: number
  warehouseId: number
  quantity: number
  unitCost: number
  notes?: string
}

export const getMovements = async (page = 0): Promise<PageResponse<InventoryMovement>> => {
  const r = await api.get('/admin/inventario/movimientos', { params: { page, size: 20 } })
  return r.data.data
}

export const createMovement = async (data: CreateMovementDto): Promise<InventoryMovement> => {
  const r = await api.post('/admin/inventario/movimientos', data)
  return r.data.data
}
