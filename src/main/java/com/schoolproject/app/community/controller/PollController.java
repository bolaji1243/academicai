package com.schoolproject.app.community.controller;

import com.schoolproject.app.community.dto.request.CreatePollRequest;
import com.schoolproject.app.community.dto.request.VotePollRequest;
import com.schoolproject.app.community.dto.response.PollResponse;
import com.schoolproject.app.community.service.PollService;
import com.schoolproject.app.lecturer.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/channels/{channelId}/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping
    public ResponseEntity<ApiResponse<PollResponse>> createPoll(
            @PathVariable Long courseId,
            @PathVariable Long channelId,
            @RequestBody CreatePollRequest request) {
        request.setChannelId(channelId);
        return ResponseEntity.ok(ApiResponse.success("Poll created",
                pollService.createPoll(courseId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PollResponse>>> getPolls(
            @PathVariable Long courseId, @PathVariable Long channelId) {
        return ResponseEntity.ok(ApiResponse.success("Polls retrieved",
                pollService.getPolls(courseId, channelId)));
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<ApiResponse<PollResponse>> vote(
            @PathVariable Long courseId,
            @PathVariable Long pollId,
            @RequestBody VotePollRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vote cast",
                pollService.vote(courseId, pollId, request.getOptionId())));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/{pollId}/close")
    public ResponseEntity<ApiResponse<PollResponse>> closePoll(
            @PathVariable Long courseId, @PathVariable Long pollId) {
        return ResponseEntity.ok(ApiResponse.success("Poll closed",
                pollService.closePoll(courseId, pollId)));
    }
}
