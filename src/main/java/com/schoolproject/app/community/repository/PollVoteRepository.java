package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    Optional<PollVote> findByPollIdAndUserId(Long pollId, Long userId);
    long countByOptionId(Long optionId);
}
