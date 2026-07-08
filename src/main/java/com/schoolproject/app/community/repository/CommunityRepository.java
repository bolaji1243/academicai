package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByCourseId(Long courseId);
}
