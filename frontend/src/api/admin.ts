import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Customers ──────────────────────────────────────────────
export interface Customer {
  id: number
  name: string
  email: string
  phone: string
  createdAt?: string
}

export const getCustomers = async (page = 0): Promise<PageResponse<Customer>> => {
  const r = await api.get('/admin/customers', { params: { page, size: 20 } })
  return r.data.data
}

export const createCustomer = async (data: Partial<Customer>): Promise<Customer> => {
  const r = await api.post('/admin/customers', data)
  return r.data.data
}

export const updateCustomer = async (id: number, data: Partial<Customer>): Promise<Customer> => {
  const r = await api.put(`/admin/customers/${id}`, data)
  return r.data.data
}

export const deleteCustomer = async (id: number): Promise<void> => {
  await api.delete(`/admin/customers/${id}`)
}

// ── Companies ──────────────────────────────────────────────
export interface Company {
  id: number
  customerId: number
  businessName: string
  tradeName: string | null
  nitDui: string
  nrc: string | null
  address: string | null
  email: string | null
  phone: string | null
  moduleAccesses: string[]
  environment: string
  hasActiveLicense: boolean
}

export const getCompanies = async (page = 0): Promise<PageResponse<Company>> => {
  const r = await api.get('/admin/companies', { params: { page, size: 20 } })
  return r.data.data
}

export const createCompany = async (data: Partial<Company>): Promise<Company> => {
  const r = await api.post('/admin/companies', data)
  return r.data.data
}

export const updateCompany = async (id: number, data: Partial<Company>): Promise<Company> => {
  const r = await api.put(`/admin/companies/${id}`, data)
  return r.data.data
}

// ── Users ──────────────────────────────────────────────────
export interface User {
  id: number
  name: string
  email: string
  role: 'ADMIN' | 'INQUILINO'
  moduleAccesses: string[]
  isActive: boolean
}

export const getUsers = async (page = 0): Promise<PageResponse<User>> => {
  const r = await api.get('/admin/users', { params: { page, size: 20 } })
  return r.data.data
}

export const createUser = async (data: Partial<User> & { password?: string }): Promise<User> => {
  const r = await api.post('/admin/users', data)
  return r.data.data
}

export const updateUser = async (id: number, data: Partial<User>): Promise<User> => {
  const r = await api.put(`/admin/users/${id}`, data)
  return r.data.data
}

// ── Plans ──────────────────────────────────────────────────
export interface Plan {
  id: number
  name: string
  description: string | null
  price: number
  moduleAccesses: string[]
  isActive: boolean
}

export const getPlans = async (page = 0): Promise<PageResponse<Plan>> => {
  const r = await api.get('/admin/plans', { params: { page, size: 20 } })
  return r.data.data
}

export const createPlan = async (data: Partial<Plan>): Promise<Plan> => {
  const r = await api.post('/admin/plans', data)
  return r.data.data
}

export const updatePlan = async (id: number, data: Partial<Plan>): Promise<Plan> => {
  const r = await api.put(`/admin/plans/${id}`, data)
  return r.data.data
}

// ── Licenses ───────────────────────────────────────────────
export interface License {
  id: number
  customerName: string
  planName: string
  startDate: string
  endDate: string
  status: string
}

export const getLicenses = async (page = 0): Promise<PageResponse<License>> => {
  const r = await api.get('/admin/licenses', { params: { page, size: 20 } })
  return r.data.data
}
