package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.lecturer.dto.request.CreateAssignmentRequest;
import com.schoolproject.app.lecturer.dto.request.GradeSubmissionRequest;
import com.schoolproject.app.lecturer.dto.response.AssignmentResponse;
import com.schoolproject.app.lecturer.dto.response.SubmissionResponse;
import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.enums.SubmissionStatus;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.AssignmentRepository;
import com.schoolproject.app.lecturer.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final LecturerContextService contextService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    @Transactional
    public AssignmentResponse createAssignment(Long courseId, CreateAssignmentRequest request) {
        Course course = contextService.verifyCourseOwnership(courseId);

        Assignment assignment = new Assignment()
                .setCourse(course)
                .setTitle(request.getTitle())
                .setInstructions(request.getInstructions())
                .setDeadline(request.getDeadline())
                .setMaxScore(request.getMaxScore());

        assignment = assignmentRepository.save(assignment);
        return AssignmentResponse.from(assignment, 0, 0, course.getTitle());
    }

    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getAssignments(Long courseId, Pageable pageable) {
        Course course = contextService.verifyCourseOwnership(courseId);
        Page<Assignment> assignments = assignmentRepository.findByCourse(course, pageable);
        return assignments.map(a -> {
            long submitted = submissionRepository.countByAssignmentAndStatus(a, SubmissionStatus.SUBMITTED);
            long pending = submissionRepository.countByAssignmentAndStatus(a, SubmissionStatus.PENDING);
            return AssignmentResponse.from(a, submitted, pending, course.getTitle());
        });
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getSubmissions(Long assignmentId, Pageable pageable) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        contextService.verifyCourseOwnership(assignment.getCourse().getId());
        return submissionRepository.findByAssignment(assignment, pageable)
                .map(SubmissionResponse::from);
    }

    @Transactional
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request) {
        var submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        contextService.verifyCourseOwnership(submission.getAssignment().getCourse().getId());

        if (request.getScore() > submission.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException(
                    "Score cannot exceed maximum score of " + submission.getAssignment().getMaxScore());
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission = submissionRepository.save(submission);
        return SubmissionResponse.from(submission);
    }
}
