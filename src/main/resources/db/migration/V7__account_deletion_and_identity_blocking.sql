CREATE TABLE blocked_account_identifiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier_type VARCHAR(16) NOT NULL,
    identifier_hash VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_blocked_account_identifier UNIQUE (identifier_type, identifier_hash)
);

ALTER TABLE job_status_history
    DROP FOREIGN KEY fk_job_status_history_user;

ALTER TABLE job_status_history
    MODIFY COLUMN changed_by_user_id BIGINT NULL;

ALTER TABLE job_status_history
    ADD CONSTRAINT fk_job_status_history_user
        FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
