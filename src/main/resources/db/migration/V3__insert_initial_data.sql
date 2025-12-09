-- V3__insert_initial_data.sql
-- Inserts initial test data for development and testing
-- Passwords are BCrypt encoded 'password123'

-- Insert test users (password: password123)
INSERT INTO users (username, document, password, full_name, email, role, active) VALUES
('admin', '1000000001', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'System Administrator', 'admin@coopcredit.com', 'ROLE_ADMIN', true),
('analyst1', '1000000002', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Maria Rodriguez', 'analyst1@coopcredit.com', 'ROLE_ANALISTA', true),
('affiliate1', '1017654321', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Juan Perez', 'affiliate1@coopcredit.com', 'ROLE_AFILIADO', true),
('affiliate2', '1018765432', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Ana Garcia', 'affiliate2@coopcredit.com', 'ROLE_AFILIADO', true);

-- Insert test affiliates
INSERT INTO affiliates (document, full_name, salary, affiliation_date, status) VALUES
('1017654321', 'Juan Perez', 3500000.00, '2022-01-15', 'ACTIVE'),
('1018765432', 'Ana Garcia', 4200000.00, '2021-06-20', 'ACTIVE'),
('1019876543', 'Carlos Lopez', 5500000.00, '2020-03-10', 'ACTIVE'),
('1020987654', 'Laura Martinez', 2800000.00, '2023-11-05', 'ACTIVE'),
('1021098765', 'Pedro Sanchez', 6000000.00, '2019-08-18', 'INACTIVE');

-- Insert test credit applications
INSERT INTO credit_applications (affiliate_id, requested_amount, term_months, proposed_rate, application_date, status, rejection_reason) VALUES
(1, 15000000.00, 36, 12.50, '2024-01-10', 'PENDING', NULL),
(2, 8000000.00, 24, 11.75, '2024-01-15', 'PENDING', NULL),
(3, 25000000.00, 48, 13.00, '2024-01-05', 'APPROVED', NULL),
(1, 5000000.00, 12, 10.50, '2023-12-20', 'APPROVED', NULL),
(4, 12000000.00, 36, 12.25, '2024-01-12', 'REJECTED', 'Insufficient seniority: only 2 months since affiliation');

-- Insert test risk evaluations for approved/rejected applications
INSERT INTO risk_evaluations (credit_application_id, score, risk_level, debt_to_income_ratio, meets_minimum_seniority, meets_maximum_amount, approved, evaluation_detail, evaluated_at) VALUES
(3, 785, 'LOW', 28.50, true, true, true, 'Excellent credit history. Low risk of default.', '2024-01-06 10:30:00'),
(4, 820, 'LOW', 15.20, true, true, true, 'Excellent credit history. Low default risk. Approved for requested amount.', '2023-12-21 09:15:00'),
(5, 642, 'MEDIUM', 45.80, false, true, false, 'Moderate credit history. Insufficient seniority: only 2 months.', '2024-01-13 11:45:00');

COMMENT ON TABLE users IS 'Test users - password for all: password123';