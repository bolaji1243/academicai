package com.schoolproject.app.service.student.university;

import com.schoolproject.app.dto.response.StudentAssignmentResponse;
import com.schoolproject.app.dto.response.UniversityDashboardResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.repository.AnnouncementRepository;
import com.schoolproject.app.lecturer.repository.AssignmentRepository;
import com.schoolproject.app.lecturer.repository.AssignmentSubmissionRepository;
import com.schoolproject.app.lecturer.repository.AttendanceRecordRepository;
import com.schoolproject.app.lecturer.repository.AttendanceSessionRepository;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityDashboardService {

    private final UniversityStudentContextService contextService;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AnnouncementRepository announcementRepository;

    public UniversityDashboardResponse getDashboard() {
        User student = contextService.getCurrentStudent();
        var enrollments = enrollmentRepository.findByStudent(student);
        List<Course> courses = enrollments.stream().map(enrollment -> enrollment.getCourse()).toList();
        LocalDateTime weekStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekEnd = LocalDate.now().plusDays(7).atTime(LocalTime.MAX);

        var dueThisWeek = courses.isEmpty()
                ? List.<com.schoolproject.app.lecturer.entity.Assignment>of()
                : assignmentRepository.findByCourseInAndDeadlineBetween(courses, weekStart, weekEnd);
        long totalSessions = courses.stream().mapToLong(sessionRepository::countByCourse).sum();
        long attendedSessions = courses.stream().mapToLong(course -> recordRepository.countBySessionCourseAndStudent(course, student)).sum();
        double attendancePercentage = totalSessions == 0 ? 0 : (attendedSessions * 100.0) / totalSessions;
        long unreadAnnouncements = courses.isEmpty()
                ? 0
                : announcementRepository.countByCourseInAndCreatedAtAfter(courses, weekStart.minusDays(7));

        return UniversityDashboardResponse.builder()
                .enrolledCoursesCount(enrollments.size())
                .assignmentsDueThisWeek(dueThisWeek.size())
                .overallAttendancePercentage(attendancePercentage)
                .unreadAnnouncementsCount(unreadAnnouncements)
                .upcomingTests(dueThisWeek.stream()
                        .map(assignment -> StudentAssignmentResponse.from(
                                assignment,
                                submissionRepository.findByAssignmentAndStudent(assignment, student).orElse(null)))
                        .toList())
                .build();
    }
}
