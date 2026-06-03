package com.schoolproject.app.aspiringstudent.practice.entity;

import com.schoolproject.app.aspiringstudent.question.PastQuestion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "practice_answers")
public class PracticeAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private PracticeSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private PastQuestion question;

    @Column(name = "selected_option", nullable = false, length = 1)
    private String selectedOption;

    @Column(nullable = false)
    private boolean correct;
}