ALTER TABLE users
    ADD COLUMN email_verification_token_hash VARCHAR(64) NULL;

ALTER TABLE users
    ADD COLUMN email_verification_expires_at DATETIME(6) NULL;

ALTER TABLE users
    ADD COLUMN password_reset_token_hash VARCHAR(64) NULL;

ALTER TABLE users
    ADD COLUMN password_reset_expires_at DATETIME(6) NULL;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);
