package com.schoolproject.app.aspiringstudent.question;

import com.schoolproject.app.aspiringstudent.entity.ExamType;
import com.schoolproject.app.aspiringstudent.topic.Topic;
import com.schoolproject.app.aspiringstudent.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface PastQuestionRepository extends JpaRepository<PastQuestion, Long> {

    List<PastQuestion> findByTopicOrderByExamYearDescQuestionTextAsc(Topic topic);

    List<PastQuestion> findByPublicIdIn(Collection<String> publicIds);

    List<PastQuestion> findByExamTypeAndSubject(ExamType examType, Subject subject);
}
