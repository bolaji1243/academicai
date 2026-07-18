package com.schoolproject.app.community.controller;

import com.schoolproject.app.community.dto.request.StompMessageRequest;
import com.schoolproject.app.community.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompMessageController {

    private final MessageService messageService;

    @MessageMapping("/channels/{channelId}/send")
    public void sendMessage(
            @DestinationVariable Long channelId,
            @Payload StompMessageRequest request,
            Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated WebSocket message attempt to channel {}", channelId);
            return;
        }
        try {
            Long courseId = messageService.getCourseIdForChannel(channelId);
            messageService.sendMessage(
                    courseId, channelId, request.getContent(), request.getReplyToId(), null);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to channel {}: {}", channelId, e.getMessage());
        }
    }
}
