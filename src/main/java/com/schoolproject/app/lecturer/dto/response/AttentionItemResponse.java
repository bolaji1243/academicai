package com.schoolproject.app.lecturer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AttentionItemResponse {

    private String type;
    private String title;
    private long count;
    private Long courseId;
    private List<Long> courseIds;
    private String href;
}
