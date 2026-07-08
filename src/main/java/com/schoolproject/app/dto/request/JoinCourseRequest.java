package com.schoolproject.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinCourseRequest {
    @NotBlank
    private String joinCode;
}
