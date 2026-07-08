package com.schoolproject.app.aspiringstudent.analytics.dto;

import java.time.LocalDateTime;

public record OverviewAnalyticsResponse(
        long totalSessions,
        double overallAverageScore,
        String mostPracticedSubject,
        LocalDateTime mostRecentSessionDate
) {
}
