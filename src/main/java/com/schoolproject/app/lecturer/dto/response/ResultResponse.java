package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.Result;
import com.schoolproject.app.lecturer.enums.AssessmentType;
import com.schoolproject.app.lecturer.enums.CourseGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private AssessmentType assessmentType;
    private Integer score;
    private Integer maxScore;
    private CourseGrade grade;
    private LocalDateTime createdAt;

    public static ResultResponse from(Result result) {
        return ResultResponse.builder()
                .id(result.getId())
                .studentId(result.getStudent().getId())
                .studentName(result.getStudent().getFullName())
                .assessmentType(result.getAssessmentType())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .grade(result.getGrade())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
