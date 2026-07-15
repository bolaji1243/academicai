package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.common.FileStorageService;
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

import java.time.LocalDateTime;
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
        String fileUrl = fileStorageService.save("submissions/" + assignmentId, file);

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
}
