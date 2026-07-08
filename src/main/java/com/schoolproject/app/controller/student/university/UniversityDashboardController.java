package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.dto.response.UniversityDashboardResponse;
import com.schoolproject.app.service.student.university.UniversityDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/university/dashboard")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityDashboardController {

    private final UniversityDashboardService dashboardService;

    @GetMapping
    public ApiResponse<UniversityDashboardResponse> getDashboard() {
        return ApiResponse.success("Dashboard retrieved successfully", dashboardService.getDashboard());
    }
}
