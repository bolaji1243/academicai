package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.dto.response.AttendanceHistoryResponse;
import com.schoolproject.app.dto.response.AttendanceRecordResponse;
import com.schoolproject.app.service.student.university.UniversityAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/university")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityAttendanceController {

    private final UniversityAttendanceService attendanceService;

    @PostMapping("/attendance/{sessionId}/mark")
    public ApiResponse<AttendanceRecordResponse> markAttendance(@PathVariable Long sessionId) {
        return ApiResponse.success("Attendance marked successfully", attendanceService.markAttendance(sessionId));
    }

    @GetMapping("/courses/{courseId}/attendance")
    public ApiResponse<AttendanceHistoryResponse> getAttendance(@PathVariable Long courseId) {
        return ApiResponse.success("Attendance retrieved successfully", attendanceService.getAttendance(courseId));
    }
}
