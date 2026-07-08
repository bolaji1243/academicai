package com.schoolproject.app.aspiringstudent.mockexam.dto;

import com.schoolproject.app.aspiringstudent.mockexam.MockExamStatus;
import java.time.LocalDateTime;

public record MockExamHistoryResponse(
        String sessionId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String examType,
        MockExamStatus status,
        int score,
        int totalQuestions,
        long durationTakenMinutes
) {
}
