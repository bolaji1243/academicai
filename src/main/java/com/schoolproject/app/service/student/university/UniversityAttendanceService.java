package com.schoolproject.app.service.student.university;

import com.schoolproject.app.dto.response.AttendanceHistoryResponse;
import com.schoolproject.app.dto.response.AttendanceRecordResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.AttendanceRecord;
import com.schoolproject.app.lecturer.entity.AttendanceSession;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.enums.MarkedBy;
import com.schoolproject.app.lecturer.exception.ConflictException;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.AttendanceRecordRepository;
import com.schoolproject.app.lecturer.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityAttendanceService {

    private final UniversityStudentContextService contextService;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;

    @Transactional
    public AttendanceRecordResponse markAttendance(Long sessionId) {
        User student = contextService.getCurrentStudent();
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));
        contextService.verifyEnrollment(session.getCourse().getId());

        if (!session.isOpen()) {
            throw new IllegalArgumentException("Attendance session is closed");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(session.getOpenedAt().plusMinutes(session.getWindowMinutes()))) {
            throw new IllegalArgumentException("Attendance marking window has closed");
        }

        if (recordRepository.existsBySessionAndStudent(session, student)) {
            throw new ConflictException("Attendance already marked for this session");
        }

        AttendanceRecord record = new AttendanceRecord()
                .setSession(session)
                .setStudent(student)
                .setMarkedAt(now)
                .setMarkedBy(MarkedBy.STUDENT);
        return AttendanceRecordResponse.present(recordRepository.save(record));
    }

    public AttendanceHistoryResponse getAttendance(Long courseId) {
        User student = contextService.getCurrentStudent();
        Course course = contextService.verifyEnrollment(courseId);
        var sessions = sessionRepository.findByCourseOrderByOpenedAtDesc(course);
        Map<Long, AttendanceRecord> records = recordRepository.findBySessionCourseAndStudent(course, student).stream()
                .collect(Collectors.toMap(record -> record.getSession().getId(), Function.identity()));
        var responseRecords = sessions.stream()
                .map(session -> {
                    AttendanceRecord record = records.get(session.getId());
                    return record == null
                            ? AttendanceRecordResponse.absent(session)
                            : AttendanceRecordResponse.present(record);
                })
                .toList();
        long total = sessions.size();
        long attended = records.size();
        double percentage = total == 0 ? 0 : (attended * 100.0) / total;

        return AttendanceHistoryResponse.builder()
                .totalSessions(total)
                .attendedSessions(attended)
                .percentage(percentage)
                .records(responseRecords)
                .build();
    }
}
