package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String publicId;
    private String fullName;
    private String email;
    private String role;
    private boolean muted;
    private LocalDateTime joinedAt;
}
