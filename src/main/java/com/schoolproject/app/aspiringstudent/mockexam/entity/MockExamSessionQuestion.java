package com.schoolproject.app.aspiringstudent.mockexam.entity;

import com.schoolproject.app.aspiringstudent.question.PastQuestion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mock_exam_session_questions")
public class MockExamSessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MockExamSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private PastQuestion question;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;
}
