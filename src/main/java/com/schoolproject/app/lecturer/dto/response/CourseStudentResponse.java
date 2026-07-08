package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.CourseEnrollment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentResponse {

    private String id;
    private String userId;
    private String fullName;
    private String email;
    private String matricNumber;
    private LocalDateTime enrolledAt;

    public static CourseStudentResponse from(CourseEnrollment enrollment) {
        var student = enrollment.getStudent();
        var uniProfile = student.getUniversityStudentProfile();
        return CourseStudentResponse.builder()
                .id(student.getPublicId())
                .userId(String.valueOf(student.getId()))
                .fullName(student.getFullName())
                .email(student.getEmail())
                .matricNumber(uniProfile != null ? uniProfile.getMatricNumber() : null)
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
