import api from '../lib/axios'
import { PageResponse } from '../types/api'

// ── Employees ─────────────────────────────────────────────
export interface Employee {
  id: number
  code: string
  name: string
  email: string | null
  dui: string
  isssNumber: string | null
  nup: string | null
  afp: string | null
  position: string
  department: string | null
  hireDate: string
  salary: number
  bankName: string | null
  bankAccount: string | null
  isActive: boolean
}

export const getEmployees = async (page = 0): Promise<PageResponse<Employee>> => {
  const r = await api.get('/api/admin/nomina/empleados', { params: { page, size: 20 } })
  return r.data.data
}

export const createEmployee = async (data: Partial<Employee>): Promise<Employee> => {
  const r = await api.post('/api/admin/nomina/empleados', data)
  return r.data.data
}

export const updateEmployee = async (id: number, data: Partial<Employee>): Promise<Employee> => {
  const r = await api.put(`/api/admin/nomina/empleados/${id}`, data)
  return r.data.data
}

// ── Payroll Periods ────────────────────────────────────────
export interface PayrollPeriod {
  id: number
  name: string
  periodStart: string
  periodEnd: string
  status: string
  employeeCount: number
  totalAmount: number
  notes?: string
}

export interface CreatePayrollPeriodDto {
  name: string
  periodStart: string
  periodEnd: string
  notes?: string
}

export const getPayrollPeriods = async (page = 0): Promise<PageResponse<PayrollPeriod>> => {
  const r = await api.get('/api/admin/nomina/periodos', { params: { page, size: 20 } })
  return r.data.data
}

export const createPayrollPeriod = async (data: CreatePayrollPeriodDto): Promise<PayrollPeriod> => {
  const r = await api.post('/api/admin/nomina/periodos', data)
  return r.data.data
}

export const applyPayrollPeriod = async (id: number): Promise<void> => {
  await api.post(`/api/admin/nomina/periodos/${id}/aplicar`)
}
