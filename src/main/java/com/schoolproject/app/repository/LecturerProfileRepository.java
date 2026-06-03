package com.schoolproject.app.repository;

import com.schoolproject.app.entity.LecturerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Long> {
    boolean existsByStaffId(String staffId);
}
