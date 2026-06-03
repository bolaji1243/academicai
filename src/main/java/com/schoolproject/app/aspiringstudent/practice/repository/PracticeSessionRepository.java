package com.schoolproject.app.aspiringstudent.practice.repository;

import com.schoolproject.app.aspiringstudent.practice.entity.PracticeSession;

import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    Optional<PracticeSession> findByPublicId(String publicId);

    List<PracticeSession> findByUserOrderByCreatedAtDesc(User user);
}