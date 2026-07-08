package com.schoolproject.app.aspiringstudent.mockexam.dto;

import com.schoolproject.app.aspiringstudent.mockexam.MockExamStatus;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamAnswer;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSession;
import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSessionQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MockExamResultResponse {

    private String sessionId;
    private String examType;
    private MockExamStatus status;
    private int score;
    private int totalQuestions;
    private double scorePercentage;
    private int durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionResult> questions;
    private List<SubjectBreakdown> subjectBreakdown;

    @Data
    @AllArgsConstructor
    public static class QuestionResult {
        private int order;
        private String questionId;
        private String subjectName;
        private String topicName;
        private String questionText;
        private String selectedOption;
        private String correctOption;
        private boolean correct;
        private String explanation;
    }

    @Data
    @AllArgsConstructor
    public static class SubjectBreakdown {
        private String subjectId;
        private String subjectName;
        private int correctCount;
        private int totalQuestions;
        private double scorePercentage;
    }

    public static MockExamResultResponse from(MockExamSession session) {
        Map<String, MockExamAnswer> answerMap = session.getAnswers().stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getPublicId(),
                        answer -> answer,
                        (left, right) -> left
                ));

        List<QuestionResult> questions = session.getQuestions().stream()
                .sorted(Comparator.comparingInt(MockExamSessionQuestion::getQuestionOrder))
                .map(question -> {
                    MockExamAnswer answer = answerMap.get(question.getQuestion().getPublicId());
                    boolean correct = answer != null && answer.isCorrect();

                    return new QuestionResult(
                            question.getQuestionOrder(),
                            question.getQuestion().getPublicId(),
                            question.getQuestion().getSubject().getName(),
                            question.getQuestion().getTopic().getName(),
                            question.getQuestion().getQuestionText(),
                            answer == null ? null : answer.getSelectedOption(),
                            question.getQuestion().getCorrectOption(),
                            correct,
                            question.getQuestion().getExplanation()
                    );
                })
                .toList();

        List<SubjectBreakdown> breakdown = session.getSubjects().stream()
                .map(subject -> {
                    List<MockExamSessionQuestion> subjectQuestions = session.getQuestions().stream()
                            .filter(question -> question.getQuestion().getSubject().getId().equals(subject.getId()))
                            .toList();

                    long correctCount = subjectQuestions.stream()
                            .map(question -> answerMap.get(question.getQuestion().getPublicId()))
                            .filter(answer -> answer != null && answer.isCorrect())
                            .count();

                    int subjectTotal = subjectQuestions.size();

                    return new SubjectBreakdown(
                            subject.getPublicId(),
                            subject.getName(),
                            (int) correctCount,
                            subjectTotal,
                            subjectTotal == 0 ? 0.0 : round((correctCount * 100.0) / subjectTotal)
                    );
                })
                .sorted(Comparator.comparing(SubjectBreakdown::getSubjectName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new MockExamResultResponse(
                session.getPublicId(),
                session.getExamType().getName(),
                session.getStatus(),
                session.getScore(),
                session.getTotalQuestions(),
                session.getTotalQuestions() == 0 ? 0.0 : round((session.getScore() * 100.0) / session.getTotalQuestions()),
                session.getDurationMinutes(),
                session.getStartTime(),
                session.getEndTime(),
                questions,
                breakdown
        );
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
