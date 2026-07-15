package com.schoolproject.app.community.service;

import com.schoolproject.app.community.dto.request.CreatePollRequest;
import com.schoolproject.app.community.dto.response.PollOptionResponse;
import com.schoolproject.app.community.dto.response.PollResponse;
import com.schoolproject.app.community.entity.Channel;
import com.schoolproject.app.community.entity.Community;
import com.schoolproject.app.community.entity.Poll;
import com.schoolproject.app.community.entity.PollOption;
import com.schoolproject.app.community.entity.PollVote;
import com.schoolproject.app.community.repository.ChannelRepository;
import com.schoolproject.app.community.repository.CommunityMemberRepository;
import com.schoolproject.app.community.repository.CommunityRepository;
import com.schoolproject.app.community.repository.PollOptionRepository;
import com.schoolproject.app.community.repository.PollRepository;
import com.schoolproject.app.community.repository.PollVoteRepository;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.exception.ForbiddenException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.service.LecturerContextService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository optionRepository;
    private final PollVoteRepository voteRepository;
    private final CommunityRepository communityRepository;
    private final ChannelRepository channelRepository;
    private final CommunityMemberRepository memberRepository;
    private final LecturerContextService contextService;

    @Transactional
    public PollResponse createPoll(Long courseId, CreatePollRequest request) {
        contextService.verifyCourseOwnership(courseId);
        User currentUser = contextService.getCurrentUser();
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));

        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        if (!channel.getCommunity().getId().equals(community.getId())) {
            throw new ForbiddenException("Channel does not belong to this community");
        }

        Poll poll = new Poll()
                .setChannel(channel)
                .setCreatedBy(currentUser)
                .setQuestion(request.getQuestion())
                .setEndsAt(request.getEndsAt())
                .setClosed(false)
                .setDeleted(false);
        poll = pollRepository.save(poll);

        List<PollOption> options = new ArrayList<>();
        for (String text : request.getOptions()) {
            PollOption option = new PollOption()
                    .setPoll(poll)
                    .setText(text)
                    .setDeleted(false);
            options.add(optionRepository.save(option));
        }

        return buildPollResponse(poll, options, null);
    }

    @Transactional
    public List<PollResponse> getPolls(Long courseId, Long channelId) {
        User currentUser = contextService.getCurrentUser();
        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));
        verifyMember(community.getId(), currentUser.getId());

        List<Poll> polls = pollRepository.findByChannelIdAndDeletedFalseOrderByCreatedAtDesc(channelId);
        return polls.stream()
                .map(p -> {
                    List<PollOption> options = optionRepository.findByPollIdAndDeletedFalse(p.getId());
                    var existingVote = voteRepository.findByPollIdAndUserId(p.getId(), currentUser.getId());
                    return buildPollResponse(p, options, existingVote.map(PollVote::getOption).orElse(null));
                })
                .toList();
    }

    @Transactional
    public PollResponse vote(Long courseId, Long pollId, Long optionId) {
        User currentUser = contextService.getCurrentUser();
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));
        verifyMember(community.getId(), currentUser.getId());

        if (!poll.getChannel().getCommunity().getId().equals(community.getId())) {
            throw new ForbiddenException("Poll does not belong to this course");
        }

        if (poll.isClosed()) {
            throw new ForbiddenException("Poll is closed");
        }

        var existingVote = voteRepository.findByPollIdAndUserId(pollId, currentUser.getId());
        if (existingVote.isPresent()) {
            voteRepository.delete(existingVote.get());
        }

        PollOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));

        PollVote vote = new PollVote()
                .setPoll(poll)
                .setOption(option)
                .setUser(currentUser);
        voteRepository.save(vote);

        List<PollOption> options = optionRepository.findByPollIdAndDeletedFalse(poll.getId());
        return buildPollResponse(poll, options, option);
    }

    @Transactional
    public PollResponse closePoll(Long courseId, Long pollId) {
        contextService.verifyCourseOwnership(courseId);
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));

        Community community = communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));
        if (!poll.getChannel().getCommunity().getId().equals(community.getId())) {
            throw new ForbiddenException("Poll does not belong to this course");
        }

        poll.setClosed(true);
        poll = pollRepository.save(poll);

        List<PollOption> options = optionRepository.findByPollIdAndDeletedFalse(poll.getId());
        return buildPollResponse(poll, options, null);
    }

    private PollResponse buildPollResponse(Poll poll, List<PollOption> options, PollOption votedOption) {
        List<PollOptionResponse> optionResponses = options.stream()
                .map(o -> PollOptionResponse.builder()
                        .id(o.getId())
                        .text(o.getText())
                        .voteCount(voteRepository.countByOptionId(o.getId()))
                        .build())
                .toList();

        return PollResponse.builder()
                .id(poll.getId())
                .channelId(poll.getChannel().getId())
                .question(poll.getQuestion())
                .createdBy(poll.getCreatedBy().getId())
                .endsAt(poll.getEndsAt())
                .closed(poll.isClosed())
                .options(optionResponses)
                .voted(votedOption != null)
                .createdAt(poll.getCreatedAt())
                .build();
    }

    private void verifyMember(Long communityId, Long userId) {
        if (!memberRepository.existsByCommunityIdAndUserId(communityId, userId)) {
            throw new ForbiddenException("You are not a member of this community");
        }
    }
}
