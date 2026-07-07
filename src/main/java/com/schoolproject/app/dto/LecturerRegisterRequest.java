package com.schoolproject.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LecturerRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must be 120 characters or less")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 160, message = "Email must be 160 characters or less")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String password;

    @NotBlank(message = "Department is required")
    @Size(max = 120, message = "Department must be 120 characters or less")
    private String department;

    @NotBlank(message = "Faculty is required")
    @Size(max = 120, message = "Faculty must be 120 characters or less")
    private String faculty;

    @NotBlank(message = "Staff ID is required")
    @Size(max = 80, message = "Staff ID must be 80 characters or less")
    private String staffId;

    @NotBlank(message = "Lecturer registration code is required")
    @Size(max = 120, message = "Lecturer registration code must be 120 characters or less")
    private String lecturerRegistrationCode;
}

