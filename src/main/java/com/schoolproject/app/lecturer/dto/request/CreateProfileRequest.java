package com.schoolproject.app.lecturer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Faculty is required")
    private String faculty;

    @NotBlank(message = "Staff ID is required")
    private String staffId;

    private String title;

    private String bio;
}
