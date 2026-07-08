package com.schoolproject.app.universitystudent.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiPracticeQuestionsRequest {
    @NotNull
    private Long materialId;

    @Min(1)
    @Max(20)
    private int count = 5;
}
