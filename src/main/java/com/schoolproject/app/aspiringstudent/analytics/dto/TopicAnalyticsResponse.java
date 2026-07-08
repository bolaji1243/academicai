package com.schoolproject.app.aspiringstudent.analytics.dto;

public record TopicAnalyticsResponse(
        int rank,
        String topicId,
        String topicName,
        String subjectId,
        String subjectName,
        long totalSessions,
        double averageScore,
        long totalQuestionsAttempted
) {
}
