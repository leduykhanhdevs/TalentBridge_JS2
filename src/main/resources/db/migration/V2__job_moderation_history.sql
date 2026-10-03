CREATE TABLE job_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    from_status VARCHAR(20) NULL,
    to_status VARCHAR(20) NOT NULL,
    reason TEXT NULL,
    changed_by_user_id BIGINT NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_job_status_history_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_job_status_history_job_changed_at ON job_status_history(job_id, changed_at);
