ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
    ADD COLUMN google_subject VARCHAR(128) NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_google_subject UNIQUE (google_subject);

ALTER TABLE refresh_tokens
    ADD COLUMN ip_address VARCHAR(64) NULL;

ALTER TABLE refresh_tokens
    ADD COLUMN user_agent VARCHAR(512) NULL;

ALTER TABLE refresh_tokens
    ADD COLUMN auth_provider VARCHAR(30) NULL;

CREATE TABLE IF NOT EXISTS audit_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    event_type VARCHAR(60) NOT NULL,
    email VARCHAR(160) NULL,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    details VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_events_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);
