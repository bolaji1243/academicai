package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.request.JoinCourseRequest;
import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.dto.response.UniversityCourseResponse;
import com.schoolproject.app.service.student.university.UniversityCourseEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/university/courses")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityCourseController {

    private final UniversityCourseEnrollmentService enrollmentService;

    @PostMapping("/join")
    public ApiResponse<UniversityCourseResponse> joinCourse(@Valid @RequestBody JoinCourseRequest request) {
        return ApiResponse.success("Joined course successfully", enrollmentService.joinCourse(request.getJoinCode()));
    }

    @GetMapping
    public ApiResponse<List<UniversityCourseResponse>> getCourses() {
        return ApiResponse.success("Courses retrieved successfully", enrollmentService.getEnrolledCourses());
    }

    @GetMapping("/{id}")
    public ApiResponse<UniversityCourseResponse> getCourse(@PathVariable Long id) {
        return ApiResponse.success("Course retrieved successfully", enrollmentService.getCourse(id));
    }

    @DeleteMapping("/{id}/leave")
    public ApiResponse<Void> leaveCourse(@PathVariable Long id) {
        enrollmentService.leaveCourse(id);
        return ApiResponse.success("Left course successfully", null);
    }
}
