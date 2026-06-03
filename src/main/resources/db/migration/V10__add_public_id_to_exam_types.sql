ALTER TABLE exam_types
    ADD COLUMN public_id VARCHAR(36) NULL;

UPDATE exam_types
SET public_id = UUID()
WHERE public_id IS NULL;

ALTER TABLE exam_types
    MODIFY public_id VARCHAR(36) NOT NULL;

ALTER TABLE exam_types
    ADD CONSTRAINT uk_exam_types_public_id UNIQUE (public_id);
