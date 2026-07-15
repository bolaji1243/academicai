package com.schoolproject.app.community.dto.request;

import com.schoolproject.app.community.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChannelRequest {
    @NotBlank(message = "Channel name is required")
    private String name;
    @NotNull(message = "Channel type is required")
    private ChannelType type;
}
