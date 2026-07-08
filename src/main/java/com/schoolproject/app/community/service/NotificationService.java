package com.schoolproject.app.community.service;

import com.schoolproject.app.community.dto.response.NotificationResponse;
import com.schoolproject.app.community.entity.Notification;
import com.schoolproject.app.community.entity.NotificationType;
import com.schoolproject.app.community.repository.NotificationRepository;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.service.LecturerContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final LecturerContextService contextService;

    @Transactional
    public void createNotification(User user, User sender, NotificationType type,
                                    String title, String body, String resourceId) {
        Notification notification = new Notification()
                .setUser(user)
                .setSender(sender)
                .setType(type)
                .setTitle(title)
                .setBody(body)
                .setResourceId(resourceId)
                .setRead(false);
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        User currentUser = contextService.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(this::toResponse);
    }

    public long getUnreadCount() {
        User currentUser = contextService.getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(currentUser.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = contextService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = contextService.getCurrentUser();
        Page<Notification> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), Pageable.unpaged());
        page.getContent().forEach(n -> n.setRead(true));
        notificationRepository.saveAll(page.getContent());
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .body(n.getBody())
                .senderName(n.getSender() != null ? n.getSender().getFullName() : null)
                .resourceId(n.getResourceId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
