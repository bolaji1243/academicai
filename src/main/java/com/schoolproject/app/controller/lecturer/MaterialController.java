package com.schoolproject.app.controller.lecturer;

import com.schoolproject.app.lecturer.dto.ApiResponse;
import com.schoolproject.app.lecturer.dto.PageResponse;
import com.schoolproject.app.lecturer.dto.request.CreateMaterialRequest;
import com.schoolproject.app.lecturer.dto.response.MaterialResponse;
import com.schoolproject.app.lecturer.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lecturer/courses")
@PreAuthorize("hasRole('LECTURER')")
@RequiredArgsConstructor
@Tag(name = "Lecturer - Materials")
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping("/{courseId}/materials")
    @Operation(summary = "Upload a course material")
    public ResponseEntity<ApiResponse<MaterialResponse>> uploadMaterial(
            @PathVariable Long courseId,
            @ModelAttribute @Valid CreateMaterialRequest request,
            @RequestParam MultipartFile file) {
        MaterialResponse data = materialService.uploadMaterial(courseId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material uploaded successfully", data));
    }

    @GetMapping("/{courseId}/materials")
    @Operation(summary = "Get course materials")
    public ResponseEntity<ApiResponse<PageResponse<MaterialResponse>>> getMaterials(
            @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "uploadedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<MaterialResponse> page = materialService.getMaterials(courseId, pageable);
        PageResponse<MaterialResponse> data = PageResponse.from(page);
        return ResponseEntity.ok(ApiResponse.success("Materials retrieved successfully", data));
    }

    @DeleteMapping("/{courseId}/materials/{materialId}")
    @Operation(summary = "Delete a course material")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @PathVariable Long courseId,
            @PathVariable Long materialId) {
        materialService.deleteMaterial(courseId, materialId);
        return ResponseEntity.ok(ApiResponse.success("Material deleted successfully", null));
    }

    @PostMapping("/{courseId}/materials/summarise/{materialId}")
    @Operation(summary = "Trigger AI summary generation for a material")
    public ResponseEntity<ApiResponse<Void>> triggerSummary(
            @PathVariable Long courseId,
            @PathVariable Long materialId) {
        materialService.triggerSummary(courseId, materialId);
        return ResponseEntity.ok(ApiResponse.success("Summary generation started", null));
    }
}
