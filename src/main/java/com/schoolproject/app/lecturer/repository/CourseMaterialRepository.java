package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {

    Page<CourseMaterial> findByCourse(Course course, Pageable pageable);

    Optional<CourseMaterial> findByIdAndCourse(Long id, Course course);

    List<CourseMaterial> findByCourseOrderByWeekTagAscUploadedAtDesc(Course course);

    void deleteByIdAndCourse(Long id, Course course);

    long countByCourse(Course course);

    @Query("SELECT COUNT(cm) FROM CourseMaterial cm WHERE cm.course IN :courses")
    long countByCourses(@Param("courses") List<Course> courses);

    @Query("SELECT MAX(cm.uploadedAt) FROM CourseMaterial cm WHERE cm.course = :course")
    Optional<LocalDateTime> findLatestUploadedAtByCourse(@Param("course") Course course);

    @Query("SELECT cm.course.id, COUNT(cm) FROM CourseMaterial cm WHERE cm.course IN :courses GROUP BY cm.course.id")
    List<Object[]> countByCourseGrouped(@Param("courses") List<Course> courses);
}
