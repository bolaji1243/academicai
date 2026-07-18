package com.schoolproject.app.aspiringstudent.question;

import com.schoolproject.app.aspiringstudent.question.dto.PastQuestionResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@PreAuthorize("hasAnyRole('ASPIRING_STUDENT', 'UNIVERSITY_STUDENT', 'LECTURER')")
public class PastQuestionController {

    private final PastQuestionService pastQuestionService;

    public PastQuestionController(PastQuestionService pastQuestionService) {
        this.pastQuestionService = pastQuestionService;
    }

    @GetMapping("/{topicId}/questions")
    public List<PastQuestionResponse> getQuestionsByTopic(@PathVariable String topicId) {
        return pastQuestionService.getQuestionsByTopic(topicId);
    }
}
