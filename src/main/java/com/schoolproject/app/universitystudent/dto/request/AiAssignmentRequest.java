package com.schoolproject.app.universitystudent.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiAssignmentRequest {
    @NotNull
    private Long assignmentId;
}
