package com.schoolproject.app.aspiringstudent.examprofile;

import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentExamProfileRepository extends JpaRepository<StudentExamProfile, Long> {

    Optional<StudentExamProfile> findByUser(User user);

    boolean existsByUser(User user);
}
