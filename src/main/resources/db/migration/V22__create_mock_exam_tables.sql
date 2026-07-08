CREATE TABLE IF NOT EXISTS mock_exam_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    exam_type_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    duration_minutes INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_mock_exam_sessions_public_id UNIQUE (public_id),
    CONSTRAINT fk_mock_exam_sessions_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_mock_exam_sessions_exam_type_id FOREIGN KEY (exam_type_id) REFERENCES exam_types (id)
);

CREATE TABLE IF NOT EXISTS mock_exam_session_subjects (
    mock_exam_session_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (mock_exam_session_id, subject_id),
    CONSTRAINT fk_mock_exam_session_subjects_session_id FOREIGN KEY (mock_exam_session_id) REFERENCES mock_exam_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_mock_exam_session_subjects_subject_id FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

CREATE TABLE IF NOT EXISTS mock_exam_session_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mock_exam_session_questions_session_id FOREIGN KEY (session_id) REFERENCES mock_exam_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_mock_exam_session_questions_question_id FOREIGN KEY (question_id) REFERENCES past_questions (id)
);

CREATE TABLE IF NOT EXISTS mock_exam_answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option VARCHAR(1) NOT NULL,
    correct BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mock_exam_answers_session_id FOREIGN KEY (session_id) REFERENCES mock_exam_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_mock_exam_answers_question_id FOREIGN KEY (question_id) REFERENCES past_questions (id)
);
