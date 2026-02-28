--liquibase formatted sql
--changeset sudconnect:sudc-0001-01

CREATE TABLE consent_records (
    id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(255) NOT NULL,
    recipient_organization_id VARCHAR(255) NOT NULL,
    disclosure_type VARCHAR(50) NOT NULL,
    consent_granted_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    authorized_by VARCHAR(255) NOT NULL,
    purpose_of_disclosure VARCHAR(500),
    permitted_data_categories VARCHAR(1000),
    CONSTRAINT pk_consent_records PRIMARY KEY (id)
);

--rollback DROP TABLE consent_records;