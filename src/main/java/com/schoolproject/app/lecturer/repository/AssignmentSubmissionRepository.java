package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    Page<AssignmentSubmission> findByAssignment(Assignment assignment, Pageable pageable);

    Optional<AssignmentSubmission> findByAssignmentAndStudent(Assignment assignment, User student);

    List<AssignmentSubmission> findByAssignmentInAndStudent(List<Assignment> assignments, User student);

    long countByAssignmentAndStatus(Assignment assignment, SubmissionStatus status);

    @Query("SELECT COUNT(asb) FROM AssignmentSubmission asb WHERE asb.assignment.course IN :courses AND asb.status = :status")
    long countByCourseInAndStatus(@Param("courses") List<Course> courses, @Param("status") SubmissionStatus status);

    @Query("SELECT COUNT(asb) FROM AssignmentSubmission asb WHERE asb.assignment.course = :course AND asb.score IS NULL")
    long countUngradedByCourse(@Param("course") Course course);

    @Query("SELECT COUNT(asb) FROM AssignmentSubmission asb WHERE asb.assignment.course IN :courses AND asb.score IS NULL")
    long countUngradedByCourses(@Param("courses") List<Course> courses);

    @Query("SELECT MAX(asb.submittedAt) FROM AssignmentSubmission asb WHERE asb.assignment.course = :course")
    Optional<LocalDateTime> findLatestSubmissionAtByCourse(@Param("course") Course course);

    @Query("SELECT asb.assignment.course.id, COUNT(asb) FROM AssignmentSubmission asb WHERE asb.assignment.course IN :courses AND asb.score IS NULL GROUP BY asb.assignment.course.id")
    List<Object[]> countUngradedByCourseGrouped(@Param("courses") List<Course> courses);
}
