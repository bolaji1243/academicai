package com.schoolproject.app.community.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorResponse {
    private String publicId;
    private String fullName;
}
