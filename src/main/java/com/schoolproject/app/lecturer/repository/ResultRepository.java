package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    Page<Result> findByCourse(Course course, Pageable pageable);

    List<Result> findByCourseAndStudent(Course course, User student);

    List<Result> findByCourseAndStudentOrderByAssessmentTypeAsc(Course course, User student);

    Optional<Result> findByIdAndCourse(Long id, Course course);

    @Query("SELECT AVG(r.score) FROM Result r WHERE r.course = :course AND r.score IS NOT NULL")
    Optional<Double> findAverageScoreByCourse(@Param("course") Course course);
}
