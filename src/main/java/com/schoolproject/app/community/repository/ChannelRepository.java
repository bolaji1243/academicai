package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByCommunityIdAndDeletedFalseOrderByCreatedAtAsc(Long communityId);
}
