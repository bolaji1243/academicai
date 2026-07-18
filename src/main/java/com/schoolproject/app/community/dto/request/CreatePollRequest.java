package com.schoolproject.app.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePollRequest {
    private Long channelId;
    @NotBlank(message = "Poll question is required")
    @Size(max = 1000, message = "Poll question must not exceed 1000 characters")
    private String question;
    @NotNull(message = "Poll options are required")
    @Size(min = 2, message = "A poll must have at least 2 options")
    private List<String> options;
    private LocalDateTime endsAt;
}
