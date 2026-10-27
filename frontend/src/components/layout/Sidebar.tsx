import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  Users,
  Building2,
  UserCog,
  CreditCard,
  FileText,
  Receipt,
  ShoppingCart,
  BookOpen,
  BookMarked,
  UserCheck,
  Calendar,
  Package,
  Warehouse,
  Banknote,
  ArrowLeftRight,
  ChevronDown,
  ChevronRight,
} from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'

interface NavItem {
  label: string
  to: string
  icon: React.ElementType
}

interface NavSection {
  label: string
  items: NavItem[]
  adminOnly?: boolean
  requiresCompany?: boolean
}

const sections: NavSection[] = [
  {
    label: 'Administración',
    adminOnly: true,
    items: [
      { label: 'Clientes', to: '/admin/clientes', icon: Users },
      { label: 'Empresas', to: '/admin/empresas', icon: Building2 },
      { label: 'Usuarios', to: '/admin/usuarios', icon: UserCog },
      { label: 'Planes', to: '/admin/planes', icon: CreditCard },
      { label: 'Licencias', to: '/admin/licencias', icon: FileText },
    ],
  },
  {
    label: 'Facturación',
    requiresCompany: true,
    items: [
      { label: 'Facturas', to: '/billing/facturas', icon: Receipt },
      { label: 'Clientes', to: '/billing/clientes', icon: Users },
      { label: 'Productos', to: '/billing/productos', icon: Package },
    ],
  },
  {
    label: 'Contabilidad',
    requiresCompany: true,
    items: [
      { label: 'Catálogo', to: '/contabilidad/catalogo', icon: BookOpen },
      { label: 'Diario', to: '/contabilidad/diario', icon: BookMarked },
    ],
  },
  {
    label: 'Nómina',
    requiresCompany: true,
    items: [
      { label: 'Empleados', to: '/nomina/empleados', icon: UserCheck },
      { label: 'Períodos', to: '/nomina/periodos', icon: Calendar },
    ],
  },
  {
    label: 'Compras',
    requiresCompany: true,
    items: [
      { label: 'Facturas', to: '/compras/facturas', icon: ShoppingCart },
      { label: 'Proveedores', to: '/compras/proveedores', icon: Building2 },
    ],
  },
  {
    label: 'Inventario',
    requiresCompany: true,
    items: [
      { label: 'Movimientos', to: '/inventario/movimientos', icon: Warehouse },
    ],
  },
  {
    label: 'Banca',
    requiresCompany: true,
    items: [
      { label: 'Cuentas', to: '/banca/cuentas', icon: Banknote },
      { label: 'Transacciones', to: '/banca/transacciones', icon: ArrowLeftRight },
    ],
  },
]

function SidebarSection({ section }: { section: NavSection }) {
  const [open, setOpen] = useState(true)

  return (
    <div className="mb-1">
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between px-3 py-1.5 text-xs font-semibold text-gray-400 uppercase tracking-wider hover:text-gray-300 transition-colors"
      >
        {section.label}
        {open ? <ChevronDown className="h-3 w-3" /> : <ChevronRight className="h-3 w-3" />}
      </button>
      {open && (
        <ul className="space-y-0.5">
          {section.items.map(item => (
            <li key={item.to}>
              <NavLink
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors mx-2 ${
                    isActive
                      ? 'bg-brand-600 text-white'
                      : 'text-gray-300 hover:bg-white/10 hover:text-white'
                  }`
                }
              >
                <item.icon className="h-4 w-4 flex-shrink-0" />
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default function Sidebar() {
  const { user } = useAuth()

  const visibleSections = sections.filter(s => {
    if (s.adminOnly && user?.role !== 'ADMIN') return false
    if (s.requiresCompany && !user?.companyId) return false
    return true
  })

  return (
    <aside className="fixed inset-y-0 left-0 w-64 bg-gray-900 flex flex-col z-30">
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-5 border-b border-white/10">
        <div className="h-8 w-8 rounded-lg bg-brand-600 flex items-center justify-center">
          <span className="text-white font-bold text-sm">C</span>
        </div>
        <span className="text-white font-semibold text-lg tracking-tight">Coteja ERP</span>
      </div>

      {/* Dashboard link */}
      <div className="px-2 pt-4 pb-2">
        <NavLink
          to="/dashboard"
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              isActive
                ? 'bg-brand-600 text-white'
                : 'text-gray-300 hover:bg-white/10 hover:text-white'
            }`
          }
        >
          <LayoutDashboard className="h-4 w-4 flex-shrink-0" />
          Dashboard
        </NavLink>
      </div>

      {/* Scrollable nav */}
      <nav className="flex-1 overflow-y-auto py-2 px-0">
        {visibleSections.map(section => (
          <SidebarSection key={section.label} section={section} />
        ))}
      </nav>

      {/* Footer */}
      <div className="px-4 py-3 border-t border-white/10">
        <p className="text-xs text-gray-500">v0.1.0 · Coteja ERP</p>
      </div>
    </aside>
  )
}
