package com.schoolproject.app.dto.response;

import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.enums.Level;
import com.schoolproject.app.enums.Semester;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniversityStudentProfileResponse {
    private Long id;
    private String fullName;
    private String matricNumber;
    private String department;
    private String faculty;
    private Level level;
    private Semester semester;
    private String session;

    public static UniversityStudentProfileResponse from(UniversityStudentProfile profile) {
        return UniversityStudentProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .matricNumber(profile.getMatricNumber())
                .department(profile.getDepartment())
                .faculty(profile.getFaculty())
                .level(profile.getLevel())
                .semester(profile.getSemester())
                .session(profile.getSession())
                .build();
    }
}
