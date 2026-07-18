package com.schoolproject.app.aspiringstudent.topic;

import com.schoolproject.app.aspiringstudent.topic.dto.TopicResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@PreAuthorize("hasAnyRole('ASPIRING_STUDENT', 'UNIVERSITY_STUDENT', 'LECTURER')")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/{subjectId}/topics")
    public List<TopicResponse> getTopicsBySubject(@PathVariable String subjectId) {
        return topicService.getTopicsBySubject(subjectId);
    }
}
