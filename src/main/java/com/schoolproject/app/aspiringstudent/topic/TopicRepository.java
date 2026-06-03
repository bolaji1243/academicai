package com.schoolproject.app.aspiringstudent.topic;

import com.schoolproject.app.aspiringstudent.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByPublicId(String publicId);

    List<Topic> findBySubjectOrderByNameAsc(Subject subject);
}
