CREATE TABLE results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    assessment_type VARCHAR(50) NOT NULL,
    score INT,
    max_score INT,
    grade VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_results_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_results_student FOREIGN KEY (student_id) REFERENCES users(id),
    INDEX idx_results_course_id (course_id),
    INDEX idx_results_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
