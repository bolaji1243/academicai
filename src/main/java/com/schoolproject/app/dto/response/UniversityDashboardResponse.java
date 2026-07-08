package com.schoolproject.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UniversityDashboardResponse {
    private long enrolledCoursesCount;
    private long assignmentsDueThisWeek;
    private double overallAttendancePercentage;
    private long unreadAnnouncementsCount;
    private List<StudentAssignmentResponse> upcomingTests;
}
