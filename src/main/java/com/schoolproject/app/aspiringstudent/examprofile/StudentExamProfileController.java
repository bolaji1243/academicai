package com.schoolproject.app.aspiringstudent.examprofile;

import com.schoolproject.app.aspiringstudent.examprofile.dto.SaveStudentExamProfileRequest;
import com.schoolproject.app.aspiringstudent.examprofile.dto.StudentExamProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentExamProfileController {

    private final StudentExamProfileService studentExamProfileService;

    public StudentExamProfileController(StudentExamProfileService studentExamProfileService) {
        this.studentExamProfileService = studentExamProfileService;
    }

    @PostMapping("/exam-profile")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentExamProfileResponse createProfile(
            Authentication authentication,
            @Valid @RequestBody SaveStudentExamProfileRequest request
    ) {
        return studentExamProfileService.createProfile(authentication.getName(), request);
    }

    @GetMapping("/exam-profile")
    public StudentExamProfileResponse getProfile(Authentication authentication) {
        return studentExamProfileService.getProfile(authentication.getName());
    }

    @PutMapping("/exam-profile")
    public StudentExamProfileResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody SaveStudentExamProfileRequest request
    ) {
        return studentExamProfileService.updateProfile(authentication.getName(), request);
    }
}
