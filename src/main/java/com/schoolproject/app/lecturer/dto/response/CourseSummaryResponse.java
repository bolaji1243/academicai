package com.schoolproject.app.lecturer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryResponse {

    private Long id;
    private String name;
    private String code;
    private long studentCount;
    private long materialCount;
    private long assignmentCount;
    private long pendingSubmissionCount;
    private Integer attendanceRate;
    private Integer averageScore;
    private LocalDateTime lastActivityAt;
}
