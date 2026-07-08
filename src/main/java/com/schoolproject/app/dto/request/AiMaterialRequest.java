package com.schoolproject.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiMaterialRequest {
    @NotNull
    private Long materialId;
}
