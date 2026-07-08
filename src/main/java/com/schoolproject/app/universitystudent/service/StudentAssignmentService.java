package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.dto.response.StudentAssignmentResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import com.schoolproject.app.lecturer.repository.AssignmentRepository;
import com.schoolproject.app.lecturer.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentAssignmentService {

    private final StudentContextService contextService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<StudentAssignmentResponse> getCourseAssignments(Long courseId) {
        User student = contextService.getCurrentStudent();
        Course course = contextService.verifyEnrollment(courseId);
        return assignmentRepository.findByCourseOrderByDeadlineAsc(course).stream()
                .map(assignment -> StudentAssignmentResponse.from(
                        assignment,
                        submissionRepository.findByAssignmentAndStudent(assignment, student).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentAssignmentResponse getAssignment(Long assignmentId) {
        User student = contextService.getCurrentStudent();
        Assignment assignment = contextService.getEnrolledAssignment(assignmentId);
        AssignmentSubmission submission = submissionRepository.findByAssignmentAndStudent(assignment, student)
                .orElse(null);
        return StudentAssignmentResponse.from(assignment, submission);
    }

    @Transactional
    public StudentAssignmentResponse submitAssignment(Long assignmentId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        User student = contextService.getCurrentStudent();
        Assignment assignment = contextService.getEnrolledAssignment(assignmentId);
        LocalDateTime now = LocalDateTime.now();
        String fileUrl = saveFile(assignmentId, file);

        AssignmentSubmission submission = submissionRepository.findByAssignmentAndStudent(assignment, student)
                .orElseGet(() -> new AssignmentSubmission()
                        .setAssignment(assignment)
                        .setStudent(student));

        submission.setFileUrl(fileUrl);
        submission.setSubmittedAt(now);
        submission.setStatus(now.isAfter(assignment.getDeadline())
                ? SubmissionStatus.LATE
                : SubmissionStatus.SUBMITTED);

        return StudentAssignmentResponse.from(assignment, submissionRepository.save(submission));
    }

    private String saveFile(Long assignmentId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String relativePath = "submissions/" + assignmentId + "/" + UUID.randomUUID() + extension;
            Path fullPath = resolveStoredPath(relativePath);
            Files.createDirectories(fullPath.getParent());
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store submission file", e);
        }
    }

    private Path resolveStoredPath(String relativePath) {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(relativePath).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return filePath;
    }
}
