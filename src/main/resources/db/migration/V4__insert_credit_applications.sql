-- V4__insert_credit_applications.sql
-- Insert test data for credit applications with valid interest rates

-- Insert test affiliates (PostgreSQL-compatible using INSERT ... ON CONFLICT)
INSERT INTO affiliates (document, full_name, salary, affiliation_date, status, created_at, updated_at)
VALUES 
    ('11111111', 'Carlos Rodriguez', 4500000, '2022-01-15', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222', 'Maria Gonzalez', 6000000, '2021-06-20', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333', 'Luis Martinez', 3500000, '2023-03-10', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444', 'Ana Jimenez', 5500000, '2020-11-05', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555', 'Pedro Sanchez', 4000000, '2022-08-22', 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (document) DO NOTHING;

-- Insert credit applications with VALID proposed rates (between 0 and 100)
-- Schema: affiliate_id, requested_amount, term_months, proposed_rate, application_date, status, rejection_reason
INSERT INTO credit_applications (
    affiliate_id, 
    requested_amount, 
    term_months, 
    proposed_rate, 
    application_date, 
    status, 
    rejection_reason,
    created_at, 
    updated_at
)
SELECT 
    a.id,
    CASE 
        WHEN a.full_name = 'Carlos Rodriguez' THEN 20000000
        WHEN a.full_name = 'Maria Gonzalez' THEN 35000000
        WHEN a.full_name = 'Luis Martinez' THEN 15000000
        WHEN a.full_name = 'Ana Jimenez' THEN 25000000
    END,
    CASE 
        WHEN a.full_name = 'Carlos Rodriguez' THEN 36
        WHEN a.full_name = 'Maria Gonzalez' THEN 48
        WHEN a.full_name = 'Luis Martinez' THEN 24
        WHEN a.full_name = 'Ana Jimenez' THEN 36
    END,
    CASE  
        WHEN a.full_name = 'Carlos Rodriguez' THEN 12.5
        WHEN a.full_name = 'Maria Gonzalez' THEN 9.8
        WHEN a.full_name = 'Luis Martinez' THEN 15.0
        WHEN a.full_name = 'Ana Jimenez' THEN 11.2
    END,
    CASE 
        WHEN a.full_name = 'Carlos Rodriguez' THEN CURRENT_TIMESTAMP - INTERVAL '5 days'
        WHEN a.full_name = 'Maria Gonzalez' THEN CURRENT_TIMESTAMP - INTERVAL '10 days'
        WHEN a.full_name = 'Luis Martinez' THEN CURRENT_TIMESTAMP - INTERVAL '2 days'
        WHEN a.full_name = 'Ana Jimenez' THEN CURRENT_TIMESTAMP - INTERVAL '1 day'
    END,
    CASE 
        WHEN a.full_name = 'Carlos Rodriguez' THEN 'APPROVED'
        WHEN a.full_name = 'Maria Gonzalez' THEN 'APPROVED'
        WHEN a.full_name = 'Luis Martinez' THEN 'PENDING'
        WHEN a.full_name = 'Ana Jimenez' THEN 'PENDING'
    END,
    NULL,  -- rejection_reason (NULL for approved/pending)
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM affiliates a
WHERE a.full_name IN ('Carlos Rodriguez', 'Maria Gonzalez', 'Luis Martinez', 'Ana Jimenez')
  AND NOT EXISTS (
    SELECT 1 FROM credit_applications ca WHERE ca.affiliate_id = a.id
  );
