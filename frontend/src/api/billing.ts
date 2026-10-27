import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Invoices ───────────────────────────────────────────────
export interface BillingInvoice {
  id: number
  controlNumber: string
  documentType: string
  clienteNombre: string
  clienteId: number
  total: number
  status: string
  emissionDate: string
  notes?: string
}

export interface InvoiceItem {
  productId: number
  quantity: number
  unitPrice: number
  description?: string
}

export interface CreateInvoiceDto {
  documentType: string
  clienteId: number
  items: InvoiceItem[]
  notes?: string
}

export const getInvoices = async (page = 0): Promise<PageResponse<BillingInvoice>> => {
  const r = await api.get('/api/admin/factura-sv/facturas', { params: { page, size: 20 } })
  return r.data.data
}

export const createInvoice = async (data: CreateInvoiceDto): Promise<BillingInvoice> => {
  const r = await api.post('/api/admin/factura-sv/procesar', data)
  return r.data.data
}

export const anularInvoice = async (id: number): Promise<void> => {
  await api.post(`/api/admin/factura-sv/facturas/${id}/anular`)
}

// ── Billing Clients ────────────────────────────────────────
export interface BillingClient {
  id: number
  name: string
  documentType: string
  documentNumber: string
  nrc: string | null
  address: string | null
  phone: string | null
  email: string | null
  isActive: boolean
}

export const getBillingClients = async (page = 0): Promise<PageResponse<BillingClient>> => {
  const r = await api.get('/api/admin/factura-sv/clientes', { params: { page, size: 20 } })
  return r.data.data
}

export const createBillingClient = async (data: Partial<BillingClient>): Promise<BillingClient> => {
  const r = await api.post('/api/admin/factura-sv/clientes', data)
  return r.data.data
}

export const updateBillingClient = async (id: number, data: Partial<BillingClient>): Promise<BillingClient> => {
  const r = await api.put(`/api/admin/factura-sv/clientes/${id}`, data)
  return r.data.data
}

// ── Billing Products ───────────────────────────────────────
export interface BillingProduct {
  id: number
  name: string
  code: string
  description: string | null
  price: number
  productType: string
  isActive: boolean
}

export const getBillingProducts = async (page = 0): Promise<PageResponse<BillingProduct>> => {
  const r = await api.get('/api/admin/factura-sv/productos', { params: { page, size: 20 } })
  return r.data.data
}

export const createBillingProduct = async (data: Partial<BillingProduct>): Promise<BillingProduct> => {
  const r = await api.post('/api/admin/factura-sv/productos', data)
  return r.data.data
}

export const updateBillingProduct = async (id: number, data: Partial<BillingProduct>): Promise<BillingProduct> => {
  const r = await api.put(`/api/admin/factura-sv/productos/${id}`, data)
  return r.data.data
}
