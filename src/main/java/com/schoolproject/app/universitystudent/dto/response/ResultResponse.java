package com.schoolproject.app.universitystudent.dto.response;

import com.schoolproject.app.lecturer.entity.Result;
import com.schoolproject.app.lecturer.enums.AssessmentType;
import com.schoolproject.app.lecturer.enums.CourseGrade;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultResponse {
    private AssessmentType assessmentType;
    private Integer score;
    private Integer maxScore;
    private CourseGrade grade;

    public static ResultResponse from(Result result) {
        return ResultResponse.builder()
                .assessmentType(result.getAssessmentType())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .grade(result.getGrade())
                .build();
    }
}
