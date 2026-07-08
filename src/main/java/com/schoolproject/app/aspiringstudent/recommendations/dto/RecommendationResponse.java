package com.schoolproject.app.aspiringstudent.recommendations.dto;

import java.util.List;

public record RecommendationResponse(
        String message,
        List<RecommendationItem> recommendations
) {
    public record RecommendationItem(
            int rank,
            String topicName,
            String subjectName,
            double averageScore,
            String message
    ) {
    }
}
