package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.dto.response.AttendanceHistoryResponse;
import com.schoolproject.app.dto.response.AttendanceRecordResponse;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.dto.response.OpenAttendanceSessionResponse;
import com.schoolproject.app.universitystudent.service.StudentAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/university-student")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final StudentAttendanceService attendanceService;

    @PostMapping("/attendance/{sessionId}/mark")
    public ApiResponse<AttendanceRecordResponse> markAttendance(@PathVariable Long sessionId) {
        return ApiResponse.success("Attendance marked successfully", attendanceService.markAttendance(sessionId));
    }

    @GetMapping("/courses/{courseId}/attendance")
    public ApiResponse<AttendanceHistoryResponse> getAttendance(@PathVariable Long courseId) {
        return ApiResponse.success("Attendance retrieved successfully", attendanceService.getAttendance(courseId));
    }

    @GetMapping("/attendance/open")
    public ApiResponse<List<OpenAttendanceSessionResponse>> getOpenSessions() {
        return ApiResponse.success("Open sessions retrieved successfully", attendanceService.getOpenSessions());
    }
}
