package com.schoolproject.app.community.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VotePollRequest {
    @NotNull(message = "Option ID is required")
    private Long optionId;
}
