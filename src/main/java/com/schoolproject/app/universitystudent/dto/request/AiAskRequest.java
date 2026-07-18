package com.schoolproject.app.universitystudent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiAskRequest {
    @NotNull
    private Long materialId;

    @NotBlank
    @Size(max = 5000, message = "Question must not exceed 5000 characters")
    private String question;
}
