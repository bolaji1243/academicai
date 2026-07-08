ALTER TABLE university_student_profiles
    ADD COLUMN full_name VARCHAR(120) NOT NULL AFTER user_id,
    MODIFY COLUMN level VARCHAR(30) NOT NULL,
    ADD COLUMN semester VARCHAR(30) NOT NULL AFTER level,
    ADD COLUMN session VARCHAR(20) NOT NULL AFTER semester;
