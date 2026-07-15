package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.community.entity.NotificationType;
import com.schoolproject.app.community.service.NotificationService;
import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.lecturer.dto.request.CreateAnnouncementRequest;
import com.schoolproject.app.lecturer.dto.request.UpdateAnnouncementRequest;
import com.schoolproject.app.lecturer.dto.response.AnnouncementResponse;
import com.schoolproject.app.lecturer.entity.Announcement;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.repository.AnnouncementRepository;
import com.schoolproject.app.lecturer.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final LecturerContextService contextService;
    private final AnnouncementRepository announcementRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    @Transactional
    public List<AnnouncementResponse> createAnnouncement(Long courseId, CreateAnnouncementRequest request) {
        if (request.isBroadcastToAll()) {
            LecturerProfile lecturer = contextService.getCurrentLecturer();
            List<Course> courses = courseRepository.findByLecturerAndArchivedFalse(lecturer);

            List<AnnouncementResponse> responses = new ArrayList<>();
            for (Course course : courses) {
                try {
                    Announcement announcement = new Announcement()
                            .setCourse(course)
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .setPinned(request.isPinned());
                    announcement = announcementRepository.save(announcement);
                    responses.add(AnnouncementResponse.from(announcement, course.getId(), course.getTitle()));
                    sendAnnouncementNotification(course, request.getTitle(), String.valueOf(announcement.getId()));
                } catch (Exception e) {
                    log.warn("Failed to create announcement for course {}: {}", course.getId(), e.getMessage());
                }
            }
            return responses;
        }

        Course course = contextService.verifyCourseOwnership(courseId);
        Announcement announcement = new Announcement()
                .setCourse(course)
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .setPinned(request.isPinned());
        announcement = announcementRepository.save(announcement);
        sendAnnouncementNotification(course, request.getTitle(), String.valueOf(announcement.getId()));
        return List.of(AnnouncementResponse.from(announcement, course.getId(), course.getTitle()));
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, UpdateAnnouncementRequest request) {
        Announcement announcement = contextService.verifyAnnouncementOwnership(announcementId);

        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            announcement.setBody(request.getBody());
        }

        announcement = announcementRepository.save(announcement);
        Long courseId = announcement.getCourse() != null ? announcement.getCourse().getId() : null;
        String courseTitle = announcement.getCourse() != null ? announcement.getCourse().getTitle() : null;
        return AnnouncementResponse.from(announcement, courseId, courseTitle);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        Announcement announcement = contextService.verifyAnnouncementOwnership(announcementId);
        announcementRepository.delete(announcement);
    }

    @Transactional
    public AnnouncementResponse togglePin(Long announcementId) {
        Announcement announcement = contextService.verifyAnnouncementOwnership(announcementId);
        announcement.setPinned(!announcement.isPinned());
        announcement = announcementRepository.save(announcement);
        Long courseId = announcement.getCourse() != null ? announcement.getCourse().getId() : null;
        String courseTitle = announcement.getCourse() != null ? announcement.getCourse().getTitle() : null;
        return AnnouncementResponse.from(announcement, courseId, courseTitle);
    }

    private void sendAnnouncementNotification(Course course, String title, String resourceId) {
        try {
            var lecturer = contextService.getCurrentLecturer();
            var user = lecturer.getUser();
            notificationService.notifyCommunityMembers(
                    course.getId(), user, NotificationType.NEW_ANNOUNCEMENT,
                    "New announcement: " + title,
                    "A new announcement was posted in " + course.getTitle(),
                    resourceId
            );
        } catch (Exception e) {
            log.warn("Failed to send announcement notification for course {}: {}", course.getId(), e.getMessage());
        }
    }
}
