package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.request.CreateAnnouncementRequest;
import com.schoolproject.app.lecturer.dto.request.UpdateAnnouncementRequest;
import com.schoolproject.app.lecturer.dto.response.AnnouncementResponse;
import com.schoolproject.app.lecturer.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping("/courses/{courseId}/announcements")
    @Operation(summary = "Create an announcement (broadcast if request.isBroadcastToAll() is true)")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> createAnnouncement(
            @PathVariable Long courseId,
            @RequestBody @Valid CreateAnnouncementRequest request) {
        List<AnnouncementResponse> data = announcementService.createAnnouncement(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement posted successfully", data));
    }

    @PutMapping("/announcements/{id}")
    @Operation(summary = "Update an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAnnouncementRequest request) {
        AnnouncementResponse data = announcementService.updateAnnouncement(id, request);
        return ResponseEntity.ok(ApiResponse.success("Announcement updated successfully", data));
    }

    @DeleteMapping("/announcements/{id}")
    @Operation(summary = "Delete an announcement")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully", null));
    }

    @PatchMapping("/announcements/{id}/pin")
    @Operation(summary = "Toggle pin status of an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> togglePin(@PathVariable Long id) {
        AnnouncementResponse data = announcementService.togglePin(id);
        return ResponseEntity.ok(ApiResponse.success("Announcement pin status updated", data));
    }
}
