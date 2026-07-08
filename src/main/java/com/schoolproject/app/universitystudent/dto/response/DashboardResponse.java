package com.schoolproject.app.universitystudent.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private long enrolledCoursesCount;
    private long assignmentsDueThisWeek;
    private double overallAttendancePercentage;
    private long unreadAnnouncementsCount;
    private List<AssignmentResponse> upcomingTests;
}
