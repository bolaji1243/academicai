package com.schoolproject.app.universitystudent.dto.response;

import com.schoolproject.app.lecturer.entity.Course;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String courseCode;
    private String description;
    private String schedule;
    private String lecturerName;
    private long materialCount;

    public static CourseResponse from(Course course, long materialCount) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .courseCode(course.getCourseCode())
                .description(course.getDescription())
                .schedule(course.getSchedule())
                .lecturerName(course.getLecturer().getUser().getFullName())
                .materialCount(materialCount)
                .build();
    }
}
