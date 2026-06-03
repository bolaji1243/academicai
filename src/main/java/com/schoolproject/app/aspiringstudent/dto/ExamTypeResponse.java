package com.schoolproject.app.aspiringstudent.dto;

import com.schoolproject.app.aspiringstudent.entity.ExamType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamTypeResponse {

    private String id;
    private String name;
    private String slug;
    private String description;

    public static ExamTypeResponse from(ExamType examType) {
        return new ExamTypeResponse(
                examType.getPublicId(),
                examType.getName(),
                examType.getSlug(),
                examType.getDescription()
        );
    }
}
