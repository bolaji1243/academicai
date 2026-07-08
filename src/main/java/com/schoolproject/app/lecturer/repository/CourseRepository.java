package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.lecturer.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findAllByLecturerAndArchivedFalse(LecturerProfile lecturer, Pageable pageable);

    Optional<Course> findByIdAndLecturer(Long id, LecturerProfile lecturer);

    boolean existsByJoinCode(String joinCode);

    Optional<Course> findByJoinCodeAndArchivedFalse(String joinCode);

    long countByLecturer(LecturerProfile lecturer);

    List<Course> findByLecturer(LecturerProfile lecturer);

    List<Course> findByLecturerAndArchivedFalse(LecturerProfile lecturer);
}
