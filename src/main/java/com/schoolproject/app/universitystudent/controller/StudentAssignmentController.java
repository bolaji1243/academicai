package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.dto.response.StudentAssignmentResponse;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.service.StudentAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/university-student")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentAssignmentController {

    private final StudentAssignmentService assignmentService;

    @GetMapping("/courses/{courseId}/assignments")
    public ApiResponse<List<StudentAssignmentResponse>> getAssignments(@PathVariable Long courseId) {
        return ApiResponse.success("Assignments retrieved successfully", assignmentService.getCourseAssignments(courseId));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ApiResponse<StudentAssignmentResponse> getAssignment(@PathVariable Long assignmentId) {
        return ApiResponse.success("Assignment retrieved successfully", assignmentService.getAssignment(assignmentId));
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ApiResponse<StudentAssignmentResponse> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Assignment submitted successfully", assignmentService.submitAssignment(assignmentId, file));
    }

    @GetMapping("/assignments/{assignmentId}/download")
    public ResponseEntity<Void> downloadQuestionFile(@PathVariable Long assignmentId) {
        String cloudinaryUrl = assignmentService.getAssignmentQuestionFileUrl(assignmentId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, cloudinaryUrl)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .build();
    }
}
