package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.common.AsyncFileUploadService;
import com.schoolproject.app.common.FileStorageService;
import com.schoolproject.app.dto.response.StudentAssignmentResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.AssignmentSubmission;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.AssignmentRepository;
import com.schoolproject.app.lecturer.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentAssignmentService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".txt", ".zip", ".rar"
    );

    private final StudentContextService contextService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final FileStorageService fileStorageService;
    private final AsyncFileUploadService asyncFileUploadService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<StudentAssignmentResponse> getCourseAssignments(Long courseId) {
        User student = contextService.getCurrentStudent();
        Course course = contextService.verifyEnrollment(courseId);
        List<Assignment> assignments = assignmentRepository.findByCourseOrderByDeadlineAsc(course);

        Map<Long, AssignmentSubmission> submissionMap = submissionRepository
                .findByAssignmentInAndStudent(assignments, student)
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getAssignment().getId(),
                        Function.identity()));

        return assignments.stream()
                .map(a -> StudentAssignmentResponse.from(a, submissionMap.get(a.getId())))
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
        fileStorageService.validate(file, MAX_FILE_SIZE, ALLOWED_EXTENSIONS);

        User student = contextService.getCurrentStudent();
        Assignment assignment = contextService.getEnrolledAssignment(assignmentId);
        LocalDateTime now = LocalDateTime.now();

        AssignmentSubmission submission = submissionRepository.findByAssignmentAndStudent(assignment, student)
                .orElseGet(() -> new AssignmentSubmission()
                        .setAssignment(assignment)
                        .setStudent(student));

        submission.setSubmittedAt(now);
        submission.setStatus(now.isAfter(assignment.getDeadline())
                ? SubmissionStatus.LATE
                : SubmissionStatus.SUBMITTED);

        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        final byte[] fileBytes;
        try {
            // Read the request-backed multipart data before returning to the client.
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read uploaded file", e);
        }
        asyncFileUploadService.uploadAndUpdateSubmission(
                savedSubmission.getId(), assignmentId, fileBytes, file.getOriginalFilename());

        return StudentAssignmentResponse.from(assignment, savedSubmission);
    }

    @Transactional(readOnly = true)
    public String getAssignmentQuestionFileUrl(Long assignmentId) {
        Assignment assignment = contextService.getEnrolledAssignment(assignmentId);
        if (assignment.getQuestionFileUrl() == null || assignment.getQuestionFileUrl().isBlank()) {
            throw new ResourceNotFoundException("No question file attached to this assignment");
        }
        return assignment.getQuestionFileUrl();
    }
}
