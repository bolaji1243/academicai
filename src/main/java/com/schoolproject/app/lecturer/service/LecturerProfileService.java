package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.dto.request.CreateProfileRequest;
import com.schoolproject.app.lecturer.dto.request.UpdateProfileRequest;
import com.schoolproject.app.lecturer.dto.response.LecturerProfileResponse;
import com.schoolproject.app.lecturer.entity.LecturerProfileDetail;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.LecturerProfileDetailRepository;
import com.schoolproject.app.repository.LecturerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LecturerProfileService {

    private final LecturerContextService contextService;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final LecturerProfileDetailRepository profileDetailRepository;

    @Transactional
    public LecturerProfileResponse createProfile(CreateProfileRequest request) {
        User user = contextService.getCurrentUser();

        if (user.getLecturerProfile() != null) {
            throw new ConflictException("Lecturer profile already exists for this user");
        }
        if (lecturerProfileRepository.existsByStaffId(request.getStaffId())) {
            throw new ConflictException("Staff ID is already in use");
        }

        LecturerProfile profile = LecturerProfile.builder()
                .user(user)
                .department(request.getDepartment())
                .faculty(request.getFaculty())
                .staffId(request.getStaffId())
                .build();
        profile = lecturerProfileRepository.save(profile);

        if (request.getTitle() != null || request.getBio() != null) {
            LecturerProfileDetail detail = new LecturerProfileDetail()
                    .setLecturerProfile(profile)
                    .setTitle(request.getTitle())
                    .setBio(request.getBio())
                    .setProfileVisible(false);
            profileDetailRepository.save(detail);
        }

        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public LecturerProfileResponse getProfile() {
        LecturerProfile profile = contextService.getCurrentLecturer();
        return toResponse(profile);
    }

    @Transactional
    public LecturerProfileResponse updateProfile(UpdateProfileRequest request) {
        LecturerProfile profile = contextService.getCurrentLecturer();

        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getFaculty() != null) {
            profile.setFaculty(request.getFaculty());
        }
        if (request.getStaffId() != null) {
            if (!request.getStaffId().equals(profile.getStaffId())
                    && lecturerProfileRepository.existsByStaffId(request.getStaffId())) {
                throw new ConflictException("Staff ID is already in use");
            }
            profile.setStaffId(request.getStaffId());
        }
        lecturerProfileRepository.save(profile);

        LecturerProfileDetail detail = profileDetailRepository
                .findByLecturerProfile(profile)
                .orElseGet(() -> new LecturerProfileDetail().setLecturerProfile(profile));

        if (request.getTitle() != null) {
            detail.setTitle(request.getTitle());
        }
        if (request.getBio() != null) {
            detail.setBio(request.getBio());
        }
        detail.setProfileVisible(true);
        profileDetailRepository.save(detail);

        return toResponse(profile);
    }

    private LecturerProfileResponse toResponse(LecturerProfile profile) {
        LecturerProfileResponse response = LecturerProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getUser().getFullName())
                .email(profile.getUser().getEmail())
                .department(profile.getDepartment())
                .faculty(profile.getFaculty())
                .staffId(profile.getStaffId())
                .build();

        profileDetailRepository.findByLecturerProfile(profile).ifPresent(detail -> {
            response.setTitle(detail.getTitle());
            response.setBio(detail.getBio());
            response.setProfileVisible(detail.isProfileVisible());
        });

        return response;
    }
}
