package com.schoolproject.app.lecturer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 5000, message = "Rough note must not exceed 5000 characters")
    private String roughNote;
}
