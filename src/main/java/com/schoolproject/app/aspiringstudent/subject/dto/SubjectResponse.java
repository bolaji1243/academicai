package com.schoolproject.app.aspiringstudent.subject.dto;

import com.schoolproject.app.aspiringstudent.subject.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectResponse {

    private String id;
    private String name;
    private String slug;
    private String description;

    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(
                subject.getPublicId(),
                subject.getName(),
                subject.getSlug(),
                subject.getDescription()
        );
    }
}
