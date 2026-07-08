package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChannelResponse {
    private Long id;
    private String name;
    private String type;
    private boolean locked;
    private LocalDateTime createdAt;
}
