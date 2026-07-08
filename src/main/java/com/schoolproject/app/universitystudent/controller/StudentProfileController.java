package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.universitystudent.dto.request.CreateProfileRequest;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.dto.response.ProfileResponse;
import com.schoolproject.app.universitystudent.service.StudentProfileService;
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
@RequestMapping("/api/university-student/profile")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService profileService;

    @PostMapping
    public ApiResponse<ProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return ApiResponse.success("Profile created successfully", profileService.createProfile(request));
    }

    @GetMapping
    public ApiResponse<ProfileResponse> getProfile() {
        return ApiResponse.success("Profile retrieved successfully", profileService.getProfile());
    }

    @PutMapping
    public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody CreateProfileRequest request) {
        return ApiResponse.success("Profile updated successfully", profileService.updateProfile(request));
    }
}
