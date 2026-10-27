import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Building2, ChevronDown, LogOut, User } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

export default function Topbar() {
  const { user, companies, logout, selectCompany } = useAuth()
  const [companyOpen, setCompanyOpen] = useState(false)
  const [userOpen, setUserOpen] = useState(false)
  const navigate = useNavigate()
  const companyRef = useRef<HTMLDivElement>(null)
  const userRef = useRef<HTMLDivElement>(null)

  // Close dropdowns on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (companyRef.current && !companyRef.current.contains(e.target as Node)) setCompanyOpen(false)
      if (userRef.current && !userRef.current.contains(e.target as Node)) setUserOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const currentCompany = companies.find(c => c.id === user?.companyId)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const handleSelectCompany = async (id: number) => {
    await selectCompany(id)
    setCompanyOpen(false)
  }

  return (
    <header className="sticky top-0 z-20 bg-white border-b border-gray-200 h-14 flex items-center px-6 gap-4">
      <div className="flex-1" />

      {/* Company switcher */}
      {companies.length > 0 && (
        <div ref={companyRef} className="relative">
          <button
            onClick={() => setCompanyOpen(o => !o)}
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-gray-200 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
          >
            <Building2 className="h-4 w-4 text-gray-400" />
            <span className="max-w-[180px] truncate">
              {currentCompany?.businessName ?? 'Seleccionar empresa'}
            </span>
            <ChevronDown className="h-3.5 w-3.5 text-gray-400" />
          </button>

          {companyOpen && (
            <div className="absolute right-0 mt-1 w-64 bg-white rounded-xl shadow-lg border border-gray-200 py-1 z-50">
              <p className="px-3 py-1.5 text-xs font-semibold text-gray-400 uppercase">Empresas</p>
              {companies.map(c => (
                <button
                  key={c.id}
                  onClick={() => handleSelectCompany(c.id)}
                  className={`w-full text-left px-3 py-2 text-sm hover:bg-gray-50 transition-colors flex items-center gap-2 ${
                    c.id === user?.companyId ? 'text-brand-600 font-medium' : 'text-gray-700'
                  }`}
                >
                  <Building2 className="h-3.5 w-3.5 flex-shrink-0" />
                  <span className="truncate">{c.businessName}</span>
                  {c.id === user?.companyId && (
                    <span className="ml-auto text-xs text-brand-500">activa</span>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* User menu */}
      <div ref={userRef} className="relative">
        <button
          onClick={() => setUserOpen(o => !o)}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <div className="h-7 w-7 rounded-full bg-brand-600 flex items-center justify-center">
            <span className="text-white text-xs font-semibold">
              {user?.name?.charAt(0).toUpperCase() ?? 'U'}
            </span>
          </div>
          <span className="text-sm font-medium text-gray-700 max-w-[140px] truncate hidden sm:block">
            {user?.name}
          </span>
          <ChevronDown className="h-3.5 w-3.5 text-gray-400" />
        </button>

        {userOpen && (
          <div className="absolute right-0 mt-1 w-48 bg-white rounded-xl shadow-lg border border-gray-200 py-1 z-50">
            <div className="px-3 py-2 border-b border-gray-100">
              <p className="text-sm font-medium text-gray-800 truncate">{user?.name}</p>
              <p className="text-xs text-gray-400 truncate">{user?.email}</p>
            </div>
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
            >
              <LogOut className="h-4 w-4" />
              Cerrar sesión
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
