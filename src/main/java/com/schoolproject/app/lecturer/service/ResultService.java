package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.dto.request.CreateResultRequest;
import com.schoolproject.app.lecturer.dto.response.ResultResponse;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.Result;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import com.schoolproject.app.lecturer.repository.ResultRepository;
import com.schoolproject.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.StringWriter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final LecturerContextService contextService;
    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    @Transactional
    public ResultResponse createResult(Long courseId, CreateResultRequest request) {
        Course course = contextService.verifyCourseOwnership(courseId);

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!enrollmentRepository.existsByCourseAndStudent(course, student)) {
            throw new ResourceNotFoundException("Student is not enrolled in this course");
        }

        if (request.getScore() > request.getMaxScore()) {
            throw new IllegalArgumentException("Score cannot exceed maximum score");
        }

        Result result = new Result()
                .setCourse(course)
                .setStudent(student)
                .setAssessmentType(request.getAssessmentType())
                .setScore(request.getScore())
                .setMaxScore(request.getMaxScore())
                .setGrade(request.getGrade());

        result = resultRepository.save(result);
        return ResultResponse.from(result);
    }

    @Transactional(readOnly = true)
    public Page<ResultResponse> getCourseResults(Long courseId, Pageable pageable) {
        Course course = contextService.verifyCourseOwnership(courseId);
        Page<Result> results = resultRepository.findByCourse(course, pageable);
        return results.map(ResultResponse::from);
    }

    @Transactional(readOnly = true)
    public byte[] exportResultsCsv(Long courseId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        List<Result> results = resultRepository.findByCourse(course, Pageable.unpaged()).getContent();

        try (StringWriter writer = new StringWriter();
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("studentName", "studentId", "assessmentType", "score", "maxScore", "grade")
                     .build())) {
            for (Result result : results) {
                csv.printRecord(
                        result.getStudent().getFullName(),
                        result.getStudent().getId(),
                        result.getAssessmentType(),
                        result.getScore(),
                        result.getMaxScore(),
                        result.getGrade());
            }
            csv.flush();
            return writer.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to export results CSV", e);
        }
    }
}
