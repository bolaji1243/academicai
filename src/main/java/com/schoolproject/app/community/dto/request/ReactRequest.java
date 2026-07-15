package com.schoolproject.app.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReactRequest {
    @NotBlank(message = "Emoji is required")
    private String emoji;
}
