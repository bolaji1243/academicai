package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PollResponse {
    private Long id;
    private Long channelId;
    private String question;
    private Long createdBy;
    private LocalDateTime endsAt;
    private boolean closed;
    private List<PollOptionResponse> options;
    private boolean voted;
    private LocalDateTime createdAt;
}
