import { createBrowserRouter, Navigate } from 'react-router-dom'
import AppShell from './components/layout/AppShell'
import ProtectedRoute from './components/layout/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/admin/CustomersPage'
import CompaniesPage from './pages/admin/CompaniesPage'
import UsersPage from './pages/admin/UsersPage'
import PlansPage from './pages/admin/PlansPage'
import LicensesPage from './pages/admin/LicensesPage'
import BillingInvoicesPage from './pages/billing/BillingInvoicesPage'
import BillingClientsPage from './pages/billing/BillingClientsPage'
import BillingProductsPage from './pages/billing/BillingProductsPage'
import AccountCatalogPage from './pages/accounting/AccountCatalogPage'
import JournalPage from './pages/accounting/JournalPage'
import EmployeesPage from './pages/payroll/EmployeesPage'
import PayrollPeriodsPage from './pages/payroll/PayrollPeriodsPage'
import PurchaseInvoicesPage from './pages/purchases/PurchaseInvoicesPage'
import SuppliersPage from './pages/purchases/SuppliersPage'
import InventoryMovementsPage from './pages/inventory/InventoryMovementsPage'
import BankAccountsPage from './pages/banking/BankAccountsPage'
import BankTransactionsPage from './pages/banking/BankTransactionsPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <ProtectedRoute><AppShell /></ProtectedRoute>,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: '/dashboard', element: <DashboardPage /> },
      { path: '/admin/clientes', element: <CustomersPage /> },
      { path: '/admin/empresas', element: <CompaniesPage /> },
      { path: '/admin/usuarios', element: <UsersPage /> },
      { path: '/admin/planes', element: <PlansPage /> },
      { path: '/admin/licencias', element: <LicensesPage /> },
      { path: '/billing/facturas', element: <BillingInvoicesPage /> },
      { path: '/billing/clientes', element: <BillingClientsPage /> },
      { path: '/billing/productos', element: <BillingProductsPage /> },
      { path: '/contabilidad/catalogo', element: <AccountCatalogPage /> },
      { path: '/contabilidad/diario', element: <JournalPage /> },
      { path: '/nomina/empleados', element: <EmployeesPage /> },
      { path: '/nomina/periodos', element: <PayrollPeriodsPage /> },
      { path: '/compras/facturas', element: <PurchaseInvoicesPage /> },
      { path: '/compras/proveedores', element: <SuppliersPage /> },
      { path: '/inventario/movimientos', element: <InventoryMovementsPage /> },
      { path: '/banca/cuentas', element: <BankAccountsPage /> },
      { path: '/banca/transacciones', element: <BankTransactionsPage /> },
    ]
  },
  { path: '*', element: <Navigate to="/dashboard" replace /> }
])
