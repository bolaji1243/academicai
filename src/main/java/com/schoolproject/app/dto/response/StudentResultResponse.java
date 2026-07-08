package com.schoolproject.app.dto.response;

import com.schoolproject.app.lecturer.entity.Result;
import com.schoolproject.app.lecturer.enums.AssessmentType;
import com.schoolproject.app.lecturer.enums.CourseGrade;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResultResponse {
    private AssessmentType assessmentType;
    private Integer score;
    private Integer maxScore;
    private CourseGrade grade;

    public static StudentResultResponse from(Result result) {
        return StudentResultResponse.builder()
                .assessmentType(result.getAssessmentType())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .grade(result.getGrade())
                .build();
    }
}
