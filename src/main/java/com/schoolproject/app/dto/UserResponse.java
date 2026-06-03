package com.schoolproject.app.dto;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private String id;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private String faculty;
    private String staffId;
    private String matricNumber;
    private String level;

    public static UserResponse from(User user) {
        UserResponseBuilder builder = UserResponse.builder()
                .id(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole());

        LecturerProfile lecturerProfile = user.getLecturerProfile();
        if (lecturerProfile != null) {
            builder.department(lecturerProfile.getDepartment())
                    .faculty(lecturerProfile.getFaculty())
                    .staffId(lecturerProfile.getStaffId());
        }

        UniversityStudentProfile universityStudentProfile = user.getUniversityStudentProfile();
        if (universityStudentProfile != null) {
            builder.department(universityStudentProfile.getDepartment())
                    .faculty(universityStudentProfile.getFaculty())
                    .matricNumber(universityStudentProfile.getMatricNumber())
                    .level(universityStudentProfile.getLevel());
        }

        return builder.build();
    }
}
