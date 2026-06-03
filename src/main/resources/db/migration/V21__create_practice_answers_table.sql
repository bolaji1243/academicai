CREATE TABLE IF NOT EXISTS practice_answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option VARCHAR(1) NOT NULL,
    correct BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_practice_answers_session_id FOREIGN KEY (session_id) REFERENCES practice_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_practice_answers_question_id FOREIGN KEY (question_id) REFERENCES past_questions (id)
);
