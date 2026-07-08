CREATE TABLE IF NOT EXISTS student_streaks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_practice_date DATE NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_student_streaks_user_id UNIQUE (user_id),
    CONSTRAINT fk_student_streaks_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);
