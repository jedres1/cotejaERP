-- ============================================================
-- V3 - Contabilidad e Inventario
-- ============================================================

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    address VARCHAR(500),
    phone VARCHAR(50),
    contact_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wh_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_wh_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS billing_product_warehouse (
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    stock_quantity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id, warehouse_id),
    CONSTRAINT fk_bpw_product FOREIGN KEY (product_id) REFERENCES billing_products(id),
    CONSTRAINT fk_bpw_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    type ENUM('entry','exit') NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    document_type VARCHAR(20) DEFAULT 'manual',
    document_number VARCHAR(100),
    billing_invoice_id BIGINT,
    purchase_invoice_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_im_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_im_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_im_product FOREIGN KEY (product_id) REFERENCES billing_products(id),
    CONSTRAINT fk_im_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_im_billing FOREIGN KEY (billing_invoice_id) REFERENCES billing_invoices(id),
    CONSTRAINT fk_im_purchase FOREIGN KEY (purchase_invoice_id) REFERENCES purchase_invoices(id)
);

CREATE TABLE IF NOT EXISTS accounting_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type ENUM('activo','pasivo','patrimonio','gasto','ingreso') NOT NULL,
    nature ENUM('deudora','acreedora') NOT NULL,
    parent_id BIGINT,
    level INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_aa_type_active (type, is_active),
    INDEX idx_aa_parent (parent_id),
    CONSTRAINT fk_aa_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_aa_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_aa_parent FOREIGN KEY (parent_id) REFERENCES accounting_accounts(id)
);

CREATE TABLE IF NOT EXISTS cost_centers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    parent_id BIGINT,
    level INT NOT NULL DEFAULT 1,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cc_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_cc_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_cc_parent FOREIGN KEY (parent_id) REFERENCES cost_centers(id)
);

CREATE TABLE IF NOT EXISTS accounting_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type ENUM('automatico','manual') NOT NULL DEFAULT 'automatico',
    debit_account_id BIGINT,
    credit_account_id BIGINT,
    secondary_debit_account_id BIGINT,
    secondary_credit_account_id BIGINT,
    cost_center_id BIGINT,
    last_correlative INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ap_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_ap_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_ap_debit FOREIGN KEY (debit_account_id) REFERENCES accounting_accounts(id),
    CONSTRAINT fk_ap_credit FOREIGN KEY (credit_account_id) REFERENCES accounting_accounts(id),
    CONSTRAINT fk_ap_sec_debit FOREIGN KEY (secondary_debit_account_id) REFERENCES accounting_accounts(id),
    CONSTRAINT fk_ap_sec_credit FOREIGN KEY (secondary_credit_account_id) REFERENCES accounting_accounts(id),
    CONSTRAINT fk_ap_cost_center FOREIGN KEY (cost_center_id) REFERENCES cost_centers(id)
);

CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    entry_number VARCHAR(30) NOT NULL,
    entry_date DATE NOT NULL,
    description TEXT NOT NULL,
    reference VARCHAR(255),
    status ENUM('borrador','aprobado','anulado') NOT NULL DEFAULT 'borrador',
    created_by BIGINT,
    updated_by BIGINT,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    notes TEXT,
    accounting_package_id BIGINT,
    source_type VARCHAR(30),
    source_id BIGINT,
    source_document VARCHAR(100),
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_je_status_date (status, entry_date),
    CONSTRAINT fk_je_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_je_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_je_package FOREIGN KEY (accounting_package_id) REFERENCES accounting_packages(id),
    CONSTRAINT fk_je_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_je_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS journal_entry_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    cost_center_id BIGINT,
    description VARCHAR(255),
    debit DECIMAL(15,2) NOT NULL DEFAULT 0,
    credit DECIMAL(15,2) NOT NULL DEFAULT 0,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_jel_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_jel_account FOREIGN KEY (account_id) REFERENCES accounting_accounts(id),
    CONSTRAINT fk_jel_cost_center FOREIGN KEY (cost_center_id) REFERENCES cost_centers(id)
);

CREATE TABLE IF NOT EXISTS accounting_year_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    year INT NOT NULL,
    name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'abierto',
    opened_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    closed_by BIGINT,
    notes TEXT,
    CONSTRAINT fk_ayp_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_ayp_closed_by FOREIGN KEY (closed_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS accounting_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'abierto',
    opened_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    closed_by BIGINT,
    notes TEXT,
    CONSTRAINT fk_ap2_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_ap2_closed_by FOREIGN KEY (closed_by) REFERENCES users(id)
);
