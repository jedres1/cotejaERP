import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Purchase Invoices ──────────────────────────────────────
export interface PurchaseInvoice {
  id: number
  invoiceNumber: string
  supplierName: string
  supplierId: number
  documentType: string
  purchaseDate: string
  subtotal: number
  iva: number
  total: number
  status: string
  paymentMethod: string | null
  notes?: string
}

export interface CreatePurchaseInvoiceDto {
  supplierId: number
  documentType: string
  invoiceNumber: string
  purchaseDate: string
  subtotal: number
  iva: number
  total: number
  paymentMethod?: string
  notes?: string
}

export const getPurchaseInvoices = async (page = 0): Promise<PageResponse<PurchaseInvoice>> => {
  const r = await api.get('/admin/purchase-invoices', { params: { page, size: 20 } })
  return r.data.data
}

export const createPurchaseInvoice = async (data: CreatePurchaseInvoiceDto): Promise<PurchaseInvoice> => {
  const r = await api.post('/admin/purchase-invoices', data)
  return r.data.data
}

export const approvePurchaseInvoice = async (id: number): Promise<void> => {
  await api.post(`/admin/purchase-invoices/${id}/aprobar`)
}

export const payPurchaseInvoice = async (id: number): Promise<void> => {
  await api.post(`/admin/purchase-invoices/${id}/pagar`)
}

// ── Suppliers ──────────────────────────────────────────────
export interface Supplier {
  id: number
  name: string
  nitDui: string
  nrc: string | null
  address: string | null
  email: string | null
  phone: string | null
  contactName: string | null
  paymentTerms: string | null
}

export const getSuppliers = async (page = 0): Promise<PageResponse<Supplier>> => {
  const r = await api.get('/admin/suppliers', { params: { page, size: 20 } })
  return r.data.data
}

export const createSupplier = async (data: Partial<Supplier>): Promise<Supplier> => {
  const r = await api.post('/admin/suppliers', data)
  return r.data.data
}

export const updateSupplier = async (id: number, data: Partial<Supplier>): Promise<Supplier> => {
  const r = await api.put(`/admin/suppliers/${id}`, data)
  return r.data.data
}
