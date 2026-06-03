package com.schoolproject.app.repository;

import com.schoolproject.app.entity.UniversityStudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityStudentProfileRepository extends JpaRepository<UniversityStudentProfile, Long> {
    boolean existsByMatricNumber(String matricNumber);
}
