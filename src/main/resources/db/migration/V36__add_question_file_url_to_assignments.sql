ALTER TABLE assignments
    ADD COLUMN question_file_url VARCHAR(500) NULL AFTER max_score;
