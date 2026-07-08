package com.schoolproject.app.lecturer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String department;
    private String faculty;
    private String staffId;
    private String title;
    private String bio;
    private boolean profileVisible;
}
