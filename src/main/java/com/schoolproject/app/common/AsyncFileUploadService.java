package com.schoolproject.app.common;

import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import com.schoolproject.app.lecturer.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void uploadAndUpdateSubmission(Long submissionId, Long assignmentId,
                                           byte[] fileBytes, String originalFilename) {
        try {
            AssignmentSubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Submission not found: " + submissionId));
            String fileUrl = fileStorageService.save(
                    "submissions/" + assignmentId, fileBytes, originalFilename);
            submission.setFileUrl(fileUrl);
            submissionRepository.save(submission);
            log.info("Successfully uploaded file for submission {}", submissionId);
        } catch (Exception e) {
            log.error("Failed to upload file for submission {}", submissionId, e);
            submissionRepository.findById(submissionId).ifPresent(submission -> {
                submission.setStatus(SubmissionStatus.PENDING);
                submissionRepository.save(submission);
            });
        }
    }
}
