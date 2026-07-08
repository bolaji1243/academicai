package com.schoolproject.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentResultsSummaryResponse {
    private int runningScore;
    private int runningMaxScore;
    private List<StudentResultResponse> results;
}
