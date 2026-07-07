package com.schoolproject.app.repository;

import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityStudentProfileRepository extends JpaRepository<UniversityStudentProfile, Long> {
    Optional<UniversityStudentProfile> findByUser(User user);

    boolean existsByMatricNumber(String matricNumber);

    boolean existsByMatricNumberAndUserNot(String matricNumber, User user);
}
