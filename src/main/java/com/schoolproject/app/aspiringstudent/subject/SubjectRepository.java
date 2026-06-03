package com.schoolproject.app.aspiringstudent.subject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findBySlug(String slug);

    Optional<Subject> findByPublicId(String publicId);

    List<Subject> findByPublicIdIn(Collection<String> publicIds);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndPublicIdNot(String slug, String publicId);
}
