package com.schoolproject.app.universitystudent.dto.response;

import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.enums.FileType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaterialResponse {
    private Long id;
    private String title;
    private String description;
    private FileType fileType;
    private String weekTag;
    private String summary;
    private LocalDateTime uploadedAt;

    public static MaterialResponse from(CourseMaterial material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .fileType(material.getFileType())
                .weekTag(material.getWeekTag())
                .summary(material.getSummary())
                .uploadedAt(material.getUploadedAt())
                .build();
    }
}
