package com.schoolproject.app.aspiringstudent.recommendations;

import com.schoolproject.app.aspiringstudent.analytics.StudentAnalyticsService;
import com.schoolproject.app.aspiringstudent.analytics.dto.TopicAnalyticsResponse;
import com.schoolproject.app.aspiringstudent.recommendations.dto.RecommendationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationsService {

    private static final String DEFAULT_MESSAGE = "Start practicing to get personalized recommendations.";

    private final StudentAnalyticsService studentAnalyticsService;

    public RecommendationsService(StudentAnalyticsService studentAnalyticsService) {
        this.studentAnalyticsService = studentAnalyticsService;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(String email) {
        List<TopicAnalyticsResponse> topics = studentAnalyticsService.getTopicAnalytics(email);

        if (topics.isEmpty()) {
            return new RecommendationResponse(DEFAULT_MESSAGE, List.of());
        }

        List<RecommendationResponse.RecommendationItem> recommendations = topics.stream()
                .sorted(Comparator.comparingDouble(TopicAnalyticsResponse::averageScore)
                        .thenComparing(TopicAnalyticsResponse::topicName, String.CASE_INSENSITIVE_ORDER))
                .limit(3)
                .map(topic -> new RecommendationResponse.RecommendationItem(
                        topic.rank(),
                        topic.topicName(),
                        topic.subjectName(),
                        topic.averageScore(),
                        "You need more practice in " + topic.topicName()
                ))
                .toList();

        return new RecommendationResponse("Here are your weakest topics.", recommendations);
    }
}
