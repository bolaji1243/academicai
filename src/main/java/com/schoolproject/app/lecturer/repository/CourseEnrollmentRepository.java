package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Page<CourseEnrollment> findByCourse(Course course, Pageable pageable);

    List<CourseEnrollment> findByCourse(Course course);

    Optional<CourseEnrollment> findByCourseAndStudent(Course course, User student);

    boolean existsByCourseAndStudent(Course course, User student);

    List<CourseEnrollment> findByStudent(User student);

    Optional<CourseEnrollment> findByStudentAndCourseId(User student, Long courseId);

    long countByCourse(Course course);

    long countByStudent(User student);

    @Query("SELECT COUNT(DISTINCT ce.student.id) FROM CourseEnrollment ce WHERE ce.course.lecturer = :lecturer")
    long countDistinctStudentsByLecturer(@Param("lecturer") LecturerProfile lecturer);

    @Query("SELECT ce.course.id, COUNT(ce) FROM CourseEnrollment ce WHERE ce.course IN :courses GROUP BY ce.course.id")
    List<Object[]> countByCourseGrouped(@Param("courses") List<Course> courses);

    void deleteByCourseAndStudent(Course course, User student);
}
