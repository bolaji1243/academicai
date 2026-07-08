package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.request.CreateCourseRequest;
import com.schoolproject.app.lecturer.dto.request.UpdateCourseRequest;
import com.schoolproject.app.lecturer.dto.response.CourseResponse;
import com.schoolproject.app.lecturer.dto.response.CourseStudentResponse;
import com.schoolproject.app.lecturer.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer/courses")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a course")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestBody @Valid CreateCourseRequest request) {
        CourseResponse data = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", data));
    }

    @GetMapping
    @Operation(summary = "Get all courses")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getCourses(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<CourseResponse> data = courseService.getMyCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        CourseResponse data = courseService.getCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", data));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCourseRequest request) {
        CourseResponse data = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", data));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive a course")
    public ResponseEntity<ApiResponse<CourseResponse>> archiveCourse(@PathVariable Long id) {
        CourseResponse data = courseService.archiveCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course archived successfully", data));
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Get enrolled students")
    public ResponseEntity<ApiResponse<List<CourseStudentResponse>>> getEnrolledStudents(
            @PathVariable Long id) {
        List<CourseStudentResponse> data = courseService.getEnrolledStudents(id);
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", data));
    }

    @DeleteMapping("/{id}/students/{studentPublicId}")
    @Operation(summary = "Remove a student from a course")
    public ResponseEntity<ApiResponse<Void>> removeStudent(
            @PathVariable Long id,
            @PathVariable String studentPublicId) {
        courseService.removeStudent(id, studentPublicId);
        return ResponseEntity.ok(ApiResponse.success("Student removed successfully", null));
    }
}
