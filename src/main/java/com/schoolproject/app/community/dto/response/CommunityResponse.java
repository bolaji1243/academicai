package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommunityResponse {
    private Long id;
    private Long courseId;
    private String name;
    private List<ChannelResponse> channels;
    private long memberCount;
    private LocalDateTime createdAt;
}
