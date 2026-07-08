package com.schoolproject.app.universitystudent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinCourseRequest {
    @NotBlank
    private String joinCode;
}
