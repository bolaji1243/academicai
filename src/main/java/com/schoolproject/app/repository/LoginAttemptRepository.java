package com.schoolproject.app.repository;

import com.schoolproject.app.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByKey(String key);

    void deleteByKey(String key);

}
