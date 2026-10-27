-- ============================================================
-- cotejaERP - Datos de demo / seed inicial
-- V5 - Usuario ADMIN + Cliente + Empresa demo
-- Password: Admin1234!
-- ============================================================

-- Cliente demo
INSERT INTO customers (name, email, phone, document_type, document_number, nrc,
                       business_activity, address, module_accesses, status)
VALUES ('Coteja Demo SA', 'demo@coteja.com', '7000-0000', 'NIT', '0614-010101-001-0', '123456',
        'Comercio al por mayor', 'San Salvador, El Salvador',
        '["billing","accounting","payroll","purchases","inventory","banking"]',
        'active');

-- Usuario ADMIN (sin customer_id — acceso global)
-- Password: Admin1234!  (BCrypt cost 10)
INSERT INTO users (customer_id, name, email, password, role, is_active, module_accesses)
VALUES (NULL,
        'Administrador',
        'admin@coteja.com',
        '$2b$10$nP5VTIvZ4WO5ho12qqjS4eF.QfnzXV8ycFu78IkqVp4qjnpKhmcSi',
        'ADMIN',
        TRUE,
        '["billing","accounting","payroll","purchases","inventory","banking"]');

-- Empresa demo ligada al cliente
INSERT INTO companies (customer_id, business_name, trade_name, nit_dui, nrc,
                       tipo_persona, business_activity, address, email, phone,
                       environment, module_accesses)
VALUES (1,
        'Empresa Demo SA de CV',
        'Demo ERP',
        '0614-010101-001-0',
        '123456',
        'juridica',
        'Comercio',
        'Calle Demo #1, San Salvador',
        'info@demo.com',
        '7000-0000',
        'pruebas',
        '["billing","accounting","payroll","purchases","inventory","banking"]');

-- Asignar empresa al usuario admin
INSERT INTO company_user (company_id, user_id) VALUES (1, 1);
