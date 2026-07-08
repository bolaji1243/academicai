package com.schoolproject.app.universitystudent.dto.response;

import com.schoolproject.app.lecturer.entity.AttendanceSession;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Builder
public class OpenAttendanceSessionResponse {
    private Long sessionId;
    private Long courseId;
    private String courseTitle;
    private String courseCode;
    private String openedAt;
    private int windowMinutes;

    public static OpenAttendanceSessionResponse from(AttendanceSession session) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return OpenAttendanceSessionResponse.builder()
                .sessionId(session.getId())
                .courseId(session.getCourse().getId())
                .courseTitle(session.getCourse().getTitle())
                .courseCode(session.getCourse().getCourseCode())
                .openedAt(session.getOpenedAt() != null ? session.getOpenedAt().format(fmt) : null)
                .windowMinutes(session.getWindowMinutes())
                .build();
    }
}
