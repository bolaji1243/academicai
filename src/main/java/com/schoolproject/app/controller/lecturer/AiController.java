package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.request.GenerateAnnouncementRequest;
import com.schoolproject.app.lecturer.dto.request.GenerateQuestionsRequest;
import com.schoolproject.app.lecturer.dto.response.AiQuestionsResponse;
import com.schoolproject.app.lecturer.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lecturer/ai")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - AI")
public class AiController {

    private final AiService aiService;

    @PostMapping("/announcements/generate")
    @Operation(summary = "Generate announcement from rough notes using AI")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateAnnouncement(
            @RequestBody @Valid GenerateAnnouncementRequest request) {
        String generatedText = aiService.generateAnnouncement(request.getRoughNote());
        Map<String, String> data = Map.of("generatedText", generatedText);
        return ResponseEntity.ok(ApiResponse.success("Announcement generated successfully", data));
    }

    @PostMapping("/assignments/generate-questions")
    @Operation(summary = "Generate assignment questions using AI")
    public ResponseEntity<ApiResponse<AiQuestionsResponse>> generateQuestions(
            @RequestBody @Valid GenerateQuestionsRequest request) {
        AiQuestionsResponse data = aiService.generateAssignmentQuestions(
                request.getTopic(), request.getLevel(), request.getCount());
        return ResponseEntity.ok(ApiResponse.success("Questions generated successfully", data));
    }
}
