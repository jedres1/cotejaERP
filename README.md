# Coteja ERP

Sistema ERP multi-empresa para PYMES de El Salvador. Incluye facturación electrónica DTE integrada con el Ministerio de Hacienda, contabilidad, nómina, compras, inventario y control bancario.

---

## Módulos

| Módulo | Descripción |
|--------|-------------|
| **Facturación DTE** | Emisión de CF (01), CCF (03), Nota de Crédito (05) y Débito (06) vía firmador MH |
| **Contabilidad** | Catálogo de cuentas, diario, períodos, partidas automáticas |
| **Nómina** | Empleados, conceptos, cálculo ISSS/AFP/ISR El Salvador, períodos |
| **Compras** | Facturas de proveedores, flujo de aprobación, importación automática por IMAP |
| **Inventario** | Bodegas, tipos de producto, movimientos (entrada/salida/ajuste) |
| **Banca** | Cuentas bancarias, transacciones, conciliaciones |
| **Admin SaaS** | Clientes, empresas, planes, licencias, usuarios, dispositivos |

---

## Stack tecnológico

**Backend**
- Java 21 · Spring Boot 4.1.2
- Spring Security 7 (sesiones, BCrypt)
- Spring Data JPA · Hibernate · MySQL 8
- Flyway (migraciones)
- Jakarta Mail (importación IMAP)

**Frontend**
- React 18 · TypeScript · Vite
- Tailwind CSS 3
- TanStack Query v5 · React Router 6
- React Hook Form · Zod · Axios
- Lucide React

---

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| Java JDK | 21+ |
| MySQL | 8.0+ |
| Node.js | 18+ |

---

## Instalación rápida

```bash
git clone https://github.com/jedres1/cotejaERP.git
cd cotejaERP
```

### 1. Configurar base de datos

Edita `src/main/resources/application.properties` si tu MySQL no usa `root/1234`:

```properties
spring.datasource.username=root
spring.datasource.password=1234
```

### 2. Iniciar el backend

```bash
# Windows
mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

Flyway crea la base de datos `coteja_erp` y carga el schema + datos de demo automáticamente.

### 3. Iniciar el frontend

```bash
cd frontend
npm install
npm run dev
```

Abre **http://localhost:5173**

---

## Acceso de demo

| Campo | Valor |
|-------|-------|
| Email | `admin@coteja.com` |
| Password | `Admin1234!` |
| Empresa | Empresa Demo SA de CV |

> Ver [`TESTING.md`](TESTING.md) para instrucciones detalladas de prueba por módulo.

---

## Estructura del proyecto

```
cotejaERP/
├── src/main/java/com/coteja/erp/
│   ├── config/              # Security, CORS, JPA, MVC, RestTemplate
│   ├── common/              # AuditableEntity, TenantContext, excepciones globales
│   ├── security/            # UserPrincipal, CustomUserDetailsService, ModuleAccessEvaluator
│   └── module/
│       ├── admin/           # SaaS: clientes, empresas, planes, licencias, usuarios
│       ├── auth/            # AuthController REST (/api/auth/me · /api/auth/logout)
│       ├── billing/         # DTE: facturas, clientes, productos, configuración
│       ├── accounting/      # Contabilidad: catálogo, diario, períodos, reportes
│       ├── payroll/         # Nómina: empleados, conceptos, períodos, configuración
│       ├── purchases/       # Compras: facturas, proveedores, importador IMAP
│       ├── inventory/       # Inventario: bodegas, tipos, movimientos
│       └── banking/         # Banca: cuentas, transacciones, conciliaciones
│
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       ├── V1__init_schema.sql          # Tablas base: users, companies, customers...
│       ├── V2__billing_and_purchases.sql
│       ├── V3__accounting_and_inventory.sql
│       ├── V4__payroll_and_banking.sql
│       └── V5__seed_demo_data.sql       # Usuario admin + empresa demo
│
└── frontend/
    ├── src/
    │   ├── api/             # Clientes Axios por módulo
    │   ├── components/
    │   │   ├── ui/          # Button, Input, Modal, Table, Badge, Pagination…
    │   │   └── layout/      # AppShell, Sidebar, Topbar, ProtectedRoute
    │   ├── context/         # AuthContext (sesión + empresa activa)
    │   ├── pages/           # Una carpeta por módulo (19 páginas)
    │   ├── types/           # Tipos TypeScript de API y auth
    │   └── router.tsx
    └── package.json
```

---

## Multi-tenancy

Cada request autenticado lleva en la sesión HTTP el `company_id` activo. El `TenantInterceptor` lo lee y lo inyecta en `TenantContext` (ThreadLocal). Todos los servicios usan `TenantContext.getCompanyId()` para aislar los datos por empresa.

## Control de acceso a módulos

El rol `ADMIN` tiene acceso total. El rol `INQUILINO` solo accede a los módulos habilitados en su empresa (`company.moduleAccesses`). El evaluador `@moduleAccess.hasAccess(auth, 'billing')` en los `@PreAuthorize` aplica esto a nivel de endpoint.

## Partidas contables automáticas

`AccountingEntryService` genera partidas del diario automáticamente al:
- Emitir una factura DTE → debe CxC, haber Ventas + IVA
- Aprobar una factura de compra → debe Gastos + IVA, haber CxP
- Registrar un movimiento de inventario → debe/haber Inventario / Costo
- Aplicar un período de nómina → debe Sueldos + cuotas patronales

## DTE El Salvador

La integración con Hacienda requiere el firmador externo. Configura la URL en `application.properties`:

```properties
coteja.dte.firmador.url=http://localhost:8113
```

El sistema soporta modo **contingencia**: si el firmador no está disponible, las facturas se marcan como `CONTINGENCIA` y se pueden enviar en lote cuando el servicio se recupere.

---

## Variables de entorno

Para producción, configura estas variables de entorno (o un archivo `application-prod.properties`):

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/coteja_erp
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=contraseña
DTE_FIRMADOR_URL=http://firmador:8113
MAIL_USERNAME=smtp@empresa.com
MAIL_PASSWORD=clave_smtp
PURCHASE_INVOICE_MAIL_USERNAME=facturas@empresa.com
PURCHASE_INVOICE_MAIL_PASSWORD=clave_imap
```

---

## Licencia

Uso interno / privado. © 2024 Coteja ERP.
