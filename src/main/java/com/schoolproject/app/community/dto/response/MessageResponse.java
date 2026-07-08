package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private Long channelId;
    private AuthorResponse author;
    private String content;
    private Long replyToId;
    private boolean pinned;
    private LocalDateTime editedAt;
    private List<AttachmentResponse> attachments;
    private Map<String, Integer> reactions;
    private LocalDateTime createdAt;
}
