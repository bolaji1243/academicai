package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.lecturer.dto.response.AnnouncementSummaryResponse;
import com.schoolproject.app.lecturer.dto.response.AttentionItemResponse;
import com.schoolproject.app.lecturer.dto.response.CourseSummaryResponse;
import com.schoolproject.app.lecturer.dto.response.DashboardResponse;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final LecturerContextService contextService;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseMaterialRepository materialRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final ResultRepository resultRepository;
    private final AnnouncementRepository announcementRepository;

    public DashboardResponse getDashboard() {
        LecturerProfile lecturer = contextService.getCurrentLecturer();
        List<Course> allCourses = courseRepository.findByLecturer(lecturer);
        List<Course> activeCourses = allCourses.stream()
                .filter(c -> !c.isArchived())
                .toList();

        long totalCourses = allCourses.size();
        long activeCoursesCount = activeCourses.size();
        long totalStudents = enrollmentRepository.countDistinctStudentsByLecturer(lecturer);

        if (activeCourses.isEmpty()) {
            return DashboardResponse.builder()
                    .totalCourses(totalCourses)
                    .activeCourses(0)
                    .totalStudents(totalStudents)
                    .pendingSubmissions(0)
                    .totalMaterials(0)
                    .totalAssignments(0)
                    .averageStudentsPerCourse(0)
                    .attentionItems(Collections.emptyList())
                    .courseSummaries(Collections.emptyList())
                    .recentAnnouncements(Collections.emptyList())
                    .build();
        }

        long totalMaterials = materialRepository.countByCourses(activeCourses);
        long totalAssignments = assignmentRepository.countByCourses(activeCourses);
        long pendingSubmissions = submissionRepository.countUngradedByCourses(activeCourses);

        long averageStudentsPerCourse = activeCoursesCount > 0
                ? BigDecimal.valueOf((double) totalStudents / activeCoursesCount)
                        .setScale(0, RoundingMode.HALF_UP)
                        .longValue()
                : 0;

        Map<Long, Long> enrollmentMap = enrollmentRepository.countByCourseGrouped(activeCourses)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> materialMap = materialRepository.countByCourseGrouped(activeCourses)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> assignmentMap = assignmentRepository.countByCourseGrouped(activeCourses)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> ungradedMap = submissionRepository.countUngradedByCourseGrouped(activeCourses)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<CourseSummaryResponse> courseSummaries = activeCourses.stream()
                .map(c -> buildCourseSummary(c, enrollmentMap, materialMap, assignmentMap, ungradedMap))
                .toList();

        List<AttentionItemResponse> attentionItems = buildAttentionItems(
                pendingSubmissions, activeCourses, materialMap, assignmentMap);

        List<AnnouncementSummaryResponse> recentAnnouncements = announcementRepository
                .findRecentByCourses(activeCourses, PageRequest.of(0, 5))
                .stream()
                .map(AnnouncementSummaryResponse::from)
                .toList();

        return DashboardResponse.builder()
                .totalCourses(totalCourses)
                .activeCourses(activeCoursesCount)
                .totalStudents(totalStudents)
                .pendingSubmissions(pendingSubmissions)
                .totalMaterials(totalMaterials)
                .totalAssignments(totalAssignments)
                .averageStudentsPerCourse(averageStudentsPerCourse)
                .attentionItems(attentionItems)
                .courseSummaries(courseSummaries)
                .recentAnnouncements(recentAnnouncements)
                .build();
    }

    private CourseSummaryResponse buildCourseSummary(
            Course course,
            Map<Long, Long> enrollmentMap,
            Map<Long, Long> materialMap,
            Map<Long, Long> assignmentMap,
            Map<Long, Long> ungradedMap) {

        Long courseId = course.getId();
        long studentCount = enrollmentMap.getOrDefault(courseId, 0L);
        long materialCount = materialMap.getOrDefault(courseId, 0L);
        long assignmentCount = assignmentMap.getOrDefault(courseId, 0L);
        long pendingSubmissionCount = ungradedMap.getOrDefault(courseId, 0L);

        Integer attendanceRate = computeAttendanceRate(course, studentCount);
        Integer averageScore = computeAverageScore(course);
        LocalDateTime lastActivityAt = computeLastActivityAt(course);

        return CourseSummaryResponse.builder()
                .id(courseId)
                .name(course.getTitle())
                .code(course.getCourseCode())
                .studentCount(studentCount)
                .materialCount(materialCount)
                .assignmentCount(assignmentCount)
                .pendingSubmissionCount(pendingSubmissionCount)
                .attendanceRate(attendanceRate)
                .averageScore(averageScore)
                .lastActivityAt(lastActivityAt)
                .build();
    }

    private Integer computeAttendanceRate(Course course, long studentCount) {
        if (studentCount == 0) {
            return null;
        }
        long sessionCount = attendanceSessionRepository.countByCourse(course);
        if (sessionCount == 0) {
            return null;
        }
        long recordCount = attendanceRecordRepository.countByCourse(course);
        if (recordCount == 0) {
            return 0;
        }
        double rate = (double) recordCount / (sessionCount * studentCount) * 100;
        return BigDecimal.valueOf(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private Integer computeAverageScore(Course course) {
        Optional<Double> avg = resultRepository.findAverageScoreByCourse(course);
        return avg.map(val -> BigDecimal.valueOf(val)
                        .setScale(0, RoundingMode.HALF_UP)
                        .intValue())
                .orElse(null);
    }

    private LocalDateTime computeLastActivityAt(Course course) {
        LocalDateTime latest = null;

        Optional<LocalDateTime> material = materialRepository.findLatestUploadedAtByCourse(course);
        if (material.isPresent() && (latest == null || material.get().isAfter(latest))) {
            latest = material.get();
        }

        Optional<LocalDateTime> assignment = assignmentRepository.findLatestCreatedAtByCourse(course);
        if (assignment.isPresent() && (latest == null || assignment.get().isAfter(latest))) {
            latest = assignment.get();
        }

        Optional<LocalDateTime> submission = submissionRepository.findLatestSubmissionAtByCourse(course);
        if (submission.isPresent() && (latest == null || submission.get().isAfter(latest))) {
            latest = submission.get();
        }

        Optional<LocalDateTime> attendance = attendanceRecordRepository.findLatestMarkedAtByCourse(course);
        if (attendance.isPresent() && (latest == null || attendance.get().isAfter(latest))) {
            latest = attendance.get();
        }

        Optional<LocalDateTime> announcement = announcementRepository.findLatestCreatedAtByCourse(course);
        if (announcement.isPresent() && (latest == null || announcement.get().isAfter(latest))) {
            latest = announcement.get();
        }

        return latest;
    }

    private List<AttentionItemResponse> buildAttentionItems(
            long pendingSubmissions,
            List<Course> activeCourses,
            Map<Long, Long> materialMap,
            Map<Long, Long> assignmentMap) {

        List<AttentionItemResponse> items = new ArrayList<>();

        if (pendingSubmissions > 0) {
            items.add(AttentionItemResponse.builder()
                    .type("pending_grading")
                    .title(pendingSubmissions + " submissions need grading")
                    .count(pendingSubmissions)
                    .courseId(null)
                    .courseIds(Collections.emptyList())
                    .href("/lecturer/courses")
                    .build());
        }

        List<Long> coursesWithoutMaterials = activeCourses.stream()
                .filter(c -> materialMap.getOrDefault(c.getId(), 0L) == 0)
                .map(Course::getId)
                .toList();

        if (!coursesWithoutMaterials.isEmpty()) {
            items.add(AttentionItemResponse.builder()
                    .type("missing_materials")
                    .title(coursesWithoutMaterials.size() + " courses need materials")
                    .count(coursesWithoutMaterials.size())
                    .courseId(null)
                    .courseIds(coursesWithoutMaterials)
                    .href("/lecturer/courses")
                    .build());
        }

        List<Long> coursesWithoutAssignments = activeCourses.stream()
                .filter(c -> assignmentMap.getOrDefault(c.getId(), 0L) == 0)
                .map(Course::getId)
                .toList();

        if (!coursesWithoutAssignments.isEmpty()) {
            items.add(AttentionItemResponse.builder()
                    .type("missing_assignments")
                    .title(coursesWithoutAssignments.size() + " course needs assignments")
                    .count(coursesWithoutAssignments.size())
                    .courseId(null)
                    .courseIds(coursesWithoutAssignments)
                    .href("/lecturer/courses")
                    .build());
        }

        return items;
    }
}
