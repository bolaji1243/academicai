package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Announcement;
import com.schoolproject.app.lecturer.entity.AttendanceSession;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.exception.ForbiddenException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.AnnouncementRepository;
import com.schoolproject.app.lecturer.repository.AttendanceSessionRepository;
import com.schoolproject.app.lecturer.repository.CourseRepository;
import com.schoolproject.app.lecturer.util.SecurityUtil;
import com.schoolproject.app.repository.LecturerProfileRepository;
import com.schoolproject.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LecturerContextService {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final CourseRepository courseRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AnnouncementRepository announcementRepository;

    public User getCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new ForbiddenException("No authenticated user found");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public LecturerProfile getCurrentLecturer() {
        User user = getCurrentUser();
        LecturerProfile profile = user.getLecturerProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Lecturer profile not found");
        }
        return profile;
    }

    public Course verifyCourseOwnership(Long courseId) {
        LecturerProfile lecturer = getCurrentLecturer();
        return courseRepository.findByIdAndLecturer(courseId, lecturer)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found or you do not own this course"));
    }

    public AttendanceSession verifySessionOwnership(Long sessionId) {
        getCurrentLecturer();
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));
        verifyCourseOwnership(session.getCourse().getId());
        return session;
    }

    public Announcement verifyAnnouncementOwnership(Long announcementId) {
        getCurrentLecturer();
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));
        if (announcement.getCourse() != null) {
            verifyCourseOwnership(announcement.getCourse().getId());
        }
        return announcement;
    }
}
