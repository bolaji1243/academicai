package com.schoolproject.app.aspiringstudent.question;

import com.schoolproject.app.aspiringstudent.entity.ExamType;
import com.schoolproject.app.aspiringstudent.subject.Subject;
import com.schoolproject.app.aspiringstudent.topic.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "past_questions")
public class PastQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, length = 36, updatable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", nullable = false)
    private ExamType examType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "exam_year")
    private Integer examYear;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @Column(name = "option_a", nullable = false, length = 1000)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 1000)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 1000)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 1000)
    private String optionD;

    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Column(length = 2000)
    private String explanation;

    @PrePersist
    public void ensurePublicId() {
        if (publicId == null || publicId.isBlank()) {
            publicId = UUID.randomUUID().toString();
        }
    }
}
