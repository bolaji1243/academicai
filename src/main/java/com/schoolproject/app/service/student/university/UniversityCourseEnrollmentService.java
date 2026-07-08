package com.schoolproject.app.service.student.university;

import com.schoolproject.app.community.repository.CommunityRepository;
import com.schoolproject.app.community.service.CommunityService;
import com.schoolproject.app.dto.response.UniversityCourseResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseEnrollment;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import com.schoolproject.app.lecturer.repository.CourseMaterialRepository;
import com.schoolproject.app.lecturer.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityCourseEnrollmentService {

    private final UniversityStudentContextService contextService;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseMaterialRepository materialRepository;
    private final CommunityService communityService;
    private final CommunityRepository communityRepository;

    @Transactional
    public UniversityCourseResponse joinCourse(String joinCode) {
        User student = contextService.getCurrentStudent();
        Course course = courseRepository.findByJoinCodeAndArchivedFalse(joinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (enrollmentRepository.existsByCourseAndStudent(course, student)) {
            throw new ConflictException("You have already joined this course");
        }

        CourseEnrollment enrollment = new CourseEnrollment()
                .setStudent(student)
                .setCourse(course)
                .setEnrolledAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        communityRepository.findByCourseId(course.getId()).ifPresent(community ->
                communityService.addMember(community.getId(), student, "UNIVERSITY_STUDENT"));

        return UniversityCourseResponse.from(course, materialRepository.countByCourse(course));
    }

    public List<UniversityCourseResponse> getEnrolledCourses() {
        User student = contextService.getCurrentStudent();
        return enrollmentRepository.findByStudent(student).stream()
                .map(enrollment -> {
                    Course course = enrollment.getCourse();
                    return UniversityCourseResponse.from(course, materialRepository.countByCourse(course));
                })
                .toList();
    }

    public UniversityCourseResponse getCourse(Long id) {
        Course course = contextService.verifyEnrollment(id);
        return UniversityCourseResponse.from(course, materialRepository.countByCourse(course));
    }

    @Transactional
    public void leaveCourse(Long id) {
        User student = contextService.getCurrentStudent();
        Course course = contextService.verifyEnrollment(id);
        enrollmentRepository.deleteByCourseAndStudent(course, student);
    }
}
