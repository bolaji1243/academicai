package com.schoolproject.app.aspiringstudent.recommendations;

import com.schoolproject.app.aspiringstudent.recommendations.dto.RecommendationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasAnyRole('ASPIRING_STUDENT', 'UNIVERSITY_STUDENT')")
public class RecommendationsController {

    private final RecommendationsService recommendationsService;

    public RecommendationsController(RecommendationsService recommendationsService) {
        this.recommendationsService = recommendationsService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(Authentication authentication) {
        return ResponseEntity.ok(recommendationsService.getRecommendations(authentication.getName()));
    }
}
