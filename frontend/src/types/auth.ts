export interface AuthUser {
  id: number
  email: string
  name: string
  role: 'ADMIN' | 'INQUILINO'
  moduleAccesses: string[]
  companyId: number | null
}

export interface Company {
  id: number
  businessName: string
  tradeName: string | null
  nitDui: string
  nrc: string | null
  moduleAccesses: string[]
  hasActiveLicense: boolean
}
