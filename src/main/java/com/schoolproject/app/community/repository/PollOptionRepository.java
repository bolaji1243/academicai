package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPollIdAndDeletedFalse(Long pollId);
}
