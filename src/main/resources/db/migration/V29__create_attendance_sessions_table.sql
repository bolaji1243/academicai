CREATE TABLE attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    opened_at DATETIME NOT NULL,
    closed_at DATETIME,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    window_minutes INT NOT NULL DEFAULT 2,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_sessions_course FOREIGN KEY (course_id) REFERENCES courses(id),
    INDEX idx_attendance_sessions_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
