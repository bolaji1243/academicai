package com.schoolproject.app.lecturer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalCourses;
    private long activeCourses;
    private long totalStudents;
    private long pendingSubmissions;
    private long totalMaterials;
    private long totalAssignments;
    private long averageStudentsPerCourse;
    private List<AttentionItemResponse> attentionItems;
    private List<CourseSummaryResponse> courseSummaries;
    private List<AnnouncementSummaryResponse> recentAnnouncements;
}
