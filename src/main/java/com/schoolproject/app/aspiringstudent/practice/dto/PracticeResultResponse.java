package com.schoolproject.app.aspiringstudent.practice.dto;

import com.schoolproject.app.aspiringstudent.practice.entity.PracticeSession;

import java.time.LocalDateTime;
import java.util.List;

public record PracticeResultResponse(
        String sessionId,
        String subjectName,
        String topicName,
        int score,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        LocalDateTime createdAt,
        List<AnswerDetail> answers
) {
    public record AnswerDetail(
            String questionId,
            String questionText,
            String selectedOption,
            String correctOption,
            boolean correct,
            String explanation
    ) {}

    public static PracticeResultResponse from(PracticeSession session, List<AnswerDetail> answerDetails) {
        int correctCount = session.getScore();
        int wrongCount = session.getTotalQuestions() - correctCount;

        return new PracticeResultResponse(
                session.getPublicId(),
                session.getSubject().getName(),
                session.getTopic().getName(),
                session.getScore(),
                session.getTotalQuestions(),
                correctCount,
                wrongCount,
                session.getCreatedAt(),
                answerDetails
        );
    }
}
