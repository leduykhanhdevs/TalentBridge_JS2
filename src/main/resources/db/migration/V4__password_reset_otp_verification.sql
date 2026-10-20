ALTER TABLE password_reset_tokens
    ADD COLUMN purpose VARCHAR(24) NOT NULL DEFAULT 'LEGACY';

ALTER TABLE password_reset_tokens
    ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0;
