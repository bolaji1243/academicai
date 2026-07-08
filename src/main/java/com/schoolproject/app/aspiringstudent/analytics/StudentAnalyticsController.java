package com.schoolproject.app.aspiringstudent.analytics;

import com.schoolproject.app.aspiringstudent.analytics.dto.OverviewAnalyticsResponse;
import com.schoolproject.app.aspiringstudent.analytics.dto.SubjectAnalyticsResponse;
import com.schoolproject.app.aspiringstudent.analytics.dto.TopicAnalyticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/analytics")
@PreAuthorize("hasAnyRole('ASPIRING_STUDENT', 'UNIVERSITY_STUDENT')")
public class StudentAnalyticsController {

    private final StudentAnalyticsService studentAnalyticsService;

    public StudentAnalyticsController(StudentAnalyticsService studentAnalyticsService) {
        this.studentAnalyticsService = studentAnalyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<OverviewAnalyticsResponse> getOverview(Authentication authentication) {
        return ResponseEntity.ok(studentAnalyticsService.getOverview(authentication.getName()));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectAnalyticsResponse>> getSubjectAnalytics(Authentication authentication) {
        return ResponseEntity.ok(studentAnalyticsService.getSubjectAnalytics(authentication.getName()));
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicAnalyticsResponse>> getTopicAnalytics(Authentication authentication) {
        return ResponseEntity.ok(studentAnalyticsService.getTopicAnalytics(authentication.getName()));
    }
}
