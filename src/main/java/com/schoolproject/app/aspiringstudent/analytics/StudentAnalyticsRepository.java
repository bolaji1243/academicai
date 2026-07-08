package com.schoolproject.app.aspiringstudent.analytics;

import com.schoolproject.app.aspiringstudent.practice.entity.PracticeSession;
import com.schoolproject.app.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StudentAnalyticsRepository extends Repository<PracticeSession, Long> {

    @Query("""
            select count(practiceSession.id) as totalSessions,
                   case
                       when coalesce(sum(practiceSession.totalQuestions), 0) = 0 then 0.0
                       else (sum(practiceSession.score) * 100.0) / sum(practiceSession.totalQuestions)
                   end as overallAverageScore,
                   max(practiceSession.createdAt) as mostRecentSessionDate
            from PracticeSession practiceSession
            where practiceSession.user = :user
            """)
    OverviewAnalyticsProjection getOverview(@Param("user") User user);

    @Query("""
            select practiceSession.subject.name as subjectName
            from PracticeSession practiceSession
            where practiceSession.user = :user
            group by practiceSession.subject.id, practiceSession.subject.name
            order by count(practiceSession.id) desc, max(practiceSession.createdAt) desc, practiceSession.subject.name asc
            """)
    List<String> findMostPracticedSubjectNames(@Param("user") User user, Pageable pageable);

    @Query("""
            select practiceSession.subject.publicId as subjectId,
                   practiceSession.subject.name as subjectName,
                   count(practiceSession.id) as totalSessions,
                   case
                       when coalesce(sum(practiceSession.totalQuestions), 0) = 0 then 0.0
                       else (sum(practiceSession.score) * 100.0) / sum(practiceSession.totalQuestions)
                   end as averageScore,
                   max(case
                       when practiceSession.totalQuestions = 0 then 0.0
                       else (practiceSession.score * 100.0) / practiceSession.totalQuestions
                   end) as bestScore,
                   min(case
                       when practiceSession.totalQuestions = 0 then 0.0
                       else (practiceSession.score * 100.0) / practiceSession.totalQuestions
                   end) as worstScore,
                   coalesce(sum(practiceSession.totalQuestions), 0) as totalQuestionsAttempted,
                   max(practiceSession.createdAt) as mostRecentSessionDate
            from PracticeSession practiceSession
            where practiceSession.user = :user
            group by practiceSession.subject.id, practiceSession.subject.publicId, practiceSession.subject.name
            order by practiceSession.subject.name asc
            """)
    List<SubjectAnalyticsProjection> getSubjectAnalytics(@Param("user") User user);

    @Query("""
            select practiceSession.topic.publicId as topicId,
                   practiceSession.topic.name as topicName,
                   practiceSession.subject.publicId as subjectId,
                   practiceSession.subject.name as subjectName,
                   count(practiceSession.id) as totalSessions,
                   case
                       when coalesce(sum(practiceSession.totalQuestions), 0) = 0 then 0.0
                       else (sum(practiceSession.score) * 100.0) / sum(practiceSession.totalQuestions)
                   end as averageScore,
                   coalesce(sum(practiceSession.totalQuestions), 0) as totalQuestionsAttempted
            from PracticeSession practiceSession
            where practiceSession.user = :user
            group by practiceSession.topic.id, practiceSession.topic.publicId, practiceSession.topic.name,
                     practiceSession.subject.id, practiceSession.subject.publicId, practiceSession.subject.name
            order by case
                         when coalesce(sum(practiceSession.totalQuestions), 0) = 0 then 0.0
                         else (sum(practiceSession.score) * 100.0) / sum(practiceSession.totalQuestions)
                     end asc,
                     count(practiceSession.id) desc,
                     practiceSession.topic.name asc
            """)
    List<TopicAnalyticsProjection> getTopicAnalytics(@Param("user") User user);

    interface OverviewAnalyticsProjection {
        long getTotalSessions();

        Double getOverallAverageScore();

        LocalDateTime getMostRecentSessionDate();
    }

    interface SubjectAnalyticsProjection {
        String getSubjectId();

        String getSubjectName();

        long getTotalSessions();

        Double getAverageScore();

        Double getBestScore();

        Double getWorstScore();

        long getTotalQuestionsAttempted();

        LocalDateTime getMostRecentSessionDate();
    }

    interface TopicAnalyticsProjection {
        String getTopicId();

        String getTopicName();

        String getSubjectId();

        String getSubjectName();

        long getTotalSessions();

        Double getAverageScore();

        long getTotalQuestionsAttempted();
    }
}
