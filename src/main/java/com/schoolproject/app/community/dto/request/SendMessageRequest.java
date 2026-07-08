package com.schoolproject.app.community.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class SendMessageRequest {
    private String content;
    private Long replyToId;
    private List<MultipartFile> attachments;
}
