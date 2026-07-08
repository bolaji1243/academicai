package com.schoolproject.app.community.dto.request;

import com.schoolproject.app.community.entity.ChannelType;
import lombok.Data;

@Data
public class CreateChannelRequest {
    private String name;
    private ChannelType type;
}
