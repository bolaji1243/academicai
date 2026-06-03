CREATE TABLE IF NOT EXISTS topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(36) NOT NULL,
    subject_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_topics_public_id UNIQUE (public_id),
    CONSTRAINT uk_topics_subject_slug UNIQUE (subject_id, slug),
    CONSTRAINT fk_topics_subject_id FOREIGN KEY (subject_id) REFERENCES subjects (id)
);
