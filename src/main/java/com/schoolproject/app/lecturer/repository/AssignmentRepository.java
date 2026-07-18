package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Page<Assignment> findByCourse(Course course, Pageable pageable);

    List<Assignment> findByCourseOrderByDeadlineAsc(Course course);

    List<Assignment> findByCourseInAndDeadlineBetween(List<Course> courses, LocalDateTime start, LocalDateTime end);

    Optional<Assignment> findByIdAndCourse(Long id, Course course);

    long countByCourse(Course course);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.course IN :courses")
    long countByCourses(@Param("courses") List<Course> courses);

    @Query("SELECT MAX(a.createdAt) FROM Assignment a WHERE a.course = :course")
    Optional<LocalDateTime> findLatestCreatedAtByCourse(@Param("course") Course course);

    @Query("SELECT a.course.id, COUNT(a) FROM Assignment a WHERE a.course IN :courses GROUP BY a.course.id")
    List<Object[]> countByCourseGrouped(@Param("courses") List<Course> courses);

    @Query("SELECT a FROM Assignment a JOIN a.course c JOIN CourseEnrollment ce ON ce.course = c WHERE a.id = :assignmentId AND ce.student.id = :studentId")
    Optional<Assignment> findByIdAndEnrolledStudent(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);
}
