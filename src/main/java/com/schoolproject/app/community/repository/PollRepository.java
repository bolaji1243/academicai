package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByChannelIdAndDeletedFalseOrderByCreatedAtDesc(Long channelId);
}
