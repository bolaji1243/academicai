package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.universitystudent.dto.request.AiAskRequest;
import com.schoolproject.app.universitystudent.dto.request.AiAssignmentRequest;
import com.schoolproject.app.universitystudent.dto.request.AiMaterialRequest;
import com.schoolproject.app.universitystudent.dto.request.AiPracticeQuestionsRequest;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.service.StudentAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/university-student/ai")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentAiController {

    private final StudentAiService aiService;

    @PostMapping("/summarise")
    public ApiResponse<String> summarise(@Valid @RequestBody AiMaterialRequest request) {
        log.info("Student AI summarise request for materialId={}", request.getMaterialId());
        return ApiResponse.success("Material summarised successfully", aiService.summarise(request.getMaterialId()));
    }

    @PostMapping("/ask")
    public ApiResponse<String> ask(@Valid @RequestBody AiAskRequest request) {
        log.info("Student AI ask request for materialId={}", request.getMaterialId());
        return ApiResponse.success("Question answered successfully", aiService.ask(request.getMaterialId(), request.getQuestion()));
    }

    @PostMapping("/practice-questions")
    public ApiResponse<String> practiceQuestions(@Valid @RequestBody AiPracticeQuestionsRequest request) {
        log.info("Student AI practice questions request for materialId={}, count={}", request.getMaterialId(), request.getCount());
        return ApiResponse.success("Practice questions generated successfully",
                aiService.practiceQuestions(request.getMaterialId(), request.getCount()));
    }

    @PostMapping("/explain-assignment")
    public ApiResponse<String> explainAssignment(@Valid @RequestBody AiAssignmentRequest request) {
        log.info("Student AI explain assignment request for assignmentId={}", request.getAssignmentId());
        return ApiResponse.success("Assignment explained successfully", aiService.explainAssignment(request.getAssignmentId()));
    }

    @PostMapping("/study-planner")
    public ApiResponse<String> studyPlanner() {
        log.info("Student AI study planner request");
        return ApiResponse.success("Study planner generated successfully", aiService.studyPlanner());
    }
}
