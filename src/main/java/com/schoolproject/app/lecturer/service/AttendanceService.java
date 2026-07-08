package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.dto.response.AttendanceRecordResponse;
import com.schoolproject.app.lecturer.dto.response.AttendanceSessionResponse;
import com.schoolproject.app.lecturer.entity.AttendanceRecord;
import com.schoolproject.app.lecturer.entity.AttendanceSession;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.enums.MarkedBy;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.AttendanceRecordRepository;
import com.schoolproject.app.lecturer.repository.AttendanceSessionRepository;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import com.schoolproject.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final LecturerContextService contextService;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public AttendanceSessionResponse startSession(Long courseId) {
        Course course = contextService.verifyCourseOwnership(courseId);

        sessionRepository.findByCourseAndOpenTrue(course).ifPresent(s -> {
            throw new ConflictException("An attendance session is already open for this course");
        });

        AttendanceSession session = new AttendanceSession()
                .setCourse(course)
                .setOpenedAt(LocalDateTime.now())
                .setOpen(true)
                .setWindowMinutes(2);

        session = sessionRepository.save(session);
        return AttendanceSessionResponse.from(session, 0, course.getId(), course.getTitle());
    }

    @Transactional
    public AttendanceSessionResponse closeSession(Long sessionId) {
        AttendanceSession session = contextService.verifySessionOwnership(sessionId);

        session.setOpen(false);
        session.setClosedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        long count = recordRepository.countBySession(session);
        return AttendanceSessionResponse.from(session, count, session.getCourse().getId(),
                session.getCourse().getTitle());
    }

    @Transactional
    public AttendanceSessionResponse stopSessionByCourse(Long courseId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        AttendanceSession session = sessionRepository.findByCourseAndOpenTrue(course)
                .orElseThrow(() -> new ConflictException("No open attendance session for this course"));
        session.setOpen(false);
        session.setClosedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        long count = recordRepository.countBySession(session);
        return AttendanceSessionResponse.from(session, count, course.getId(), course.getTitle());
    }

    @Transactional
    public AttendanceSessionResponse reopenSession(Long sessionId) {
        AttendanceSession session = contextService.verifySessionOwnership(sessionId);

        session.setOpen(true);
        session.setClosedAt(null);
        session.setWindowMinutes(session.getWindowMinutes() + 1);
        session = sessionRepository.save(session);

        long count = recordRepository.countBySession(session);
        return AttendanceSessionResponse.from(session, count, session.getCourse().getId(),
                session.getCourse().getTitle());
    }

    @Transactional(readOnly = true)
    public Page<AttendanceRecordResponse> getSessionRecords(Long sessionId, Pageable pageable) {
        AttendanceSession session = contextService.verifySessionOwnership(sessionId);
        Page<AttendanceRecord> records = recordRepository.findBySession(session, pageable);
        return records.map(AttendanceRecordResponse::from);
    }

    @Transactional
    public AttendanceRecordResponse manuallyAddStudent(Long sessionId, Long studentId) {
        AttendanceSession session = contextService.verifySessionOwnership(sessionId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!enrollmentRepository.existsByCourseAndStudent(session.getCourse(), student)) {
            throw new ResourceNotFoundException("Student is not enrolled in this course");
        }

        if (recordRepository.existsBySessionAndStudent(session, student)) {
            throw new ConflictException("Student is already marked for this session");
        }

        AttendanceRecord record = new AttendanceRecord()
                .setSession(session)
                .setStudent(student)
                .setMarkedAt(LocalDateTime.now())
                .setMarkedBy(MarkedBy.LECTURER);

        record = recordRepository.save(record);
        return AttendanceRecordResponse.from(record);
    }

    @Transactional
    public void manuallyRemoveStudent(Long sessionId, Long studentId) {
        AttendanceSession session = contextService.verifySessionOwnership(sessionId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        AttendanceRecord record = recordRepository.findBySessionAndStudent(session, student)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found for this student"));
        recordRepository.delete(record);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceSessionResponse> getAttendanceHistory(Long courseId, Pageable pageable) {
        Course course = contextService.verifyCourseOwnership(courseId);
        Page<AttendanceSession> sessions = sessionRepository.findByCourse(course, pageable);
        return sessions.map(s -> {
            long count = recordRepository.countBySession(s);
            return AttendanceSessionResponse.from(s, count, course.getId(), course.getTitle());
        });
    }
}
