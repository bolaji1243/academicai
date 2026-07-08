package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.AttendanceRecord;
import com.schoolproject.app.lecturer.enums.MarkedBy;
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
public class AttendanceRecordResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String markedAt;
    private MarkedBy markedBy;

    public static AttendanceRecordResponse from(AttendanceRecord record) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent().getId())
                .studentName(record.getStudent().getFullName())
                .markedAt(record.getMarkedAt() != null ? record.getMarkedAt().format(fmt) : null)
                .markedBy(record.getMarkedBy())
                .build();
    }
}
