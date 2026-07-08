package com.schoolproject.app.lecturer.dto.request;

import com.schoolproject.app.lecturer.enums.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Week tag is required")
    private String weekTag;

    @NotNull(message = "File type is required")
    private FileType fileType;
}
