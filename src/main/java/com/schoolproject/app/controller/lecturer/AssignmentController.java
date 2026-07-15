package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.request.CreateAssignmentRequest;
import com.schoolproject.app.lecturer.dto.request.GradeSubmissionRequest;
import com.schoolproject.app.lecturer.dto.response.AssignmentResponse;
import com.schoolproject.app.lecturer.dto.response.SubmissionResponse;
import com.schoolproject.app.lecturer.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lecturer")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping(value = "/courses/{courseId}/assignments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create an assignment")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @PathVariable Long courseId,
            @RequestPart("data") @Valid CreateAssignmentRequest request,
            @RequestPart(value = "questionFile", required = false) MultipartFile questionFile) {
        AssignmentResponse data = assignmentService.createAssignment(courseId, request, questionFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created successfully", data));
    }

    @GetMapping("/courses/{courseId}/assignments")
    @Operation(summary = "Get assignments for a course")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAssignments(
            @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<AssignmentResponse> data = assignmentService.getAssignments(courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Assignments retrieved successfully", data));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Get submissions for an assignment")
    public ResponseEntity<ApiResponse<Page<SubmissionResponse>>> getSubmissions(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<SubmissionResponse> data = assignmentService.getSubmissions(assignmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Submissions retrieved successfully", data));
    }

    @PatchMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade a submission")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody @Valid GradeSubmissionRequest request) {
        SubmissionResponse data = assignmentService.gradeSubmission(submissionId, request);
        return ResponseEntity.ok(ApiResponse.success("Submission graded successfully", data));
    }
}
