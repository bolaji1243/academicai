package com.schoolproject.app.universitystudent.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResultsSummaryResponse {
    private int runningScore;
    private int runningMaxScore;
    private List<ResultResponse> results;
}
