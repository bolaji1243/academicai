package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.dto.response.StudentAssignmentResponse;
import com.schoolproject.app.service.student.university.UniversityAssignmentService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/student/university")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityAssignmentController {

    private final UniversityAssignmentService assignmentService;

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
}
