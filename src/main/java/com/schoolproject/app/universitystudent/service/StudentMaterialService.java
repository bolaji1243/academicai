package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.repository.CourseMaterialRepository;
import com.schoolproject.app.universitystudent.dto.response.MaterialResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentMaterialService {

    private final StudentContextService contextService;
    private final CourseMaterialRepository materialRepository;

    public List<MaterialResponse> getMaterials(Long courseId) {
        Course course = contextService.verifyEnrollment(courseId);
        return materialRepository.findByCourseOrderByWeekTagAscUploadedAtDesc(course).stream()
                .map(MaterialResponse::from)
                .toList();
    }

    public MaterialResponse getMaterialById(Long materialId) {
        CourseMaterial material = contextService.getEnrolledMaterial(materialId);
        return MaterialResponse.from(material);
    }

    public String getMaterialDownloadUrl(Long materialId) {
        CourseMaterial material = contextService.getEnrolledMaterial(materialId);
        return material.getFileUrl();
    }
}
