package com.schoolproject.app.lecturer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsRequest {

    @NotBlank(message = "Topic is required")
    @Size(max = 255, message = "Topic must not exceed 255 characters")
    private String topic;

    @NotBlank(message = "Level is required")
    @Size(max = 50, message = "Level must not exceed 50 characters")
    private String level;

    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 10, message = "Count must not exceed 10")
    private Integer count;
}
