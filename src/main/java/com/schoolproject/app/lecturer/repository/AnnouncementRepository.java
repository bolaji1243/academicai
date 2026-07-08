package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.lecturer.entity.Announcement;
import com.schoolproject.app.lecturer.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findByCourse(Course course, Pageable pageable);

    List<Announcement> findByCourseOrderByPinnedDescCreatedAtDesc(Course course);

    long countByCourseInAndCreatedAtAfter(List<Course> courses, LocalDateTime createdAt);

    @Query("SELECT a FROM Announcement a WHERE a.course IN :courses ORDER BY a.createdAt DESC")
    List<Announcement> findRecentByCourses(@Param("courses") List<Course> courses, Pageable pageable);

    @Query("SELECT MAX(a.createdAt) FROM Announcement a WHERE a.course = :course")
    Optional<LocalDateTime> findLatestCreatedAtByCourse(@Param("course") Course course);
}
