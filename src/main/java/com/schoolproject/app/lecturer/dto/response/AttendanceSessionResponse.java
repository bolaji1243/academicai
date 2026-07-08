package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.AttendanceSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String openedAt;
    private String closedAt;
    private boolean isOpen;
    private int windowMinutes;
    private long markedCount;

    public static AttendanceSessionResponse from(AttendanceSession session, long markedCount, Long courseId, String courseTitle) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return AttendanceSessionResponse.builder()
                .id(session.getId())
                .courseId(courseId)
                .courseTitle(courseTitle)
                .openedAt(session.getOpenedAt() != null ? session.getOpenedAt().format(fmt) : null)
                .closedAt(session.getClosedAt() != null ? session.getClosedAt().format(fmt) : null)
                .isOpen(session.isOpen())
                .windowMinutes(session.getWindowMinutes())
                .markedCount(markedCount)
                .build();
    }
}
