CREATE TABLE IF NOT EXISTS lecturer_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    department VARCHAR(120) NOT NULL,
    faculty VARCHAR(120) NOT NULL,
    staff_id VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_lecturer_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_lecturer_profiles_staff_id UNIQUE (staff_id),
    CONSTRAINT fk_lecturer_profiles_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS university_student_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    matric_number VARCHAR(80) NOT NULL,
    department VARCHAR(120) NOT NULL,
    level VARCHAR(30) NOT NULL,
    faculty VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_university_student_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_university_student_profiles_matric_number UNIQUE (matric_number),
    CONSTRAINT fk_university_student_profiles_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

ALTER TABLE users
    DROP COLUMN university_name,
    DROP COLUMN lecturer_subject;
