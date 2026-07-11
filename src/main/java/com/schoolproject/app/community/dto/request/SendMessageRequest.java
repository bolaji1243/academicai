package com.schoolproject.app.community.dto.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String content;
    private Long replyToId;
}
