package com.schoolproject.app.aspiringstudent.practice.controller;
import com.schoolproject.app.aspiringstudent.practice.dto.PracticeResultResponse;
import com.schoolproject.app.aspiringstudent.practice.dto.PracticeSessionSummary;
import com.schoolproject.app.aspiringstudent.practice.dto.SubmitPracticeRequest;
import com.schoolproject.app.aspiringstudent.practice.service.PracticeSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/practice-sessions")
public class PracticeSessionController {

    private final PracticeSessionService practiceSessionService;

    public PracticeSessionController(PracticeSessionService practiceSessionService) {
        this.practiceSessionService = practiceSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PracticeResultResponse submitSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubmitPracticeRequest request
    ) {
        return practiceSessionService.submitSession(userDetails.getUsername(), request);
    }

    @GetMapping
    public List<PracticeSessionSummary> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return practiceSessionService.getSessionsForUser(userDetails.getUsername());
    }

    @GetMapping("/{sessionId}")
    public PracticeResultResponse getSessionDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sessionId
    ) {
        return practiceSessionService.getSessionDetail(userDetails.getUsername(), sessionId);
    }
}