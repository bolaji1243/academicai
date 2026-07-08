package com.schoolproject.app.lecturer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionsResponse {

    private String topic;
    private String level;
    private List<GeneratedQuestionResponse> questions;
}
