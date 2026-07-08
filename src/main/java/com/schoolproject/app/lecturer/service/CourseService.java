package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.community.entity.Community;
import com.schoolproject.app.community.service.CommunityService;
import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.dto.request.CreateCourseRequest;
import com.schoolproject.app.lecturer.dto.request.UpdateCourseRequest;
import com.schoolproject.app.lecturer.dto.response.CourseResponse;
import com.schoolproject.app.lecturer.dto.response.CourseStudentResponse;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseEnrollment;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import com.schoolproject.app.lecturer.repository.CourseRepository;
import com.schoolproject.app.lecturer.util.JoinCodeGenerator;
import com.schoolproject.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final LecturerContextService contextService;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final JoinCodeGenerator joinCodeGenerator;
    private final UserRepository userRepository;
    private final CommunityService communityService;

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        LecturerProfile lecturer = contextService.getCurrentLecturer();

        String joinCode;
        do {
            joinCode = joinCodeGenerator.generate();
        } while (courseRepository.existsByJoinCode(joinCode));

        Course course = new Course()
                .setTitle(request.getTitle())
                .setCourseCode(request.getCourseCode())
                .setDescription(request.getDescription())
                .setSchedule(request.getSchedule())
                .setJoinCode(joinCode)
                .setLecturer(lecturer)
                .setArchived(false);

        course = courseRepository.save(course);

        Community community = communityService.createCommunity(course.getId(), course.getTitle());
        communityService.addMember(community.getId(), lecturer.getUser(), "LECTURER");

        return CourseResponse.from(course, 0, lecturer.getUser().getFullName());
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getMyCourses(Pageable pageable) {
        LecturerProfile lecturer = contextService.getCurrentLecturer();
        Page<Course> courses = courseRepository.findAllByLecturerAndArchivedFalse(lecturer, pageable);
        String lecturerName = lecturer.getUser().getFullName();
        return courses.map(course -> {
            long count = enrollmentRepository.countByCourse(course);
            return CourseResponse.from(course, count, lecturerName);
        });
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long courseId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        long count = enrollmentRepository.countByCourse(course);
        String lecturerName = course.getLecturer().getUser().getFullName();
        return CourseResponse.from(course, count, lecturerName);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request) {
        Course course = contextService.verifyCourseOwnership(courseId);

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }
        if (request.getCourseCode() != null) {
            course.setCourseCode(request.getCourseCode());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getSchedule() != null) {
            course.setSchedule(request.getSchedule());
        }

        course = courseRepository.save(course);
        long count = enrollmentRepository.countByCourse(course);
        String lecturerName = course.getLecturer().getUser().getFullName();
        return CourseResponse.from(course, count, lecturerName);
    }

    @Transactional
    public CourseResponse archiveCourse(Long courseId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        course.setArchived(true);
        course = courseRepository.save(course);
        long count = enrollmentRepository.countByCourse(course);
        String lecturerName = course.getLecturer().getUser().getFullName();
        return CourseResponse.from(course, count, lecturerName);
    }

    @Transactional(readOnly = true)
    public List<CourseStudentResponse> getEnrolledStudents(Long courseId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
        return enrollments.stream()
                .map(CourseStudentResponse::from)
                .toList();
    }

    @Transactional
    public void removeStudent(Long courseId, String studentPublicId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        User student = userRepository.findByPublicId(studentPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        CourseEnrollment enrollment = enrollmentRepository.findByCourseAndStudent(course, student)
                .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in this course"));
        enrollmentRepository.delete(enrollment);
    }
}
