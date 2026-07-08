package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.dto.response.StudentResultResponse;
import com.schoolproject.app.dto.response.StudentResultsSummaryResponse;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.Result;
import com.schoolproject.app.lecturer.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentResultService {

    private final StudentContextService contextService;
    private final ResultRepository resultRepository;

    public StudentResultsSummaryResponse getResults(Long courseId) {
        User student = contextService.getCurrentStudent();
        Course course = contextService.verifyEnrollment(courseId);
        var results = resultRepository.findByCourseAndStudentOrderByAssessmentTypeAsc(course, student);
        int runningScore = results.stream().map(Result::getScore).filter(score -> score != null).mapToInt(Integer::intValue).sum();
        int runningMaxScore = results.stream().map(Result::getMaxScore).filter(score -> score != null).mapToInt(Integer::intValue).sum();

        return StudentResultsSummaryResponse.builder()
                .runningScore(runningScore)
                .runningMaxScore(runningMaxScore)
                .results(results.stream().map(StudentResultResponse::from).toList())
                .build();
    }
}
