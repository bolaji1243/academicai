package com.schoolproject.app.aspiringstudent.mockexam;

import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamHistoryResponse;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamResultResponse;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamStartResponse;
import com.schoolproject.app.aspiringstudent.mockexam.dto.MockExamSubmitRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/mock-exam")
@PreAuthorize("hasAnyRole('ASPIRING_STUDENT', 'UNIVERSITY_STUDENT')")
public class MockExamController {

    private final MockExamService mockExamService;

    public MockExamController(MockExamService mockExamService) {
        this.mockExamService = mockExamService;
    }

    @PostMapping("/start")
    public ResponseEntity<MockExamStartResponse> start(Authentication authentication) {
        return ResponseEntity.ok(mockExamService.startMockExam(authentication.getName()));
    }

    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<MockExamResultResponse> submit(
            Authentication authentication,
            @PathVariable String sessionId,
            @Valid @RequestBody MockExamSubmitRequest request
    ) {
        return ResponseEntity.ok(mockExamService.submitMockExam(authentication.getName(), sessionId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MockExamHistoryResponse>> history(Authentication authentication) {
        return ResponseEntity.ok(mockExamService.getHistory(authentication.getName()));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<MockExamResultResponse> result(
            Authentication authentication,
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(mockExamService.getResult(authentication.getName(), sessionId));
    }
}
