package com.schoolproject.app.service.student.university;

import com.schoolproject.app.dto.request.UniversityStudentProfileRequest;
import com.schoolproject.app.dto.response.UniversityStudentProfileResponse;
import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.repository.UniversityStudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UniversityStudentProfileService {

    private final UniversityStudentContextService contextService;
    private final UniversityStudentProfileRepository profileRepository;

    @Transactional
    public UniversityStudentProfileResponse createProfile(UniversityStudentProfileRequest request) {
        User student = contextService.getCurrentStudent();
        if (profileRepository.findByUser(student).isPresent()) {
            throw new ConflictException("University student profile already exists");
        }
        if (profileRepository.existsByMatricNumber(request.getMatricNumber())) {
            throw new ConflictException("Matric number already exists");
        }

        UniversityStudentProfile profile = new UniversityStudentProfile();
        profile.setUser(student);
        apply(profile, request);
        return UniversityStudentProfileResponse.from(profileRepository.save(profile));
    }

    public UniversityStudentProfileResponse getProfile() {
        User student = contextService.getCurrentStudent();
        UniversityStudentProfile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new ResourceNotFoundException("University student profile not found"));
        return UniversityStudentProfileResponse.from(profile);
    }

    @Transactional
    public UniversityStudentProfileResponse updateProfile(UniversityStudentProfileRequest request) {
        User student = contextService.getCurrentStudent();
        UniversityStudentProfile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new ResourceNotFoundException("University student profile not found"));
        if (profileRepository.existsByMatricNumberAndUserNot(request.getMatricNumber(), student)) {
            throw new ConflictException("Matric number already exists");
        }

        apply(profile, request);
        return UniversityStudentProfileResponse.from(profileRepository.save(profile));
    }

    private void apply(UniversityStudentProfile profile, UniversityStudentProfileRequest request) {
        profile.setFullName(request.getFullName());
        profile.setMatricNumber(request.getMatricNumber());
        profile.setDepartment(request.getDepartment());
        profile.setFaculty(request.getFaculty());
        profile.setLevel(request.getLevel());
        profile.setSemester(request.getSemester());
        profile.setSession(request.getSession());
    }
}
