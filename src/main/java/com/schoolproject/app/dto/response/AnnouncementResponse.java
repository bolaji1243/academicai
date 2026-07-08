package com.schoolproject.app.dto.response;

import com.schoolproject.app.lecturer.entity.Announcement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String body;
    private boolean pinned;
    private LocalDateTime createdAt;

    public static AnnouncementResponse from(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .pinned(announcement.isPinned())
                .createdAt(announcement.getCreatedAt())
                .build();
    }
}
