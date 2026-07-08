package com.schoolproject.app.aspiringstudent.mockexam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record MockExamSubmitRequest(
        @NotEmpty(message = "answers must not be empty")
        @Valid
        List<AnswerItem> answers
) {
    public record AnswerItem(
            @NotBlank(message = "questionId is required")
            String questionId,

            @NotBlank(message = "selectedOption is required")
            @Pattern(regexp = "^[ABCD]$", message = "selectedOption must be A, B, C, or D")
            String selectedOption
    ) {
    }
}
