package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class AnnouncementSummaryResponse {

    private Long id;
    private String title;
    private String body;
    private boolean pinned;
    private LocalDateTime createdAt;
    private Long courseId;
    private String courseTitle;

    public static AnnouncementSummaryResponse from(Announcement announcement) {
        return AnnouncementSummaryResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .pinned(announcement.isPinned())
                .createdAt(announcement.getCreatedAt())
                .courseId(announcement.getCourse() != null ? announcement.getCourse().getId() : null)
                .courseTitle(announcement.getCourse() != null ? announcement.getCourse().getTitle() : null)
                .build();
    }
}
