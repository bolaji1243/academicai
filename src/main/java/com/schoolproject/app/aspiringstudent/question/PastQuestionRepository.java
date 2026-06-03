package com.schoolproject.app.aspiringstudent.question;

import com.schoolproject.app.aspiringstudent.topic.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface PastQuestionRepository extends JpaRepository<PastQuestion, Long> {

    List<PastQuestion> findByTopicOrderByExamYearDescQuestionTextAsc(Topic topic);

    List<PastQuestion> findByPublicIdIn(Collection<String> publicIds);
}
