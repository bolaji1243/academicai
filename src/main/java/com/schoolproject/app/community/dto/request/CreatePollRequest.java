package com.schoolproject.app.community.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePollRequest {
    private Long channelId;
    private String question;
    private List<String> options;
    private LocalDateTime endsAt;
}
