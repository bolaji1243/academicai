package com.schoolproject.app.aspiringstudent.streak;

import com.schoolproject.app.aspiringstudent.streak.dto.StreakResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasAnyRole('ASPIRING_STUDENT', 'UNIVERSITY_STUDENT')")
public class StudentStreakController {

    private final StudentStreakService studentStreakService;

    public StudentStreakController(StudentStreakService studentStreakService) {
        this.studentStreakService = studentStreakService;
    }

    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
        return ResponseEntity.ok(studentStreakService.getStreak(authentication.getName()));
    }
}
