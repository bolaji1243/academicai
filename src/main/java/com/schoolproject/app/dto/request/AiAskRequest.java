package com.schoolproject.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiAskRequest {
    @NotNull
    private Long materialId;

    @NotBlank
    private String question;
}
