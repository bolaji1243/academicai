package com.schoolproject.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceHistoryResponse {
    private long totalSessions;
    private long attendedSessions;
    private double percentage;
    private List<AttendanceRecordResponse> records;
}
