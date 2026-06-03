CREATE TABLE IF NOT EXISTS exam_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_exam_types_slug UNIQUE (slug)
);
