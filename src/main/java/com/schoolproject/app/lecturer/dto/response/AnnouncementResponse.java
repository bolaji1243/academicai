package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

    private Long id;
    private String title;
    private String body;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private Long courseId;
    private String courseTitle;

    public static AnnouncementResponse from(Announcement announcement, Long courseId, String courseTitle) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .isPinned(announcement.isPinned())
                .createdAt(announcement.getCreatedAt())
                .courseId(courseId)
                .courseTitle(courseTitle)
                .build();
    }
}
