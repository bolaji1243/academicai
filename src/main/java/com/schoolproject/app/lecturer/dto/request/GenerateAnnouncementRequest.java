package com.schoolproject.app.lecturer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAnnouncementRequest {

    @NotBlank(message = "Rough note is required")
    private String roughNote;
}
