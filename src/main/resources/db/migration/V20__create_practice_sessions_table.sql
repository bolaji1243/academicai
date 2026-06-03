CREATE TABLE IF NOT EXISTS practice_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    exam_type_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_practice_sessions_public_id UNIQUE (public_id),
    CONSTRAINT fk_practice_sessions_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_practice_sessions_exam_type_id FOREIGN KEY (exam_type_id) REFERENCES exam_types (id),
    CONSTRAINT fk_practice_sessions_subject_id FOREIGN KEY (subject_id) REFERENCES subjects (id),
    CONSTRAINT fk_practice_sessions_topic_id FOREIGN KEY (topic_id) REFERENCES topics (id)
);
