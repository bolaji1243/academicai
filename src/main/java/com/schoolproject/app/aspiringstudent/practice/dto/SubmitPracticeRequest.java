package com.schoolproject.app.aspiringstudent.practice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record SubmitPracticeRequest(

        @NotBlank(message = "examTypeId is required")
        String examTypeId,

        @NotBlank(message = "subjectId is required")
        String subjectId,

        @NotBlank(message = "topicId is required")
        String topicId,

        @NotEmpty(message = "answers must not be empty")
        @Valid
        List<AnswerItem> answers
) {
    public record AnswerItem(

            @NotBlank(message = "questionId is required")
            String questionId,

            @NotNull(message = "selectedOption is required")
            @Pattern(regexp = "^[ABCD]$", message = "selectedOption must be A, B, C, or D")
            String selectedOption
    ) {}
}