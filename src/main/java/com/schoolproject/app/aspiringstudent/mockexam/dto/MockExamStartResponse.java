package com.schoolproject.app.aspiringstudent.mockexam.dto;

import com.schoolproject.app.aspiringstudent.mockexam.MockExamStatus;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSession;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSessionQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Data
@AllArgsConstructor
public class MockExamStartResponse {

    private String sessionId;
    private String examType;
    private MockExamStatus status;
    private LocalDateTime startTime;
    private int durationMinutes;
    private int totalQuestions;
    private List<QuestionView> questions;

    @Data
    @AllArgsConstructor
    public static class QuestionView {
        private int order;
        private String questionId;
        private String subjectName;
        private String topicName;
        private Integer examYear;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
    }

    public static MockExamStartResponse from(MockExamSession session) {
        List<QuestionView> questions = session.getQuestions().stream()
                .sorted(Comparator.comparingInt(MockExamSessionQuestion::getQuestionOrder))
                .map(question -> new QuestionView(
                        question.getQuestionOrder(),
                        question.getQuestion().getPublicId(),
                        question.getQuestion().getSubject().getName(),
                        question.getQuestion().getTopic().getName(),
                        question.getQuestion().getExamYear(),
                        question.getQuestion().getQuestionText(),
                        question.getQuestion().getOptionA(),
                        question.getQuestion().getOptionB(),
                        question.getQuestion().getOptionC(),
                        question.getQuestion().getOptionD()
                ))
                .toList();

        return new MockExamStartResponse(
                session.getPublicId(),
                session.getExamType().getName(),
                session.getStatus(),
                session.getStartTime(),
                session.getDurationMinutes(),
                session.getTotalQuestions(),
                questions
        );
    }
}
