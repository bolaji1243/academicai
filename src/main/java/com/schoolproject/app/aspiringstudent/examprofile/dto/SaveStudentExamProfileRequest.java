package com.schoolproject.app.aspiringstudent.examprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Student exam profile request")
public class SaveStudentExamProfileRequest {

    @NotBlank(message = "Exam type ID is required")
    @Schema(example = "00000000-0000-0000-0000-000000000000")
    private String examTypeId;

    @NotNull(message = "Subject IDs are required")
    @Schema(example = "[\"00000000-0000-0000-0000-000000000001\", \"00000000-0000-0000-0000-000000000002\"]")
    private List<String> subjectIds;
}
