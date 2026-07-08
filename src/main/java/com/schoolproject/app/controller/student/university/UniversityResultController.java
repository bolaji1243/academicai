package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.dto.response.StudentResultsSummaryResponse;
import com.schoolproject.app.service.student.university.UniversityResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/university")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityResultController {

    private final UniversityResultService resultService;

    @GetMapping("/courses/{courseId}/results")
    public ApiResponse<StudentResultsSummaryResponse> getResults(@PathVariable Long courseId) {
        return ApiResponse.success("Results retrieved successfully", resultService.getResults(courseId));
    }
}
