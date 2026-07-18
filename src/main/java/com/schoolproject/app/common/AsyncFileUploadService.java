package com.schoolproject.app.common;

import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import com.schoolproject.app.lecturer.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncFileUploadService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".txt", ".zip", ".rar"
    );

    private final FileStorageService fileStorageService;
    private final AssignmentSubmissionRepository submissionRepository;

    @Async
    public void uploadAndUpdateSubmission(AssignmentSubmission submission, Long assignmentId, MultipartFile file) {
        try {
            String fileUrl = fileStorageService.save("submissions/" + assignmentId, file);
            submission.setFileUrl(fileUrl);
            submissionRepository.save(submission);
            log.info("Successfully uploaded file for submission {}", submission.getId());
        } catch (Exception e) {
            log.error("Failed to upload file for submission {}", submission.getId(), e);
            submission.setStatus(SubmissionStatus.PENDING);
            submissionRepository.save(submission);
        }
    }
}
