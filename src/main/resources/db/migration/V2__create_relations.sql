-- V2__create_relations.sql
-- Creates foreign key constraints and additional indexes for relationships

-- Add foreign key from credit_applications to affiliates
ALTER TABLE credit_applications
    ADD CONSTRAINT fk_credit_affiliate
    FOREIGN KEY (affiliate_id)
    REFERENCES affiliates(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Add foreign key from risk_evaluations to credit_applications
ALTER TABLE risk_evaluations
    ADD CONSTRAINT fk_risk_credit_application
    FOREIGN KEY (credit_application_id)
    REFERENCES credit_applications(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Add composite indexes for common queries
CREATE INDEX idx_credit_affiliate_status ON credit_applications(affiliate_id, status);
CREATE INDEX idx_credit_date_status ON credit_applications(application_date DESC, status);

-- Add index for risk evaluation queries
CREATE INDEX idx_risk_level_score ON risk_evaluations(risk_level, score DESC);

-- Comment removed for H2 compatibility

COMMENT ON CONSTRAINT fk_risk_credit_application ON risk_evaluations IS 'Links risk evaluation to credit application';