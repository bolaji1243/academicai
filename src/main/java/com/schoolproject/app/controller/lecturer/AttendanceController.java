package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.response.AttendanceRecordResponse;
import com.schoolproject.app.lecturer.dto.response.AttendanceSessionResponse;
import com.schoolproject.app.lecturer.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lecturer")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/courses/{courseId}/attendance/start")
    @Operation(summary = "Start an attendance session")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> startSession(
            @PathVariable Long courseId) {
        AttendanceSessionResponse data = attendanceService.startSession(courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance session started", data));
    }

    @PostMapping("/courses/{courseId}/attendance/stop")
    @Operation(summary = "Stop the currently open attendance session for a course")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> stopSession(
            @PathVariable Long courseId) {
        AttendanceSessionResponse data = attendanceService.stopSessionByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Attendance session stopped", data));
    }

    @PostMapping("/attendance/{sessionId}/close")
    @Operation(summary = "Close an attendance session by session ID")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> closeSession(
            @PathVariable Long sessionId) {
        AttendanceSessionResponse data = attendanceService.closeSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Attendance session closed", data));
    }

    @PostMapping("/attendance/{sessionId}/reopen")
    @Operation(summary = "Reopen an attendance session")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> reopenSession(
            @PathVariable Long sessionId) {
        AttendanceSessionResponse data = attendanceService.reopenSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Attendance session reopened", data));
    }

    @GetMapping("/attendance/{sessionId}/records")
    @Operation(summary = "Get attendance records for a session")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordResponse>>> getSessionRecords(
            @PathVariable Long sessionId,
            @PageableDefault(size = 10, sort = "markedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<AttendanceRecordResponse> data = attendanceService.getSessionRecords(sessionId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", data));
    }

    @PatchMapping("/attendance/{sessionId}/students/{studentId}/add")
    @Operation(summary = "Manually add a student to attendance")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> manuallyAddStudent(
            @PathVariable Long sessionId,
            @PathVariable Long studentId) {
        AttendanceRecordResponse data = attendanceService.manuallyAddStudent(sessionId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Student added to attendance", data));
    }

    @PatchMapping("/attendance/{sessionId}/students/{studentId}/remove")
    @Operation(summary = "Manually remove a student from attendance")
    public ResponseEntity<ApiResponse<Void>> manuallyRemoveStudent(
            @PathVariable Long sessionId,
            @PathVariable Long studentId) {
        attendanceService.manuallyRemoveStudent(sessionId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Student removed from attendance", null));
    }

    @GetMapping("/courses/{courseId}/attendance/history")
    @Operation(summary = "Get attendance history for a course")
    public ResponseEntity<ApiResponse<Page<AttendanceSessionResponse>>> getAttendanceHistory(
            @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "openedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<AttendanceSessionResponse> data = attendanceService.getAttendanceHistory(courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved successfully", data));
    }
}
