CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    attempt_key VARCHAR(160) NOT NULL,
    failures INT NOT NULL,
    last_failure_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_login_attempts_attempt_key UNIQUE (attempt_key)
);
