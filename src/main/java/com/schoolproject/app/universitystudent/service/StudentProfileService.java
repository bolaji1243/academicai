package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.repository.UniversityStudentProfileRepository;
import com.schoolproject.app.universitystudent.dto.request.CreateProfileRequest;
import com.schoolproject.app.universitystudent.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentContextService contextService;
    private final UniversityStudentProfileRepository profileRepository;

    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        User student = contextService.getCurrentStudent();
        if (profileRepository.findByUser(student).isPresent()) {
            throw new ConflictException("University student profile already exists");
        }
        if (profileRepository.existsByMatricNumber(request.getMatricNumber())) {
            throw new ConflictException("Matric number already exists");
        }

        UniversityStudentProfile profile = UniversityStudentProfile.builder()
                .user(student)
                .fullName(request.getFullName())
                .matricNumber(request.getMatricNumber())
                .department(request.getDepartment())
                .faculty(request.getFaculty())
                .level(request.getLevel())
                .semester(request.getSemester())
                .session(request.getSession())
                .build();
        return ProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        User student = contextService.getCurrentStudent();
        UniversityStudentProfile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new ResourceNotFoundException("University student profile not found"));
        return ProfileResponse.from(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(CreateProfileRequest request) {
        User student = contextService.getCurrentStudent();
        UniversityStudentProfile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new ResourceNotFoundException("University student profile not found"));
        if (profileRepository.existsByMatricNumberAndUserNot(request.getMatricNumber(), student)) {
            throw new ConflictException("Matric number already exists");
        }

        profile.setFullName(request.getFullName());
        profile.setMatricNumber(request.getMatricNumber());
        profile.setDepartment(request.getDepartment());
        profile.setFaculty(request.getFaculty());
        profile.setLevel(request.getLevel());
        profile.setSemester(request.getSemester());
        profile.setSession(request.getSession());
        return ProfileResponse.from(profileRepository.save(profile));
    }
}
