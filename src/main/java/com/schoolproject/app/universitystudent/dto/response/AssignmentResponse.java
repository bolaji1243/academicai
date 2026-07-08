package com.schoolproject.app.universitystudent.dto.response;

import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private String title;
    private String instructions;
    private LocalDateTime deadline;
    private Integer maxScore;
    private SubmissionStatus status;
    private Integer score;
    private String feedback;
    private LocalDateTime submittedAt;

    public static AssignmentResponse from(Assignment assignment, AssignmentSubmission submission) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .instructions(assignment.getInstructions())
                .deadline(assignment.getDeadline())
                .maxScore(assignment.getMaxScore())
                .status(submission == null ? SubmissionStatus.PENDING : submission.getStatus())
                .score(submission == null ? null : submission.getScore())
                .feedback(submission == null ? null : submission.getFeedback())
                .submittedAt(submission == null ? null : submission.getSubmittedAt())
                .build();
    }
}
