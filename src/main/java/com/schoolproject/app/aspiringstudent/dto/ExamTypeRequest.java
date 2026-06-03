package com.schoolproject.app.aspiringstudent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Exam type request")
public class ExamTypeRequest {

    @Schema(example = "JAMB")
    @Size(max = 255, message = "Name must be 255 characters or less")
    private String name;

    @Schema(example = "jamb", description = "Optional. If omitted, the backend generates it from the name.")
    @Pattern(regexp = "^[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*$", message = "Slug must contain letters, numbers, and hyphens only")
    @Size(max = 255, message = "Slug must be 255 characters or less")
    private String slug;

    @Schema(example = "Joint Admissions and Matriculation Board exam")
    @Size(max = 500, message = "Description must be 500 characters or less")
    private String description;
}
