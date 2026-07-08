package com.schoolproject.app.controller.student.university;

import com.schoolproject.app.dto.response.AnnouncementResponse;
import com.schoolproject.app.dto.response.ApiResponse;
import com.schoolproject.app.service.student.university.UniversityAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/university")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class UniversityAnnouncementController {

    private final UniversityAnnouncementService announcementService;

    @GetMapping("/courses/{courseId}/announcements")
    public ApiResponse<List<AnnouncementResponse>> getAnnouncements(@PathVariable Long courseId) {
        return ApiResponse.success("Announcements retrieved successfully", announcementService.getAnnouncements(courseId));
    }
}
