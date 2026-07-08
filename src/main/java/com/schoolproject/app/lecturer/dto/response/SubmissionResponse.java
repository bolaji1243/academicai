package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {

    private Long id;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private Integer score;
    private String feedback;
    private Long studentId;
    private String studentName;
    private String studentEmail;

    public static SubmissionResponse from(AssignmentSubmission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .fileUrl(submission.getFileUrl())
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getFullName())
                .studentEmail(submission.getStudent().getEmail())
                .build();
    }
}
