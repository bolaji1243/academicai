package com.schoolproject.app.community.repository;

import com.schoolproject.app.community.entity.CommunityMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<CommunityMember> findByCommunityId(Long communityId);

    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);
}
