package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.request.CreateResultRequest;
import com.schoolproject.app.lecturer.dto.response.ResultResponse;
import com.schoolproject.app.lecturer.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lecturer/courses")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Results")
public class ResultController {

    private final ResultService resultService;

    @PostMapping("/{courseId}/results")
    @Operation(summary = "Record a result")
    public ResponseEntity<ApiResponse<ResultResponse>> createResult(
            @PathVariable Long courseId,
            @RequestBody @Valid CreateResultRequest request) {
        ResultResponse data = resultService.createResult(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Result recorded successfully", data));
    }

    @GetMapping("/{courseId}/results")
    @Operation(summary = "Get results for a course")
    public ResponseEntity<ApiResponse<Page<ResultResponse>>> getCourseResults(
            @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ResultResponse> data = resultService.getCourseResults(courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Results retrieved successfully", data));
    }

    @GetMapping("/{courseId}/results/export")
    @Operation(summary = "Export results as CSV")
    public ResponseEntity<byte[]> exportResults(@PathVariable Long courseId) {
        byte[] csv = resultService.exportResultsCsv(courseId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"results_" + courseId + ".csv\"");
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
