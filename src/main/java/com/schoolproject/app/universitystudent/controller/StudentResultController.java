package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.dto.response.StudentResultsSummaryResponse;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.service.StudentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/university-student")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentResultController {

    private final StudentResultService resultService;

    @GetMapping("/courses/{courseId}/results")
    public ApiResponse<StudentResultsSummaryResponse> getResults(@PathVariable Long courseId) {
        return ApiResponse.success("Results retrieved successfully", resultService.getResults(courseId));
    }
}
