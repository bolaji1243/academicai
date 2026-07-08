package com.schoolproject.app.universitystudent.dto.response;

import com.schoolproject.app.lecturer.entity.AttendanceRecord;
import com.schoolproject.app.lecturer.entity.AttendanceSession;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class AttendanceRecordResponse {
    private Long sessionId;
    private String openedAt;
    private String closedAt;
    private boolean present;
    private String markedAt;

    public static AttendanceRecordResponse absent(AttendanceSession session) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return AttendanceRecordResponse.builder()
                .sessionId(session.getId())
                .openedAt(session.getOpenedAt() != null ? session.getOpenedAt().format(fmt) : null)
                .closedAt(session.getClosedAt() != null ? session.getClosedAt().format(fmt) : null)
                .present(false)
                .build();
    }

    public static AttendanceRecordResponse present(AttendanceRecord record) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        AttendanceSession session = record.getSession();
        return AttendanceRecordResponse.builder()
                .sessionId(session.getId())
                .openedAt(session.getOpenedAt() != null ? session.getOpenedAt().format(fmt) : null)
                .closedAt(session.getClosedAt() != null ? session.getClosedAt().format(fmt) : null)
                .present(true)
                .markedAt(record.getMarkedAt() != null ? record.getMarkedAt().format(fmt) : null)
                .build();
    }
}
