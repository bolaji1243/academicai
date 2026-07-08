package com.schoolproject.app.aspiringstudent.streak;

import com.schoolproject.app.aspiringstudent.streak.dto.StreakResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class StudentStreakService {

    private final StudentStreakRepository studentStreakRepository;
    private final UserRepository userRepository;

    public StudentStreakService(StudentStreakRepository studentStreakRepository, UserRepository userRepository) {
        this.studentStreakRepository = studentStreakRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StreakResponse recordActivity(String email, LocalDateTime activityAt) {
        User user = findUser(email);
        LocalDate activityDate = activityAt.toLocalDate();
        StudentStreak streak = studentStreakRepository.findByUser(user)
                .orElseGet(StudentStreak::new);

        streak.setUser(user);

        LocalDate lastPracticeDate = streak.getLastPracticeDate();
        if (lastPracticeDate == null) {
            streak.setCurrentStreak(1);
            streak.setLongestStreak(Math.max(streak.getLongestStreak(), 1));
            streak.setLastPracticeDate(activityDate);
            return toResponse(studentStreakRepository.save(streak));
        }

        if (activityDate.isBefore(lastPracticeDate)) {
            return toResponse(streak);
        }

        if (activityDate.equals(lastPracticeDate)) {
            return toResponse(streak);
        }

        if (activityDate.equals(lastPracticeDate.plusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        streak.setLongestStreak(Math.max(streak.getLongestStreak(), streak.getCurrentStreak()));
        streak.setLastPracticeDate(activityDate);

        return toResponse(studentStreakRepository.save(streak));
    }

    @Transactional(readOnly = true)
    public StreakResponse getStreak(String email) {
        User user = findUser(email);

        return studentStreakRepository.findByUser(user)
                .map(this::toResponse)
                .orElseGet(() -> new StreakResponse(0, 0, null));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private StreakResponse toResponse(StudentStreak streak) {
        return new StreakResponse(
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastPracticeDate()
        );
    }
}
