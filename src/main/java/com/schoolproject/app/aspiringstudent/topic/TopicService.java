package com.schoolproject.app.aspiringstudent.topic;

import com.schoolproject.app.aspiringstudent.subject.Subject;
import com.schoolproject.app.aspiringstudent.subject.SubjectRepository;
import com.schoolproject.app.aspiringstudent.topic.dto.TopicResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;

    public TopicService(TopicRepository topicRepository, SubjectRepository subjectRepository) {
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getTopicsBySubject(String subjectId) {
        Subject subject = subjectRepository.findByPublicId(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        return topicRepository.findBySubjectOrderByNameAsc(subject)
                .stream()
                .map(TopicResponse::from)
                .toList();
    }
}
