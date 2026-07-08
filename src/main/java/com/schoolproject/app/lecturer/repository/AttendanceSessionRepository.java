package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.lecturer.entity.AttendanceSession;
import com.schoolproject.app.lecturer.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    Page<AttendanceSession> findByCourse(Course course, Pageable pageable);

    List<AttendanceSession> findByCourseOrderByOpenedAtDesc(Course course);

    Optional<AttendanceSession> findByCourseAndOpenTrue(Course course);

    long countByCourse(Course course);

    @Query("SELECT MAX(s.openedAt) FROM AttendanceSession s WHERE s.course = :course")
    Optional<LocalDateTime> findLatestOpenedAtByCourse(@Param("course") Course course);

    List<AttendanceSession> findByCourseInAndOpenTrue(List<Course> courses);
}
