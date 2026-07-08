package com.schoolproject.app.service.student.university;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.exception.ForbiddenException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.AssignmentRepository;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import com.schoolproject.app.lecturer.repository.CourseMaterialRepository;
import com.schoolproject.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityStudentContextService {

    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseMaterialRepository materialRepository;
    private final AssignmentRepository assignmentRepository;

    public User getCurrentStudent() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    public Course verifyEnrollment(Long courseId) {
        User student = getCurrentStudent();
        return enrollmentRepository.findByStudentAndCourseId(student, courseId)
                .map(enrollment -> enrollment.getCourse())
                .orElseThrow(() -> new ForbiddenException("You are not enrolled in this course"));
    }

    public CourseMaterial getEnrolledMaterial(Long materialId) {
        CourseMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        verifyEnrollment(material.getCourse().getId());
        return material;
    }

    public Assignment getEnrolledAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        verifyEnrollment(assignment.getCourse().getId());
        return assignment;
    }
}
