package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.universitystudent.dto.request.JoinCourseRequest;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.dto.response.CourseResponse;
import com.schoolproject.app.universitystudent.service.StudentCourseEnrollmentService;
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
@RequestMapping("/api/university-student/courses")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentCourseEnrollmentService enrollmentService;

    @PostMapping("/join")
    public ApiResponse<CourseResponse> joinCourse(@Valid @RequestBody JoinCourseRequest request) {
        return ApiResponse.success("Joined course successfully", enrollmentService.joinCourse(request.getJoinCode()));
    }

    @GetMapping
    public ApiResponse<List<CourseResponse>> getCourses() {
        return ApiResponse.success("Courses retrieved successfully", enrollmentService.getEnrolledCourses());
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourse(@PathVariable Long id) {
        return ApiResponse.success("Course retrieved successfully", enrollmentService.getCourse(id));
    }

    @DeleteMapping("/{id}/leave")
    public ApiResponse<Void> leaveCourse(@PathVariable Long id) {
        enrollmentService.leaveCourse(id);
        return ApiResponse.success("Left course successfully", null);
    }
}
