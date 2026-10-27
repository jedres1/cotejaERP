import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Bank Accounts ─────────────────────────────────────────
export interface BankAccount {
  id: number
  name: string
  bankName: string
  accountNumber: string
  accountType: string
  currency: string
  currentBalance: number
}

export const getBankAccounts = async (page = 0): Promise<PageResponse<BankAccount>> => {
  const r = await api.get('/admin/control-bancario/cuentas', { params: { page, size: 20 } })
  return r.data.data
}

export const createBankAccount = async (data: Partial<BankAccount>): Promise<BankAccount> => {
  const r = await api.post('/admin/control-bancario/cuentas', data)
  return r.data.data
}

export const updateBankAccount = async (id: number, data: Partial<BankAccount>): Promise<BankAccount> => {
  const r = await api.put(`/admin/control-bancario/cuentas/${id}`, data)
  return r.data.data
}

// ── Transactions ───────────────────────────────────────────
export interface BankTransaction {
  id: number
  bankAccountId: number
  bankAccountName: string
  transactionDate: string
  description: string
  transactionType: string
  amount: number
  reference: string | null
  balance: number
}

export interface CreateTransactionDto {
  bankAccountId: number
  transactionDate: string
  description: string
  transactionType: string
  amount: number
  reference?: string
}

export const getBankTransactions = async (page = 0): Promise<PageResponse<BankTransaction>> => {
  const r = await api.get('/admin/control-bancario/transacciones', { params: { page, size: 20 } })
  return r.data.data
}

export const createBankTransaction = async (data: CreateTransactionDto): Promise<BankTransaction> => {
  const r = await api.post('/admin/control-bancario/transacciones', data)
  return r.data.data
}
