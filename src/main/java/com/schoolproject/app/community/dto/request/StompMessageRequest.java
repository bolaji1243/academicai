package com.schoolproject.app.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StompMessageRequest {
    @NotBlank(message = "Message content is required")
    private String content;
    private Long replyToId;
}
