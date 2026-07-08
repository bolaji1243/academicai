package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.AttendanceRecord;
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

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Page<AttendanceRecord> findBySession(AttendanceSession session, Pageable pageable);

    boolean existsBySessionAndStudent(AttendanceSession session, User student);

    Optional<AttendanceRecord> findBySessionAndStudent(AttendanceSession session, User student);

    List<AttendanceRecord> findBySessionCourseAndStudent(Course course, User student);

    long countBySessionCourseAndStudent(Course course, User student);

    long countBySession(AttendanceSession session);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.session.course = :course")
    long countByCourse(@Param("course") Course course);

    @Query("SELECT COUNT(DISTINCT ar.session.id) FROM AttendanceRecord ar WHERE ar.session.course = :course")
    long countDistinctSessionsByCourse(@Param("course") Course course);

    @Query("SELECT MAX(ar.markedAt) FROM AttendanceRecord ar WHERE ar.session.course = :course")
    Optional<LocalDateTime> findLatestMarkedAtByCourse(@Param("course") Course course);
}
