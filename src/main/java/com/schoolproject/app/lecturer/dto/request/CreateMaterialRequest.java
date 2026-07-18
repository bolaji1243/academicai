package com.schoolproject.app.lecturer.dto.request;

import com.schoolproject.app.lecturer.enums.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Week tag is required")
    @Size(max = 50, message = "Week tag must not exceed 50 characters")
    private String weekTag;

    @NotNull(message = "File type is required")
    private FileType fileType;
}
