package com.schoolproject.app.aspiringstudent.streak.dto;

import java.time.LocalDate;

public record StreakResponse(
        int currentStreak,
        int longestStreak,
        LocalDate lastPracticeDate
) {
}
