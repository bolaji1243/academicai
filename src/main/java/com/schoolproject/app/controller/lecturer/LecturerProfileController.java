package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.request.CreateProfileRequest;
import com.schoolproject.app.lecturer.dto.request.UpdateProfileRequest;
import com.schoolproject.app.lecturer.dto.response.LecturerProfileResponse;
import com.schoolproject.app.lecturer.service.LecturerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lecturer/profile")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Profile")
public class LecturerProfileController {

    private final LecturerProfileService lecturerProfileService;

    @PostMapping
    @Operation(summary = "Create lecturer profile")
    public ResponseEntity<ApiResponse<LecturerProfileResponse>> createProfile(
            @RequestBody @Valid CreateProfileRequest request) {
        LecturerProfileResponse data = lecturerProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", data));
    }

    @GetMapping
    @Operation(summary = "Get lecturer profile")
    public ResponseEntity<ApiResponse<LecturerProfileResponse>> getProfile() {
        LecturerProfileResponse data = lecturerProfileService.getProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", data));
    }

    @PutMapping
    @Operation(summary = "Update lecturer profile")
    public ResponseEntity<ApiResponse<LecturerProfileResponse>> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request) {
        LecturerProfileResponse data = lecturerProfileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", data));
    }
}
