package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.Assignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private String title;
    private String instructions;
    private LocalDateTime deadline;
    private Integer maxScore;
    private LocalDateTime createdAt;
    private Long courseId;
    private String courseTitle;
    private long submittedCount;
    private long pendingCount;

    public static AssignmentResponse from(Assignment assignment, long submittedCount, long pendingCount, String courseTitle) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .instructions(assignment.getInstructions())
                .deadline(assignment.getDeadline())
                .maxScore(assignment.getMaxScore())
                .createdAt(assignment.getCreatedAt())
                .courseId(assignment.getCourse().getId())
                .courseTitle(courseTitle)
                .submittedCount(submittedCount)
                .pendingCount(pendingCount)
                .build();
    }
}
