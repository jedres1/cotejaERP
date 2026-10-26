-- ============================================================
-- V4 - Nómina y Control Bancario
-- ============================================================

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    dui VARCHAR(20),
    isss_number VARCHAR(30),
    nup VARCHAR(30),
    afp ENUM('crecer','confia'),
    position VARCHAR(100),
    department VARCHAR(100),
    hire_date DATE NOT NULL,
    salary DECIMAL(10,2) NOT NULL DEFAULT 0,
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_emp_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS payroll_concepts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type ENUM('beneficio','deduccion') NOT NULL,
    calculation_method ENUM('fijo','porcentaje','formula','editable') NOT NULL DEFAULT 'fijo',
    amount DECIMAL(10,2) DEFAULT 0,
    rate DECIMAL(8,4) DEFAULT 0,
    formula TEXT,
    account_id BIGINT,
    payable_account_id BIGINT,
    system_key VARCHAR(30),
    applies_to_all BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pc_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_pc_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_pc_account FOREIGN KEY (account_id) REFERENCES accounting_accounts(id),
    CONSTRAINT fk_pc_payable FOREIGN KEY (payable_account_id) REFERENCES accounting_accounts(id)
);

CREATE TABLE IF NOT EXISTS employee_payroll_concept (
    employee_id BIGINT NOT NULL,
    payroll_concept_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, payroll_concept_id),
    CONSTRAINT fk_epc_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_epc_concept FOREIGN KEY (payroll_concept_id) REFERENCES payroll_concepts(id)
);

CREATE TABLE IF NOT EXISTS payroll_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status ENUM('borrador','aplicado') NOT NULL DEFAULT 'borrador',
    notes TEXT,
    journal_entry_id BIGINT,
    applied_at TIMESTAMP NULL,
    applied_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pp_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_pp_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_pp_journal FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id),
    CONSTRAINT fk_pp_applied_by FOREIGN KEY (applied_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS payroll_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_period_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    salary DECIMAL(10,2) NOT NULL DEFAULT 0,
    overtime_hours DECIMAL(5,2) DEFAULT 0,
    overtime_amount DECIMAL(10,2) DEFAULT 0,
    bonuses DECIMAL(10,2) DEFAULT 0,
    gross_salary DECIMAL(10,2) NOT NULL DEFAULT 0,
    isss_employee DECIMAL(10,2) DEFAULT 0,
    afp_employee DECIMAL(10,2) DEFAULT 0,
    isr DECIMAL(10,2) DEFAULT 0,
    total_deductions DECIMAL(10,2) DEFAULT 0,
    net_salary DECIMAL(10,2) NOT NULL DEFAULT 0,
    isss_employer DECIMAL(10,2) DEFAULT 0,
    afp_employer DECIMAL(10,2) DEFAULT 0,
    total_employer_cost DECIMAL(10,2) DEFAULT 0,
    concept_details JSON,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pl_period FOREIGN KEY (payroll_period_id) REFERENCES payroll_periods(id),
    CONSTRAINT fk_pl_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE IF NOT EXISTS payroll_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    accounting_package_id BIGINT,
    account_salaries_id BIGINT,
    account_isss_employer_id BIGINT,
    account_afp_employer_id BIGINT,
    account_isss_payable_id BIGINT,
    account_afp_payable_id BIGINT,
    account_isr_payable_id BIGINT,
    account_salaries_payable_id BIGINT,
    isss_salary_cap DECIMAL(10,2) DEFAULT 1000.00,
    isss_employee_rate DECIMAL(5,4) DEFAULT 0.0300,
    isss_employer_rate DECIMAL(5,4) DEFAULT 0.0750,
    afp_employee_rate DECIMAL(5,4) DEFAULT 0.0725,
    afp_employer_rate DECIMAL(5,4) DEFAULT 0.0875,
    CONSTRAINT fk_ps_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    account_type VARCHAR(30),
    currency VARCHAR(10) DEFAULT 'USD',
    opening_balance DECIMAL(15,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ba_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_ba_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS bank_reconciliations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_account_id BIGINT NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    statement_file_path VARCHAR(500),
    statement_balance DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'open',
    completed_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_br_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id)
);

CREATE TABLE IF NOT EXISTS bank_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_account_id BIGINT NOT NULL,
    reconciliation_id BIGINT,
    type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    bank_account VARCHAR(50),
    amount DECIMAL(15,2) NOT NULL,
    reference VARCHAR(255),
    notes TEXT,
    is_reconciled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bt_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id),
    CONSTRAINT fk_bt_reconciliation FOREIGN KEY (reconciliation_id) REFERENCES bank_reconciliations(id)
);

CREATE TABLE IF NOT EXISTS bank_transaction_purchase_invoice (
    bank_transaction_id BIGINT NOT NULL,
    purchase_invoice_id BIGINT NOT NULL,
    amount_applied DECIMAL(15,2) NOT NULL,
    UNIQUE KEY uk_btpi (bank_transaction_id, purchase_invoice_id),
    CONSTRAINT fk_btpi_transaction FOREIGN KEY (bank_transaction_id) REFERENCES bank_transactions(id),
    CONSTRAINT fk_btpi_invoice FOREIGN KEY (purchase_invoice_id) REFERENCES purchase_invoices(id)
);

-- Agregar FK diferida a billing_invoice_payments
ALTER TABLE billing_invoice_payments
    ADD CONSTRAINT fk_bip_bank_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id),
    ADD CONSTRAINT fk_bip_reconciliation FOREIGN KEY (reconciliation_id) REFERENCES bank_reconciliations(id);
