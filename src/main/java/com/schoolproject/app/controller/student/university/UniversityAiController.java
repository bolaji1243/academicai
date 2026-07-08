package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.request.AiAskRequest;
import com.schoolproject.app.dto.request.AiAssignmentRequest;
import com.schoolproject.app.dto.request.AiMaterialRequest;
import com.schoolproject.app.dto.request.AiPracticeQuestionsRequest;
import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.service.student.university.UniversityAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/university/ai")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityAiController {

    private final UniversityAiService aiService;

    @PostMapping("/summarise")
    public ApiResponse<String> summarise(@Valid @RequestBody AiMaterialRequest request) {
        return ApiResponse.success("Material summarised successfully", aiService.summarise(request.getMaterialId()));
    }

    @PostMapping("/ask")
    public ApiResponse<String> ask(@Valid @RequestBody AiAskRequest request) {
        return ApiResponse.success("Question answered successfully", aiService.ask(request.getMaterialId(), request.getQuestion()));
    }

    @PostMapping("/practice-questions")
    public ApiResponse<String> practiceQuestions(@Valid @RequestBody AiPracticeQuestionsRequest request) {
        return ApiResponse.success("Practice questions generated successfully",
                aiService.practiceQuestions(request.getMaterialId(), request.getCount()));
    }

    @PostMapping("/explain-assignment")
    public ApiResponse<String> explainAssignment(@Valid @RequestBody AiAssignmentRequest request) {
        return ApiResponse.success("Assignment explained successfully", aiService.explainAssignment(request.getAssignmentId()));
    }

    @PostMapping("/study-planner")
    public ApiResponse<String> studyPlanner() {
        return ApiResponse.success("Study planner generated successfully", aiService.studyPlanner());
    }
}
