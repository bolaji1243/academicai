package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private String senderName;
    private String resourceId;
    private boolean read;
    private LocalDateTime createdAt;
}
