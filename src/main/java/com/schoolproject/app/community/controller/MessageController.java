package com.schoolproject.app.community.controller;

import com.schoolproject.app.community.dto.request.EditMessageRequest;
import com.schoolproject.app.community.dto.request.ReactRequest;
import com.schoolproject.app.community.dto.request.SendMessageRequest;
import com.schoolproject.app.community.dto.response.MessageResponse;
import com.schoolproject.app.community.service.MessageService;
import com.schoolproject.app.lecturer.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses/{courseId}/channels/{channelId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long courseId,
            @PathVariable Long channelId,
            SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(
                courseId, channelId, request.getContent(), request.getReplyToId(), request.getAttachments());
        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable Long courseId,
            @PathVariable Long channelId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved",
                messageService.getMessages(courseId, channelId, pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> searchMessages(
            @PathVariable Long courseId,
            @PathVariable Long channelId,
            @RequestParam String q,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Search results",
                messageService.searchMessages(courseId, channelId, q, pageable)));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long courseId,
            @PathVariable Long messageId,
            @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Message edited",
                messageService.editMessage(courseId, messageId, request.getContent())));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long courseId, @PathVariable Long messageId) {
        messageService.deleteMessage(courseId, messageId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/{messageId}/pin")
    public ResponseEntity<ApiResponse<Void>> togglePin(
            @PathVariable Long courseId, @PathVariable Long messageId) {
        messageService.togglePin(courseId, messageId);
        return ResponseEntity.ok(ApiResponse.success("Pin toggled", null));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getPinnedMessages(
            @PathVariable Long courseId, @PathVariable Long channelId) {
        return ResponseEntity.ok(ApiResponse.success("Pinned messages retrieved",
                messageService.getPinnedMessages(courseId, channelId)));
    }

    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> toggleReaction(
            @PathVariable Long courseId,
            @PathVariable Long messageId,
            @RequestBody ReactRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reaction toggled",
                messageService.toggleReaction(courseId, messageId, request.getEmoji())));
    }
}
