package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.request.UniversityStudentProfileRequest;
import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.dto.response.UniversityStudentProfileResponse;
import com.schoolproject.app.service.student.university.UniversityStudentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/university/profile")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityStudentProfileController {

    private final UniversityStudentProfileService profileService;

    @PostMapping
    public ApiResponse<UniversityStudentProfileResponse> createProfile(
            @Valid @RequestBody UniversityStudentProfileRequest request) {
        return ApiResponse.success("Profile created successfully", profileService.createProfile(request));
    }

    @GetMapping
    public ApiResponse<UniversityStudentProfileResponse> getProfile() {
        return ApiResponse.success("Profile retrieved successfully", profileService.getProfile());
    }

    @PutMapping
    public ApiResponse<UniversityStudentProfileResponse> updateProfile(
            @Valid @RequestBody UniversityStudentProfileRequest request) {
        return ApiResponse.success("Profile updated successfully", profileService.updateProfile(request));
    }
}
