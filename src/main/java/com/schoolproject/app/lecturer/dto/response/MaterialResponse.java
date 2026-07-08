package com.schoolproject.app.lecturer.dto.response;

import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponse {

    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private FileType fileType;
    private String weekTag;
    private String summary;
    private LocalDateTime uploadedAt;

    public static MaterialResponse from(CourseMaterial material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .fileUrl(material.getFileUrl())
                .fileType(material.getFileType())
                .weekTag(material.getWeekTag())
                .summary(material.getSummary())
                .uploadedAt(material.getUploadedAt())
                .build();
    }
}
