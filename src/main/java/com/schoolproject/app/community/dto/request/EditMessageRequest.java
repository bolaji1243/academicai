package com.schoolproject.app.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditMessageRequest {
    @NotBlank(message = "Message content is required")
    @Size(max = 10000, message = "Message must not exceed 10000 characters")
    private String content;
}
