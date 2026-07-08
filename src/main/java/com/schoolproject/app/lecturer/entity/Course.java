package com.schoolproject.app.lecturer.entity;

import com.schoolproject.app.entity.LecturerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_join_code", columnList = "join_code"),
        @Index(name = "idx_courses_lecturer_id", columnList = "lecturer_id")
})
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "schedule", length = 100)
    private String schedule;

    @Column(name = "join_code", nullable = false, unique = true, length = 8)
    private String joinCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private LecturerProfile lecturer;

    @Column(name = "is_archived", nullable = false)
    private boolean archived;
}
