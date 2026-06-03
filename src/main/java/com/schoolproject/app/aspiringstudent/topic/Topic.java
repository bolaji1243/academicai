package com.schoolproject.app.aspiringstudent.topic;

import com.schoolproject.app.aspiringstudent.subject.Subject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "topics",
        uniqueConstraints = @UniqueConstraint(name = "uk_topics_subject_slug", columnNames = {"subject_id", "slug"})
)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, length = 36, updatable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column(length = 500)
    private String description;

    @PrePersist
    public void ensurePublicId() {
        if (publicId == null || publicId.isBlank()) {
            publicId = UUID.randomUUID().toString();
        }
    }
}
