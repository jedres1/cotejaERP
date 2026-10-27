import { Receipt, Users, ShoppingCart, Banknote } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

interface StatCard {
  label: string
  value: string
  icon: React.ElementType
  color: string
  bgColor: string
}

const stats: StatCard[] = [
  {
    label: 'Facturas del mes',
    value: '—',
    icon: Receipt,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50',
  },
  {
    label: 'Empleados activos',
    value: '—',
    icon: Users,
    color: 'text-green-600',
    bgColor: 'bg-green-50',
  },
  {
    label: 'Compras del mes',
    value: '—',
    icon: ShoppingCart,
    color: 'text-orange-600',
    bgColor: 'bg-orange-50',
  },
  {
    label: 'Cuentas bancarias',
    value: '—',
    icon: Banknote,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50',
  },
]

export default function DashboardPage() {
  const { user, companies } = useAuth()
  const currentCompany = companies.find(c => c.id === user?.companyId)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-sm text-gray-500 mt-1">
          Bienvenido, <span className="font-medium text-gray-700">{user?.name}</span>
          {currentCompany && (
            <> — <span className="font-medium text-brand-600">{currentCompany.businessName}</span></>
          )}
        </p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {stats.map(stat => (
          <div
            key={stat.label}
            className="bg-white rounded-xl border border-gray-200 p-5 flex items-center gap-4 shadow-sm hover:shadow-md transition-shadow"
          >
            <div className={`h-12 w-12 rounded-xl flex items-center justify-center flex-shrink-0 ${stat.bgColor}`}>
              <stat.icon className={`h-6 w-6 ${stat.color}`} />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              <p className="text-sm text-gray-500">{stat.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* No company warning */}
      {!user?.companyId && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-5 flex items-start gap-3">
          <div className="flex-shrink-0 text-yellow-500 mt-0.5">
            <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
          </div>
          <div>
            <p className="text-sm font-semibold text-yellow-800">Sin empresa seleccionada</p>
            <p className="text-sm text-yellow-700 mt-0.5">
              Selecciona una empresa en la barra superior para acceder a todos los módulos.
            </p>
          </div>
        </div>
      )}

      {/* Info cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <h2 className="text-base font-semibold text-gray-800 mb-3">Módulos disponibles</h2>
          {user?.moduleAccesses && user.moduleAccesses.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {user.moduleAccesses.map(m => (
                <span key={m} className="px-3 py-1 bg-brand-50 text-brand-700 rounded-full text-xs font-medium border border-brand-100">
                  {m}
                </span>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-400">No hay módulos asignados</p>
          )}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <h2 className="text-base font-semibold text-gray-800 mb-3">Información de sesión</h2>
          <dl className="space-y-2 text-sm">
            <div className="flex gap-2">
              <dt className="text-gray-500 min-w-[80px]">Rol:</dt>
              <dd className="text-gray-800 font-medium">{user?.role}</dd>
            </div>
            <div className="flex gap-2">
              <dt className="text-gray-500 min-w-[80px]">Correo:</dt>
              <dd className="text-gray-800">{user?.email}</dd>
            </div>
            <div className="flex gap-2">
              <dt className="text-gray-500 min-w-[80px]">Empresa:</dt>
              <dd className="text-gray-800">{currentCompany?.businessName ?? '—'}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  )
}
