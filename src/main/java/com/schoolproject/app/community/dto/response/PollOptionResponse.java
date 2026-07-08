package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PollOptionResponse {
    private Long id;
    private String text;
    private long voteCount;
}
