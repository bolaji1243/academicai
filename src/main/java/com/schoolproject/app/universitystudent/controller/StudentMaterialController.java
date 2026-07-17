package com.schoolproject.app.universitystudent.controller;

import com.schoolproject.app.universitystudent.dto.response.ApiResponse;
import com.schoolproject.app.universitystudent.dto.response.MaterialResponse;
import com.schoolproject.app.universitystudent.service.StudentMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/university-student")
@PreAuthorize("hasRole('UNIVERSITY_STUDENT')")
@RequiredArgsConstructor
public class StudentMaterialController {

    private final StudentMaterialService materialService;

    @GetMapping("/courses/{courseId}/materials")
    public ApiResponse<List<MaterialResponse>> getMaterials(@PathVariable Long courseId) {
        return ApiResponse.success("Materials retrieved successfully", materialService.getMaterials(courseId));
    }

    @GetMapping("/courses/{courseId}/materials/{materialId}")
    public ApiResponse<MaterialResponse> getMaterialById(
            @PathVariable Long courseId,
            @PathVariable Long materialId) {
        return ApiResponse.success("Material retrieved successfully", materialService.getMaterialById(materialId));
    }

    @GetMapping("/materials/{materialId}/download")
    public ResponseEntity<Void> downloadMaterial(@PathVariable Long materialId) {
        String cloudinaryUrl = materialService.getMaterialDownloadUrl(materialId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, cloudinaryUrl)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .build();
    }
}
