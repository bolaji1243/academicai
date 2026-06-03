package com.schoolproject.app.dto;

import com.schoolproject.app.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

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

    private Role role;

    @Size(max = 80, message = "Matric number must be 80 characters or less")
    private String matricNumber;

    @Size(max = 120, message = "Department must be 120 characters or less")
    private String department;

    @Size(max = 30, message = "Level must be 30 characters or less")
    private String level;

    @Size(max = 120, message = "Faculty must be 120 characters or less")
    private String faculty;
}
