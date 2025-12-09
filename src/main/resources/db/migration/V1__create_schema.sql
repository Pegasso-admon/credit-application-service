-- V1__create_schema.sql
-- Initial schema creation for CoopCredit application
-- Creates all base tables with proper constraints and indexes

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    document VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_AFILIADO', 'ROLE_ANALISTA', 'ROLE_ADMIN')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_document ON users(document);
CREATE INDEX idx_email ON users(email);

COMMENT ON TABLE users IS 'System users with authentication and authorization';
COMMENT ON COLUMN users.role IS 'User role: ROLE_AFILIADO, ROLE_ANALISTA, or ROLE_ADMIN';

-- Affiliates table
CREATE TABLE affiliates (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    salary NUMERIC(15, 2) NOT NULL CHECK (salary > 0),
    affiliation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_affiliate_document ON affiliates(document);
CREATE INDEX idx_affiliate_status ON affiliates(status);
CREATE INDEX idx_affiliate_affiliation_date ON affiliates(affiliation_date);

COMMENT ON TABLE affiliates IS 'Cooperative affiliates who can request credits';
COMMENT ON COLUMN affiliates.salary IS 'Monthly salary in local currency';
COMMENT ON COLUMN affiliates.status IS 'Affiliate status: ACTIVE or INACTIVE';

-- Credit applications table
CREATE TABLE credit_applications (
    id BIGSERIAL PRIMARY KEY,
    affiliate_id BIGINT NOT NULL,
    requested_amount NUMERIC(15, 2) NOT NULL CHECK (requested_amount > 0),
    term_months INTEGER NOT NULL CHECK (term_months > 0 AND term_months <= 360),
    proposed_rate NUMERIC(5, 2) NOT NULL CHECK (proposed_rate >= 0),
    application_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_credit_affiliate ON credit_applications(affiliate_id);
CREATE INDEX idx_credit_status ON credit_applications(status);
CREATE INDEX idx_credit_application_date ON credit_applications(application_date);

COMMENT ON TABLE credit_applications IS 'Credit applications submitted by affiliates';
COMMENT ON COLUMN credit_applications.status IS 'Application status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN credit_applications.proposed_rate IS 'Annual interest rate percentage';

-- Risk evaluations table
CREATE TABLE risk_evaluations (
    id BIGSERIAL PRIMARY KEY,
    credit_application_id BIGINT NOT NULL UNIQUE,
    score INTEGER NOT NULL CHECK (score >= 300 AND score <= 950),
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    debt_to_income_ratio NUMERIC(5, 2) NOT NULL CHECK (debt_to_income_ratio >= 0),
    meets_minimum_seniority BOOLEAN NOT NULL,
    meets_maximum_amount BOOLEAN NOT NULL,
    approved BOOLEAN NOT NULL,
    evaluation_detail VARCHAR(500),
    evaluated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_risk_credit_application ON risk_evaluations(credit_application_id);
CREATE INDEX idx_risk_level ON risk_evaluations(risk_level);
CREATE INDEX idx_risk_score ON risk_evaluations(score);
CREATE INDEX idx_risk_approved ON risk_evaluations(approved);

COMMENT ON TABLE risk_evaluations IS 'Risk evaluations for credit applications';
COMMENT ON COLUMN risk_evaluations.score IS 'Credit score from external risk service (300-950)';
COMMENT ON COLUMN risk_evaluations.risk_level IS 'Risk level: LOW, MEDIUM, or HIGH';
COMMENT ON COLUMN risk_evaluations.debt_to_income_ratio IS 'Monthly payment to income ratio';