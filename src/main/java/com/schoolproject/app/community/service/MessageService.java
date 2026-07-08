package com.schoolproject.app.community.service;

import com.schoolproject.app.common.FileStorageService;
import com.schoolproject.app.community.dto.response.AttachmentResponse;
import com.schoolproject.app.community.dto.response.AuthorResponse;
import com.schoolproject.app.community.dto.response.MessageResponse;
import com.schoolproject.app.community.entity.Channel;
import com.schoolproject.app.community.entity.Community;
import com.schoolproject.app.community.entity.CommunityMember;
import com.schoolproject.app.community.entity.Message;
import com.schoolproject.app.community.entity.MessageAttachment;
import com.schoolproject.app.community.entity.MessageReaction;
import com.schoolproject.app.community.entity.NotificationType;
import com.schoolproject.app.community.repository.ChannelRepository;
import com.schoolproject.app.community.repository.CommunityMemberRepository;
import com.schoolproject.app.community.repository.CommunityRepository;
import com.schoolproject.app.community.repository.MessageAttachmentRepository;
import com.schoolproject.app.community.repository.MessageReactionRepository;
import com.schoolproject.app.community.repository.MessageRepository;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.exception.ForbiddenException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.service.LecturerContextService;
import com.schoolproject.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S+)");

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final MessageReactionRepository reactionRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final LecturerContextService contextService;

    @Value("${app.upload.max-file-size:10485760}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".ppt", ".pptx",
            ".xls", ".xlsx", ".txt", ".zip", ".mp4", ".mp3", ".webm"
    );

    @Transactional
    public MessageResponse sendMessage(Long courseId, Long channelId, String content, Long replyToId,
                                        List<MultipartFile> attachments) {
        User currentUser = contextService.getCurrentUser();
        Community community = getCommunity(courseId);
        Channel channel = getChannelInCommunity(channelId, community);
        verifyMember(community.getId(), currentUser.getId());

        if (channel.isLocked()) {
            throw new ForbiddenException("Channel is locked");
        }

        Message replyTo = null;
        if (replyToId != null) {
            replyTo = messageRepository.findById(replyToId)
                    .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        }

        String parsedContent = parseMentions(content, community.getId());

        Message message = new Message()
                .setChannel(channel)
                .setAuthor(currentUser)
                .setContent(parsedContent)
                .setReplyTo(replyTo)
                .setPinned(false)
                .setDeleted(false);
        message = messageRepository.save(message);

        List<AttachmentResponse> attachmentResponses = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                fileStorageService.validate(file, maxFileSize, ALLOWED_EXTENSIONS);
                String relativePath = fileStorageService.save("community/" + channelId, file);
                MessageAttachment attachment = new MessageAttachment()
                        .setMessage(message)
                        .setFileName(file.getOriginalFilename())
                        .setFileType(fileStorageService.getExtension(file.getOriginalFilename()))
                        .setFileSize(file.getSize())
                        .setFilePath(relativePath);
                attachment = attachmentRepository.save(attachment);
                attachmentResponses.add(AttachmentResponse.builder()
                        .id(attachment.getId())
                        .fileName(attachment.getFileName())
                        .fileType(attachment.getFileType())
                        .fileSize(attachment.getFileSize())
                        .filePath(attachment.getFilePath())
                        .build());
            }
        }

        notifyMentionedUsers(parsedContent, community.getId(), currentUser, message.getId());

        return buildMessageResponse(message, attachmentResponses);
    }

    public Page<MessageResponse> getMessages(Long courseId, Long channelId, Pageable pageable) {
        User currentUser = contextService.getCurrentUser();
        Community community = getCommunity(courseId);
        getChannelInCommunity(channelId, community);
        verifyMember(community.getId(), currentUser.getId());

        Page<Message> messages = messageRepository
                .findByChannelIdAndDeletedFalseOrderByCreatedAtDesc(channelId, pageable);

        return messages.map(this::buildFullMessageResponse);
    }

    @Transactional
    public MessageResponse editMessage(Long courseId, Long messageId, String newContent) {
        User currentUser = contextService.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own messages");
        }

        Community community = getCommunity(courseId);

        String parsedContent = parseMentions(newContent, community.getId());
        message.setContent(parsedContent);
        message.setEditedAt(LocalDateTime.now());
        message = messageRepository.save(message);

        return buildFullMessageResponse(message);
    }

    @Transactional
    public void deleteMessage(Long courseId, Long messageId) {
        User currentUser = contextService.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        boolean isAuthor = message.getAuthor().getId().equals(currentUser.getId());

        if (isAuthor) {
            message.setDeleted(true);
            messageRepository.save(message);
            return;
        }

        contextService.verifyCourseOwnership(courseId);
        message.setDeleted(true);
        messageRepository.save(message);
    }

    public Page<MessageResponse> searchMessages(Long courseId, Long channelId, String query, Pageable pageable) {
        User currentUser = contextService.getCurrentUser();
        Community community = getCommunity(courseId);
        getChannelInCommunity(channelId, community);
        verifyMember(community.getId(), currentUser.getId());

        String searchQuery = query.replaceAll("[^a-zA-Z0-9@._\\-]", " ") + "*";
        Page<Message> messages = messageRepository.searchByContent(channelId, searchQuery, pageable);
        return messages.map(this::buildFullMessageResponse);
    }

    @Transactional
    public void togglePin(Long courseId, Long messageId) {
        contextService.verifyCourseOwnership(courseId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        message.setPinned(!message.isPinned());
        messageRepository.save(message);
    }

    public List<MessageResponse> getPinnedMessages(Long courseId, Long channelId) {
        User currentUser = contextService.getCurrentUser();
        Community community = getCommunity(courseId);
        getChannelInCommunity(channelId, community);
        verifyMember(community.getId(), currentUser.getId());

        List<Message> messages = messageRepository.findByChannelIdAndPinnedTrueAndDeletedFalse(channelId);
        return messages.stream().map(this::buildFullMessageResponse).toList();
    }

    @Transactional
    public Map<String, Integer> toggleReaction(Long courseId, Long messageId, String emoji) {
        User currentUser = contextService.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        Community community = getCommunity(courseId);
        verifyMember(community.getId(), currentUser.getId());

        var existing = reactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, currentUser.getId(), emoji);
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
        } else {
            MessageReaction reaction = new MessageReaction()
                    .setMessage(message)
                    .setUser(currentUser)
                    .setEmoji(emoji);
            reactionRepository.save(reaction);
        }

        return getReactionSummary(messageId);
    }

    private Map<String, Integer> getReactionSummary(Long messageId) {
        List<MessageReaction> reactions = reactionRepository.findByMessageId(messageId);
        Map<String, Integer> summary = new HashMap<>();
        for (MessageReaction r : reactions) {
            summary.merge(r.getEmoji(), 1, Integer::sum);
        }
        return summary;
    }

    private String parseMentions(String content, Long communityId) {
        if (content == null) return null;
        Matcher matcher = MENTION_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String mentionName = matcher.group(1);
            memberRepository.findByCommunityId(communityId).stream()
                    .map(m -> m.getUser())
                    .filter(u -> u.getFullName().equalsIgnoreCase(mentionName))
                    .findFirst()
                    .ifPresentOrElse(
                            u -> matcher.appendReplacement(sb, "<@" + u.getPublicId() + ">"),
                            () -> matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)))
                    );
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void notifyMentionedUsers(String content, Long communityId, User sender, Long messageId) {
        if (content == null) return;
        Matcher matcher = Pattern.compile("<@(\\S+?)>").matcher(content);
        Set<String> mentioned = new HashSet<>();
        while (matcher.find()) {
            mentioned.add(matcher.group(1));
        }
        if (mentioned.isEmpty()) return;

        memberRepository.findByCommunityId(communityId).stream()
                .map(CommunityMember::getUser)
                .filter(u -> mentioned.contains(u.getPublicId()) && !u.getId().equals(sender.getId()))
                .forEach(u -> notificationService.createNotification(
                        u, sender, NotificationType.MENTION,
                        "You were mentioned",
                        sender.getFullName() + " mentioned you in a message",
                        String.valueOf(messageId)
                ));
    }

    private Community getCommunity(Long courseId) {
        return communityRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found for course"));
    }

    private Channel getChannelInCommunity(Long channelId, Community community) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));
        if (!channel.getCommunity().getId().equals(community.getId())) {
            throw new ForbiddenException("Channel does not belong to this community");
        }
        return channel;
    }

    private void verifyMember(Long communityId, Long userId) {
        if (!memberRepository.existsByCommunityIdAndUserId(communityId, userId)) {
            throw new ForbiddenException("You are not a member of this community");
        }
    }

    private MessageResponse buildMessageResponse(Message message, List<AttachmentResponse> attachmentResponses) {
        Map<String, Integer> reactions = getReactionSummary(message.getId());
        return MessageResponse.builder()
                .id(message.getId())
                .channelId(message.getChannel().getId())
                .author(AuthorResponse.builder()
                        .publicId(message.getAuthor().getPublicId())
                        .fullName(message.getAuthor().getFullName())
                        .build())
                .content(message.getContent())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .pinned(message.isPinned())
                .editedAt(message.getEditedAt())
                .attachments(attachmentResponses)
                .reactions(reactions)
                .createdAt(message.getCreatedAt())
                .build();
    }

    private MessageResponse buildFullMessageResponse(Message message) {
        List<AttachmentResponse> attachmentResponses = attachmentRepository.findByMessageId(message.getId())
                .stream()
                .map(a -> AttachmentResponse.builder()
                        .id(a.getId())
                        .fileName(a.getFileName())
                        .fileType(a.getFileType())
                        .fileSize(a.getFileSize())
                        .filePath(a.getFilePath())
                        .build())
                .toList();
        return buildMessageResponse(message, attachmentResponses);
    }
}
