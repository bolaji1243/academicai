package com.schoolproject.app.aspiringstudent.dashboard;

import com.schoolproject.app.aspiringstudent.analytics.StudentAnalyticsService;
import com.schoolproject.app.aspiringstudent.analytics.dto.SubjectAnalyticsResponse;
import com.schoolproject.app.aspiringstudent.dashboard.dto.StudentDashboardResponse;
import com.schoolproject.app.aspiringstudent.mockexam.MockExamRepository;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamHistoryResponse;
import com.schoolproject.app.aspiringstudent.practice.dto.PracticeSessionSummary;
import com.schoolproject.app.aspiringstudent.practice.repository.PracticeSessionRepository;
import com.schoolproject.app.aspiringstudent.streak.StudentStreakService;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StudentDashboardService {

    private static final int DEFAULT_RECENT_RESULTS_LIMIT = 5;

    private final UserRepository userRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final MockExamRepository mockExamRepository;
    private final StudentAnalyticsService studentAnalyticsService;
    private final StudentStreakService studentStreakService;

    public StudentDashboardService(
            UserRepository userRepository,
            PracticeSessionRepository practiceSessionRepository,
            MockExamRepository mockExamRepository,
            StudentAnalyticsService studentAnalyticsService,
            StudentStreakService studentStreakService
    ) {
        this.userRepository = userRepository;
        this.practiceSessionRepository = practiceSessionRepository;
        this.mockExamRepository = mockExamRepository;
        this.studentAnalyticsService = studentAnalyticsService;
        this.studentStreakService = studentStreakService;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard(String email) {
        User user = findUser(email);

        List<PracticeSessionSummary> practiceSessions = practiceSessionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(PracticeSessionSummary::from)
                .toList();

        List<MockExamHistoryResponse> mockResults = mockExamRepository.findHistoryByUser(user)
                .stream()
                .map(history -> new MockExamHistoryResponse(
                        history.getSessionId(),
                        history.getStartedAt(),
                        history.getEndedAt(),
                        history.getExamType(),
                        history.getStatus(),
                        history.getScore(),
                        history.getTotalQuestions(),
                        history.getDurationMinutes()
                ))
                .toList();

        List<SubjectAnalyticsResponse> subjectAnalytics = studentAnalyticsService.getSubjectAnalytics(email);

        long totalTestsTaken = practiceSessions.size() + mockResults.size();
        double averageScore = calculateCombinedAverage(practiceSessions, mockResults);
        int currentStreak = studentStreakService.getStreak(email).currentStreak();

        StudentDashboardResponse.SubjectSnapshot weakestSubject = findWeakestSubject(subjectAnalytics);
        StudentDashboardResponse.SubjectSnapshot strongestSubject = findStrongestSubject(subjectAnalytics);
        List<StudentDashboardResponse.RecentResultItem> recentResults = buildRecentResults(practiceSessions, mockResults);

        return new StudentDashboardResponse(
                totalTestsTaken,
                averageScore,
                currentStreak,
                weakestSubject,
                strongestSubject,
                recentResults
        );
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private double calculateCombinedAverage(
            List<PracticeSessionSummary> practiceSessions,
            List<MockExamHistoryResponse> mockResults
    ) {
        long totalQuestions = 0;
        long totalScore = 0;

        for (PracticeSessionSummary session : practiceSessions) {
            totalQuestions += session.totalQuestions();
            totalScore += session.score();
        }

        for (MockExamHistoryResponse session : mockResults) {
            totalQuestions += session.totalQuestions();
            totalScore += session.score();
        }

        if (totalQuestions == 0) {
            return 0.0;
        }

        return roundToTwoDecimals((totalScore * 100.0) / totalQuestions);
    }

    private StudentDashboardResponse.SubjectSnapshot findWeakestSubject(List<SubjectAnalyticsResponse> subjects) {
        return subjects.stream()
                .min(subjectComparator())
                .map(subject -> new StudentDashboardResponse.SubjectSnapshot(
                        subject.subjectId(),
                        subject.subjectName(),
                        subject.averageScore(),
                        subject.totalSessions()
                ))
                .orElse(null);
    }

    private StudentDashboardResponse.SubjectSnapshot findStrongestSubject(List<SubjectAnalyticsResponse> subjects) {
        return subjects.stream()
                .max(subjectComparator())
                .map(subject -> new StudentDashboardResponse.SubjectSnapshot(
                        subject.subjectId(),
                        subject.subjectName(),
                        subject.averageScore(),
                        subject.totalSessions()
                ))
                .orElse(null);
    }

    private Comparator<SubjectAnalyticsResponse> subjectComparator() {
        return Comparator.comparingDouble(SubjectAnalyticsResponse::averageScore)
                .thenComparing(SubjectAnalyticsResponse::subjectName, String.CASE_INSENSITIVE_ORDER);
    }

    private List<StudentDashboardResponse.RecentResultItem> buildRecentResults(
            List<PracticeSessionSummary> practiceSessions,
            List<MockExamHistoryResponse> mockResults
    ) {
        List<StudentDashboardResponse.RecentResultItem> items = new ArrayList<>();

        for (PracticeSessionSummary session : practiceSessions) {
            items.add(new StudentDashboardResponse.RecentResultItem(
                    StudentDashboardResponse.ResultSource.PRACTICE,
                    session.sessionId(),
                    session.examType() + " - " + session.subjectName() + " / " + session.topicName(),
                    session.score(),
                    session.totalQuestions(),
                    calculateSessionPercentage(session.score(), session.totalQuestions()),
                    session.createdAt()
            ));
        }

        for (MockExamHistoryResponse session : mockResults) {
            LocalDateTime occurredAt = session.endedAt() != null ? session.endedAt() : session.startedAt();
            items.add(new StudentDashboardResponse.RecentResultItem(
                    StudentDashboardResponse.ResultSource.MOCK_EXAM,
                    session.sessionId(),
                    session.examType(),
                    session.score(),
                    session.totalQuestions(),
                    calculateSessionPercentage(session.score(), session.totalQuestions()),
                    occurredAt
            ));
        }

        return items.stream()
                .sorted(Comparator.comparing(StudentDashboardResponse.RecentResultItem::occurredAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(DEFAULT_RECENT_RESULTS_LIMIT)
                .toList();
    }

    private double calculateSessionPercentage(int score, int totalQuestions) {
        if (totalQuestions <= 0) {
            return 0.0;
        }

        return roundToTwoDecimals((score * 100.0) / totalQuestions);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
