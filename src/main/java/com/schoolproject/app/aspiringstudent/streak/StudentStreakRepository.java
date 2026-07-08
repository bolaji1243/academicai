package com.schoolproject.app.aspiringstudent.streak;

import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentStreakRepository extends JpaRepository<StudentStreak, Long> {

    Optional<StudentStreak> findByUser(User user);
}
