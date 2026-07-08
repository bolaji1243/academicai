package com.schoolproject.app.aspiringstudent.analytics.dto;

import java.time.LocalDateTime;

public record SubjectAnalyticsResponse(
        String subjectId,
        String subjectName,
        long totalSessions,
        double averageScore,
        int bestScore,
        int worstScore,
        long totalQuestionsAttempted,
        LocalDateTime mostRecentSessionDate
) {
}
