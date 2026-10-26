-- ============================================================
-- cotejaERP - Schema inicial
-- V1 - Tablas base: customers, users, companies, plans, licenses
-- ============================================================

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    document_type VARCHAR(20),
    document_number VARCHAR(50),
    nrc VARCHAR(50),
    trade_name VARCHAR(255),
    business_activity VARCHAR(255),
    activity_description TEXT,
    address_department VARCHAR(100),
    address_municipality VARCHAR(100),
    address VARCHAR(500),
    preferred_dte_type VARCHAR(10),
    billing_email VARCHAR(255),
    billing_phone VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    notes TEXT,
    module_accesses JSON,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'customer',
    is_active BOOLEAN DEFAULT TRUE,
    module_accesses JSON,
    email_verified_at TIMESTAMP NULL,
    remember_token VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    nit_dui VARCHAR(50),
    nrc VARCHAR(50),
    tipo_persona VARCHAR(20),
    business_activity VARCHAR(255),
    address_department VARCHAR(100),
    address_municipality VARCHAR(100),
    address_district VARCHAR(100),
    address VARCHAR(500),
    email VARCHAR(255),
    phone VARCHAR(50),
    environment VARCHAR(20) DEFAULT 'production',
    module_accesses JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_companies_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS company_user (
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, user_id),
    CONSTRAINT fk_cu_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_cu_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    annual_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    implementation_fee DECIMAL(10,2) DEFAULT 0,
    additional_user_price DECIMAL(10,2) DEFAULT 0,
    included_users INT DEFAULT 1,
    max_devices INT DEFAULT 1,
    support_included BOOLEAN DEFAULT FALSE,
    cloud_backup BOOLEAN DEFAULT FALSE,
    unlimited_documents BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS licenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    license_key VARCHAR(100) NOT NULL UNIQUE,
    license_type VARCHAR(20) DEFAULT 'propietario',
    status VARCHAR(20) DEFAULT 'active',
    starts_at DATE NOT NULL,
    expires_at DATE NOT NULL,
    max_users INT DEFAULT 1,
    max_devices INT DEFAULT 1,
    grace_days INT DEFAULT 0,
    last_validated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_licenses_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_licenses_plan FOREIGN KEY (plan_id) REFERENCES plans(id)
);

CREATE TABLE IF NOT EXISTS license_devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    activated_at TIMESTAMP NULL,
    last_seen_at TIMESTAMP NULL,
    UNIQUE KEY uk_license_device (license_id, device_id),
    CONSTRAINT fk_ld_license FOREIGN KEY (license_id) REFERENCES licenses(id)
);

CREATE TABLE IF NOT EXISTS license_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(20) DEFAULT 'operator',
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_lu_license FOREIGN KEY (license_id) REFERENCES licenses(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    period VARCHAR(20) DEFAULT 'monthly',
    method VARCHAR(50),
    reference VARCHAR(255),
    status VARCHAR(20) DEFAULT 'paid',
    paid_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_license FOREIGN KEY (license_id) REFERENCES licenses(id),
    CONSTRAINT fk_payments_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS backups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    filename VARCHAR(500) NOT NULL,
    path VARCHAR(1000),
    size_bytes BIGINT,
    checksum VARCHAR(255),
    encryption VARCHAR(50) DEFAULT 'aes-256-gcm',
    compression VARCHAR(20) DEFAULT 'gzip',
    device_id VARCHAR(255),
    uploaded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_backups_license FOREIGN KEY (license_id) REFERENCES licenses(id),
    CONSTRAINT fk_backups_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS app_releases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform VARCHAR(20) NOT NULL,
    version VARCHAR(30) NOT NULL,
    filename VARCHAR(500),
    download_url VARCHAR(1000),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Datos iniciales
INSERT INTO plans (code, name, monthly_price, annual_price, included_users, max_devices, is_active)
VALUES
    ('LOCAL', 'Plan Local', 25.00, 270.00, 1, 1, TRUE),
    ('EMPRESAS', 'Plan Empresas', 75.00, 810.00, 5, 5, TRUE);
