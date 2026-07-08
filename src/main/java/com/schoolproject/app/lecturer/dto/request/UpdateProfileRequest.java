package com.schoolproject.app.lecturer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String department;

    private String faculty;

    private String staffId;

    private String title;

    private String bio;
}
