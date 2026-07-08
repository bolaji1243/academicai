package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChannelIdAndDeletedFalseOrderByCreatedAtDesc(Long channelId, Pageable pageable);

    List<Message> findByChannelIdAndPinnedTrueAndDeletedFalse(Long channelId);

    @Query(value = "SELECT * FROM messages m WHERE m.channel_id = :channelId AND m.deleted = false AND MATCH(m.content) AGAINST(:query IN BOOLEAN MODE)", nativeQuery = true)
    Page<Message> searchByContent(@Param("channelId") Long channelId, @Param("query") String query, Pageable pageable);
}
