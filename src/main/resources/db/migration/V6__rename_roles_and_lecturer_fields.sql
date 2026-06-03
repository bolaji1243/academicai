UPDATE users
SET role = 'ASPIRING_STUDENT'
WHERE role = 'STUDENT';

UPDATE users
SET role = 'LECTURER'
WHERE role = 'TEACHER';

ALTER TABLE users
    CHANGE COLUMN school_name university_name VARCHAR(120) NULL,
    CHANGE COLUMN teacher_subject lecturer_subject VARCHAR(120) NULL;
