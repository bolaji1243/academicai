package com.schoolproject.app.aspiringstudent.analytics;

import com.schoolproject.app.aspiringstudent.analytics.dto.OverviewAnalyticsResponse;
import com.schoolproject.app.aspiringstudent.analytics.dto.SubjectAnalyticsResponse;
import com.schoolproject.app.aspiringstudent.analytics.dto.TopicAnalyticsResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class StudentAnalyticsService {

    private final StudentAnalyticsRepository studentAnalyticsRepository;
    private final UserRepository userRepository;

    public StudentAnalyticsService(
            StudentAnalyticsRepository studentAnalyticsRepository,
            UserRepository userRepository
    ) {
        this.studentAnalyticsRepository = studentAnalyticsRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public OverviewAnalyticsResponse getOverview(String email) {
        User user = findUserByEmail(email);
        StudentAnalyticsRepository.OverviewAnalyticsProjection overview =
                studentAnalyticsRepository.getOverview(user);

        String mostPracticedSubject = studentAnalyticsRepository
                .findMostPracticedSubjectNames(user, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);

        return new OverviewAnalyticsResponse(
                overview.getTotalSessions(),
                roundPercentage(overview.getOverallAverageScore()),
                mostPracticedSubject,
                overview.getMostRecentSessionDate()
        );
    }

    @Transactional(readOnly = true)
    public List<SubjectAnalyticsResponse> getSubjectAnalytics(String email) {
        User user = findUserByEmail(email);

        return studentAnalyticsRepository.getSubjectAnalytics(user)
                .stream()
                .map(subject -> new SubjectAnalyticsResponse(
                        subject.getSubjectId(),
                        subject.getSubjectName(),
                        subject.getTotalSessions(),
                        roundPercentage(subject.getAverageScore()),
                        roundScore(subject.getBestScore()),
                        roundScore(subject.getWorstScore()),
                        subject.getTotalQuestionsAttempted(),
                        subject.getMostRecentSessionDate()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicAnalyticsResponse> getTopicAnalytics(String email) {
        User user = findUserByEmail(email);
        List<StudentAnalyticsRepository.TopicAnalyticsProjection> topics =
                studentAnalyticsRepository.getTopicAnalytics(user);

        return IntStream.range(0, topics.size())
                .mapToObj(index -> {
                    StudentAnalyticsRepository.TopicAnalyticsProjection topic = topics.get(index);

                    return new TopicAnalyticsResponse(
                            index + 1,
                            topic.getTopicId(),
                            topic.getTopicName(),
                            topic.getSubjectId(),
                            topic.getSubjectName(),
                            topic.getTotalSessions(),
                            roundPercentage(topic.getAverageScore()),
                            topic.getTotalQuestionsAttempted()
                    );
                })
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private double roundPercentage(Double value) {
        if (value == null) {
            return 0.0;
        }

        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int roundScore(Double value) {
        if (value == null) {
            return 0;
        }

        return BigDecimal.valueOf(value)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}
