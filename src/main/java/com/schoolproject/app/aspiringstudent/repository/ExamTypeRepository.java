package com.schoolproject.app.aspiringstudent.repository;

import com.schoolproject.app.aspiringstudent.entity.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamTypeRepository extends JpaRepository<ExamType, Long> {

    Optional<ExamType> findBySlug(String slug);

    Optional<ExamType> findByPublicId(String publicId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndPublicIdNot(String slug, String publicId);
}
