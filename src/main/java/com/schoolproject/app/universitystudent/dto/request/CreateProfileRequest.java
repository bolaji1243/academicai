package com.schoolproject.app.universitystudent.dto.request;

import com.schoolproject.app.enums.Level;
import com.schoolproject.app.enums.Semester;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProfileRequest {

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Size(max = 80)
    private String matricNumber;

    @NotBlank
    @Size(max = 120)
    private String department;

    @NotBlank
    @Size(max = 120)
    private String faculty;

    @NotNull
    private Level level;

    @NotNull
    private Semester semester;

    @NotBlank
    @Pattern(regexp = "\\d{4}/\\d{4}", message = "session must use format 2024/2025")
    private String session;
}
