import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Account Catalog ────────────────────────────────────────
export interface Account {
  id: number
  code: string
  name: string
  type: string
  parentId: number | null
  isLeaf: boolean
}

export const getAccounts = async (page = 0): Promise<PageResponse<Account>> => {
  const r = await api.get('/api/admin/contabilidad/catalogo', { params: { page, size: 50 } })
  return r.data.data
}

export const createAccount = async (data: Partial<Account>): Promise<Account> => {
  const r = await api.post('/api/admin/contabilidad/catalogo', data)
  return r.data.data
}

export const updateAccount = async (id: number, data: Partial<Account>): Promise<Account> => {
  const r = await api.put(`/api/admin/contabilidad/catalogo/${id}`, data)
  return r.data.data
}

export const deleteAccount = async (id: number): Promise<void> => {
  await api.delete(`/api/admin/contabilidad/catalogo/${id}`)
}

// ── Journal ───────────────────────────────────────────────
export interface JournalLine {
  accountId: number
  accountCode?: string
  accountName?: string
  description?: string
  debit: number
  credit: number
}

export interface JournalEntry {
  id: number
  entryNumber: string
  entryDate: string
  description: string
  reference?: string
  status: string
  totalDebit: number
  totalCredit: number
  lines: JournalLine[]
}

export interface CreateJournalEntryDto {
  entryDate: string
  description: string
  reference?: string
  lines: JournalLine[]
}

export const getJournalEntries = async (page = 0): Promise<PageResponse<JournalEntry>> => {
  const r = await api.get('/api/admin/contabilidad/diario', { params: { page, size: 20 } })
  return r.data.data
}

export const createJournalEntry = async (data: CreateJournalEntryDto): Promise<JournalEntry> => {
  const r = await api.post('/api/admin/contabilidad/diario', data)
  return r.data.data
}

export const approveJournalEntry = async (id: number): Promise<void> => {
  await api.post(`/api/admin/contabilidad/diario/${id}/aprobar`)
}

export const anularJournalEntry = async (id: number): Promise<void> => {
  await api.post(`/api/admin/contabilidad/diario/${id}/anular`)
}
