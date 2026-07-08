package com.schoolproject.app.aspiringstudent.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StudentDashboardResponse(
        long totalTestsTaken,
        double averageScore,
        int currentStreak,
        SubjectSnapshot weakestSubject,
        SubjectSnapshot strongestSubject,
        List<RecentResultItem> recentResults
) {

    public record SubjectSnapshot(
            String subjectId,
            String subjectName,
            double averageScore,
            long totalSessions
    ) {
    }

    public record RecentResultItem(
            ResultSource source,
            String sessionId,
            String title,
            int score,
            int totalQuestions,
            double averageScore,
            LocalDateTime occurredAt
    ) {
    }

    public enum ResultSource {
        PRACTICE,
        MOCK_EXAM
    }
}
