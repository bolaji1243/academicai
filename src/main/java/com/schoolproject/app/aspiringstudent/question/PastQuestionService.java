package com.schoolproject.app.aspiringstudent.question;

import com.schoolproject.app.aspiringstudent.question.dto.PastQuestionResponse;
import com.schoolproject.app.aspiringstudent.topic.Topic;
import com.schoolproject.app.aspiringstudent.topic.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PastQuestionService {

    private final PastQuestionRepository pastQuestionRepository;
    private final TopicRepository topicRepository;

    public PastQuestionService(PastQuestionRepository pastQuestionRepository, TopicRepository topicRepository) {
        this.pastQuestionRepository = pastQuestionRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public List<PastQuestionResponse> getQuestionsByTopic(String topicId) {
        Topic topic = topicRepository.findByPublicId(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        return pastQuestionRepository.findByTopicOrderByExamYearDescQuestionTextAsc(topic)
                .stream()
                .map(PastQuestionResponse::from)
                .toList();
    }
}
