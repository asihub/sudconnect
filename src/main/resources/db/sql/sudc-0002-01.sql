--liquibase formatted sql
--changeset sudconnect:sudc-0002-01

CREATE TABLE audit_logs (
    id VARCHAR(36) NOT NULL,
    patient_id_hash VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    recipient_organization_id VARCHAR(255),
    data_categories VARCHAR(1000),
    consent_record_id VARCHAR(36),
    performed_at TIMESTAMP NOT NULL,
    details VARCHAR(2000),
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_audit_logs_patient ON audit_logs(patient_id_hash);
CREATE INDEX idx_audit_logs_performed_at ON audit_logs(performed_at);

--rollback DROP TABLE audit_logs;