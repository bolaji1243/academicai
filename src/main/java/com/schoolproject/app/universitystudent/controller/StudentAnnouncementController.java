package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.universitystudent.dto.response.AnnouncementResponse;
import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.service.StudentAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/university-student")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentAnnouncementController {

    private final StudentAnnouncementService announcementService;

    @GetMapping("/courses/{courseId}/announcements")
    public ApiResponse<List<AnnouncementResponse>> getAnnouncements(@PathVariable Long courseId) {
        return ApiResponse.success("Announcements retrieved successfully", announcementService.getAnnouncements(courseId));
    }
}
