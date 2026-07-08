CREATE TABLE profile_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lecturer_profile_id BIGINT NOT NULL,
    title VARCHAR(120),
    bio TEXT,
    profile_visible BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_details_lecturer_profile FOREIGN KEY (lecturer_profile_id) REFERENCES lecturer_profiles(id),
    CONSTRAINT uk_profile_details_lecturer_profile UNIQUE (lecturer_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
