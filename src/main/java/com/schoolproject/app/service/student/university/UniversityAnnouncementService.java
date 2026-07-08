package com.schoolproject.app.service.student.university;

import com.schoolproject.app.dto.response.AnnouncementResponse;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityAnnouncementService {

    private final UniversityStudentContextService contextService;
    private final AnnouncementRepository announcementRepository;

    public List<AnnouncementResponse> getAnnouncements(Long courseId) {
        Course course = contextService.verifyEnrollment(courseId);
        return announcementRepository.findByCourseOrderByPinnedDescCreatedAtDesc(course).stream()
                .map(AnnouncementResponse::from)
                .toList();
    }
}
