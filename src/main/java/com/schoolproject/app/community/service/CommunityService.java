package com.schoolproject.app.community.service;

import com.schoolproject.app.community.dto.request.CreateChannelRequest;
import com.schoolproject.app.community.dto.response.ChannelResponse;
import com.schoolproject.app.community.dto.response.CommunityResponse;
import com.schoolproject.app.community.dto.response.MemberResponse;
import com.schoolproject.app.community.entity.Channel;
import com.schoolproject.app.community.entity.ChannelType;
import com.schoolproject.app.community.entity.Community;
import com.schoolproject.app.community.entity.CommunityMember;
import com.schoolproject.app.community.repository.ChannelRepository;
import com.schoolproject.app.community.repository.CommunityMemberRepository;
import com.schoolproject.app.community.repository.CommunityRepository;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.service.LecturerContextService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final ChannelRepository channelRepository;
    private final CommunityMemberRepository memberRepository;
    private final LecturerContextService contextService;

    @Transactional
    public Community createCommunity(Long courseId, String courseName) {
        Community community = new Community()
                .setCourseId(courseId)
                .setName(courseName + " Community")
                .setDeleted(false);
        community = communityRepository.save(community);

        Channel general = new Channel()
                .setCommunity(community)
                .setName("General")
                .setType(ChannelType.GENERAL)
                .setLocked(false)
                .setDeleted(false);
        channelRepository.save(general);

        Channel announcements = new Channel()
                .setCommunity(community)
                .setName("Announcements")
                .setType(ChannelType.ANNOUNCEMENT)
                .setLocked(false)
                .setDeleted(false);
        channelRepository.save(announcements);

        return community;
    }

    @Transactional
    public void addMember(Long communityId, User user, String role) {
        if (!memberRepository.existsByCommunityIdAndUserId(communityId, user.getId())) {
            CommunityMember member = new CommunityMember()
                    .setCommunity(communityRepository.getReferenceById(communityId))
                    .setUser(user)
                    .setRole(role)
                    .setMuted(false)
                    .setDeleted(false);
            memberRepository.save(member);
        }
    }

    public CommunityResponse getCommunity(Long courseId) {
        User currentUser = contextService.getCurrentUser();
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));

        boolean isMember = memberRepository.existsByCommunityIdAndUserId(community.getId(), currentUser.getId());
        if (!isMember) {
            throw new com.schoolproject.app.lecturer.exception.ForbiddenException("You are not a member of this community");
        }

        List<Channel> channels = channelRepository.findByCommunityIdAndDeletedFalseOrderByCreatedAtAsc(community.getId());
        long memberCount = memberRepository.findByCommunityId(community.getId()).size();

        List<ChannelResponse> channelResponses = channels.stream()
                .map(ch -> ChannelResponse.builder()
                        .id(ch.getId())
                        .name(ch.getName())
                        .type(ch.getType().name())
                        .locked(ch.isLocked())
                        .createdAt(ch.getCreatedAt())
                        .build())
                .toList();

        return CommunityResponse.builder()
                .id(community.getId())
                .courseId(community.getCourseId())
                .name(community.getName())
                .channels(channelResponses)
                .memberCount(memberCount)
                .createdAt(community.getCreatedAt())
                .build();
    }

    @Transactional
    public ChannelResponse createChannel(Long courseId, CreateChannelRequest request) {
        contextService.verifyCourseOwnership(courseId);
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));

        Channel channel = new Channel()
                .setCommunity(community)
                .setName(request.getName())
                .setType(request.getType() != null ? request.getType() : ChannelType.GENERAL)
                .setLocked(false)
                .setDeleted(false);
        channel = channelRepository.save(channel);

        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .type(channel.getType().name())
                .locked(channel.isLocked())
                .createdAt(channel.getCreatedAt())
                .build();
    }

    public List<MemberResponse> getMembers(Long courseId) {
        User currentUser = contextService.getCurrentUser();
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));

        if (!memberRepository.existsByCommunityIdAndUserId(community.getId(), currentUser.getId())) {
            throw new com.schoolproject.app.lecturer.exception.ForbiddenException("You are not a member of this community");
        }

        return memberRepository.findByCommunityId(community.getId()).stream()
                .map(m -> MemberResponse.builder()
                        .id(m.getId())
                        .publicId(m.getUser().getPublicId())
                        .fullName(m.getUser().getFullName())
                        .email(m.getUser().getEmail())
                        .role(m.getRole())
                        .muted(m.isMuted())
                        .joinedAt(m.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void toggleMute(Long courseId) {
        User currentUser = contextService.getCurrentUser();
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));

        CommunityMember member = memberRepository
                .findByCommunityIdAndUserId(community.getId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this community"));

        member.setMuted(!member.isMuted());
        memberRepository.save(member);
    }

    @Transactional
    public void lockChannel(Long courseId, Long channelId, boolean locked) {
        contextService.verifyCourseOwnership(courseId);
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        if (!channel.getCommunity().getId().equals(community.getId())) {
            throw new com.schoolproject.app.lecturer.exception.ForbiddenException("Channel does not belong to this community");
        }

        channel.setLocked(locked);
        channelRepository.save(channel);
    }
}
