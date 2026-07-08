package com.schoolproject.app.aspiringstudent.mockexam;

import com.schoolproject.app.aspiringstudent.mockexam.entity.MockExamSession;
import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MockExamRepository extends JpaRepository<MockExamSession, Long> {

    Optional<MockExamSession> findByPublicIdAndUser_Email(String publicId, String email);

    @Query("""
            select session.publicId as sessionId,
                   session.startTime as startedAt,
                   session.endTime as endedAt,
                   session.examType.name as examType,
                   session.status as status,
                   session.score as score,
                   session.totalQuestions as totalQuestions,
                   session.durationMinutes as durationMinutes
            from MockExamSession session
            where session.user = :user
              and session.status <> com.schoolproject.app.aspiringstudent.mockexam.MockExamStatus.IN_PROGRESS
            order by session.startTime desc
            """)
    List<MockExamHistoryProjection> findHistoryByUser(@Param("user") User user);

    interface MockExamHistoryProjection {
        String getSessionId();

        LocalDateTime getStartedAt();

        LocalDateTime getEndedAt();

        String getExamType();

        MockExamStatus getStatus();

        int getScore();

        int getTotalQuestions();

        int getDurationMinutes();
    }
}
