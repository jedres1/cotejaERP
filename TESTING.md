# Coteja ERP — Guía de pruebas

## Credenciales de acceso

| Campo    | Valor                  |
|----------|------------------------|
| URL      | http://localhost:5173  |
| Email    | `admin@coteja.com`     |
| Password | `Admin1234!`           |
| Rol      | ADMIN (acceso total)   |
| Empresa  | Empresa Demo SA de CV  |

---

## Requisitos previos

| Herramienta | Versión mínima | Instalación |
|-------------|----------------|-------------|
| Java JDK    | 21             | https://adoptium.net |
| Maven       | incluido (`./mvnw`) | — |
| MySQL       | 8.0            | https://dev.mysql.com/downloads/mysql/ |
| Node.js     | 18+            | https://nodejs.org |

> **MySQL:** el usuario `root` debe tener password `1234` y permisos para crear bases de datos,  
> o bien edita `src/main/resources/application.properties` con tus credenciales.

---

## Clonar y ejecutar

### 1. Clonar el repositorio

```bash
git clone https://github.com/jedres1/cotejaERP.git
cd cotejaERP
```

### 2. Iniciar el backend (Spring Boot)

```bash
# Windows
mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

Al primer arranque, Flyway crea la base de datos `coteja_erp` y ejecuta las migraciones V1–V5.  
El seed V5 inserta el usuario admin y la empresa demo.

El backend queda en: **http://localhost:8080**

### 3. Iniciar el frontend (React)

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

La app queda en: **http://localhost:5173**

---

## Pasos para probar en el navegador

### Paso 1 — Login

1. Abre http://localhost:5173
2. Ingresa `admin@coteja.com` / `Admin1234!`
3. Haz clic en **Iniciar sesión**
4. Serás redirigido al Dashboard

### Paso 2 — Seleccionar empresa

En el Topbar (esquina superior derecha) verás **"Sin empresa"**.  
Haz clic → selecciona **"Empresa Demo SA de CV"** → el sistema carga los módulos de esa empresa.

### Paso 3 — Módulos disponibles para probar

| Módulo | Ruta | Qué probar |
|--------|------|------------|
| **Facturación** | /billing/clientes | Crear un cliente (consumidor final o empresa) |
| **Facturación** | /billing/productos | Crear un producto o servicio |
| **Facturación** | /billing/facturas | Emitir una factura DTE (requiere firmador) |
| **Contabilidad** | /contabilidad/catalogo | Crear cuentas contables |
| **Contabilidad** | /contabilidad/diario | Crear y aprobar partidas |
| **Nómina** | /nomina/empleados | Crear empleados con datos ISSS/AFP |
| **Nómina** | /nomina/periodos | Crear período de nómina → aplicar |
| **Compras** | /compras/proveedores | Registrar proveedor |
| **Compras** | /compras/facturas | Registrar factura de compra → aprobar |
| **Inventario** | /inventario/movimientos | Registrar entrada/salida de inventario |
| **Banca** | /banca/cuentas | Crear cuenta bancaria |
| **Banca** | /banca/transacciones | Registrar transacción |
| **Admin** | /admin/clientes | Gestionar clientes del SaaS (solo ADMIN) |
| **Admin** | /admin/empresas | Gestionar empresas y módulos activos |
| **Admin** | /admin/usuarios | Crear usuarios con rol INQUILINO |

---

## Crear un usuario INQUILINO (cliente real)

1. Ve a **Admin → Clientes** → crea un cliente
2. Ve a **Admin → Empresas** → crea una empresa para ese cliente, activa los módulos que necesita
3. Ve a **Admin → Usuarios** → crea un usuario con rol `INQUILINO` y asigna módulos
4. Ese usuario podrá hacer login y ver solo sus empresas y módulos asignados

---

## Configurar DTE El Salvador

Para emitir facturas electrónicas reales necesitas el firmador de Hacienda:

1. Descarga e instala el firmador del MH en el servidor
2. Edita `application.properties`:
   ```properties
   coteja.dte.firmador.url=http://localhost:8113
   ```
3. Para pruebas, el sistema usa `environment=pruebas` en la empresa demo

---

## Variables de entorno opcionales

Crea un archivo `.env` o configura las siguientes variables para producción:

```properties
# Base de datos
SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/coteja_erp?createDatabaseIfNotExist=true
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=password

# DTE
DTE_FIRMADOR_URL=http://localhost:8113

# Email salida (SMTP)
MAIL_USERNAME=noreply@tuempresa.com
MAIL_PASSWORD=password_smtp

# Email entrada (importación facturas de compra)
PURCHASE_INVOICE_MAIL_USERNAME=facturas@tuempresa.com
PURCHASE_INVOICE_MAIL_PASSWORD=password_imap
```

---

## Estructura del proyecto

```
cotejaERP/
├── src/main/java/com/coteja/erp/
│   ├── config/          # Security, CORS, JPA, MVC
│   ├── common/          # AuditableEntity, TenantContext, excepciones
│   ├── security/        # UserPrincipal, CustomUserDetailsService
│   └── module/
│       ├── admin/       # Clientes, Empresas, Usuarios, Planes, Licencias
│       ├── auth/        # AuthController (/api/auth/me, /api/auth/logout)
│       ├── billing/     # Facturación DTE El Salvador
│       ├── accounting/  # Contabilidad, diario, períodos
│       ├── payroll/     # Nómina, empleados, períodos
│       ├── purchases/   # Compras, proveedores, importación IMAP
│       ├── inventory/   # Inventario, bodegas, movimientos
│       └── banking/     # Control bancario, conciliación
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Flyway V1–V5
└── frontend/            # React + Vite + Tailwind
    ├── src/
    │   ├── api/         # Funciones axios por módulo
    │   ├── components/  # UI primitivos + layout
    │   ├── context/     # AuthContext (sesión + empresa)
    │   ├── pages/       # Una carpeta por módulo
    │   └── router.tsx
    └── package.json
```

---

## Tecnologías

**Backend:** Spring Boot 4.1.2 · Java 21 · MySQL 8 · Flyway · Spring Security 7  
**Frontend:** React 18 · TypeScript · Vite · Tailwind CSS 3 · TanStack Query v5 · React Router 6
