package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String title;
    private String courseCode;
    private String description;
    private String schedule;
    private String joinCode;
    private boolean isArchived;
    private LocalDateTime createdAt;
    private String lecturerName;
    private long enrolledCount;

    public static CourseResponse from(Course course, long enrolledCount, String lecturerName) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .courseCode(course.getCourseCode())
                .description(course.getDescription())
                .schedule(course.getSchedule())
                .joinCode(course.getJoinCode())
                .isArchived(course.isArchived())
                .createdAt(course.getCreatedAt())
                .lecturerName(lecturerName)
                .enrolledCount(enrolledCount)
                .build();
    }
}
