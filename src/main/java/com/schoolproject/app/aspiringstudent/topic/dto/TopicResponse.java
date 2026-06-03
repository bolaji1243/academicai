package com.schoolproject.app.aspiringstudent.topic.dto;

import com.schoolproject.app.aspiringstudent.subject.dto.SubjectResponse;
import com.schoolproject.app.aspiringstudent.topic.Topic;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicResponse {

    private String id;
    private SubjectResponse subject;
    private String name;
    private String slug;
    private String description;

    public static TopicResponse from(Topic topic) {
        return new TopicResponse(
                topic.getPublicId(),
                SubjectResponse.from(topic.getSubject()),
                topic.getName(),
                topic.getSlug(),
                topic.getDescription()
        );
    }
}
