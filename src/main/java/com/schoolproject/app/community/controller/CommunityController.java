package com.schoolproject.app.community.controller;

import com.schoolproject.app.community.dto.request.CreateChannelRequest;
import com.schoolproject.app.community.dto.response.ChannelResponse;
import com.schoolproject.app.community.dto.response.CommunityResponse;
import com.schoolproject.app.community.dto.response.MemberResponse;
import com.schoolproject.app.community.service.CommunityService;
import com.schoolproject.app.lecturer.dto.ApiResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/courses/{courseId}/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<ApiResponse<CommunityResponse>> getCommunity(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success("Community retrieved", communityService.getCommunity(courseId)));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/channels")
    public ResponseEntity<ApiResponse<ChannelResponse>> createChannel(
            @PathVariable Long courseId, @Valid @RequestBody CreateChannelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Channel created", communityService.createChannel(courseId, request)));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success("Members retrieved", communityService.getMembers(courseId)));
    }

    @PutMapping("/mute")
    public ResponseEntity<ApiResponse<Void>> toggleMute(@PathVariable Long courseId) {
        communityService.toggleMute(courseId);
        return ResponseEntity.ok(ApiResponse.success("Mute toggled", null));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/channels/{channelId}/lock")
    public ResponseEntity<ApiResponse<Void>> lockChannel(
            @PathVariable Long courseId, @PathVariable Long channelId) {
        communityService.lockChannel(courseId, channelId, true);
        return ResponseEntity.ok(ApiResponse.success("Channel locked", null));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/channels/{channelId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockChannel(
            @PathVariable Long courseId, @PathVariable Long channelId) {
        communityService.lockChannel(courseId, channelId, false);
        return ResponseEntity.ok(ApiResponse.success("Channel unlocked", null));
    }
}
