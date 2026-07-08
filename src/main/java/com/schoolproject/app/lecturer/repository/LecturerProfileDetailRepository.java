package com.schoolproject.app.lecturer.repository;

import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.lecturer.entity.LecturerProfileDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LecturerProfileDetailRepository extends JpaRepository<LecturerProfileDetail, Long> {

    Optional<LecturerProfileDetail> findByLecturerProfile(LecturerProfile lecturerProfile);

    boolean existsByLecturerProfile(LecturerProfile lecturerProfile);
}
