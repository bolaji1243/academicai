package com.schoolproject.app.aspiringstudent.practice.dto;


import com.schoolproject.app.aspiringstudent.practice.entity.PracticeSession;

import java.time.LocalDateTime;

public record PracticeSessionSummary(
        String sessionId,
        String examType,
        String subjectName,
        String topicName,
        int score,
        int totalQuestions,
        LocalDateTime createdAt
) {

    public static PracticeSessionSummary from(PracticeSession session) {
        return new PracticeSessionSummary(
                session.getPublicId(),
                session.getExamType().getName(),
                session.getSubject().getName(),
                session.getTopic().getName(),
                session.getScore(),
                session.getTotalQuestions(),
                session.getCreatedAt()
        );
    }
}