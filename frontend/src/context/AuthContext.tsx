import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import api from '../lib/axios'
import { AuthUser, Company } from '../types/auth'

interface AuthContextValue {
  user: AuthUser | null
  isLoading: boolean
  companies: Company[]
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  selectCompany: (id: number) => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [companies, setCompanies] = useState<Company[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    api.get('/api/auth/me')
      .then(r => {
        setUser(r.data.data)
        if (r.data.data) loadCompanies()
      })
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  const loadCompanies = async () => {
    try {
      const r = await api.get('/admin/empresa/seleccionar')
      setCompanies(r.data.data ?? [])
    } catch {
      // ignore
    }
  }

  const login = async (email: string, password: string) => {
    const params = new URLSearchParams({ username: email, password })
    await api.post('/login', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    const r = await api.get('/api/auth/me')
    setUser(r.data.data)
    await loadCompanies()
  }

  const logout = async () => {
    await api.post('/api/auth/logout')
    setUser(null)
    setCompanies([])
  }

  const selectCompany = async (id: number) => {
    await api.post('/admin/empresa/cambiar', { companyId: id })
    setUser(prev => prev ? { ...prev, companyId: id } : prev)
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, companies, login, logout, selectCompany }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be inside AuthProvider')
  return ctx
}
