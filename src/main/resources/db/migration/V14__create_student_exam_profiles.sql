CREATE TABLE IF NOT EXISTS student_exam_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    exam_type_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_student_exam_profiles_public_id UNIQUE (public_id),
    CONSTRAINT uk_student_exam_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_student_exam_profiles_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_student_exam_profiles_exam_type_id FOREIGN KEY (exam_type_id) REFERENCES exam_types (id)
);

CREATE TABLE IF NOT EXISTS student_exam_profile_subjects (
    student_exam_profile_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (student_exam_profile_id, subject_id),
    CONSTRAINT fk_student_exam_profile_subjects_profile_id
        FOREIGN KEY (student_exam_profile_id) REFERENCES student_exam_profiles (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_student_exam_profile_subjects_subject_id
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);
