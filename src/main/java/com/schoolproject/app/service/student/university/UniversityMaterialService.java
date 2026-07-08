package com.schoolproject.app.service.student.university;

import com.schoolproject.app.dto.response.MaterialResponse;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.repository.CourseMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityMaterialService {

    private final UniversityStudentContextService contextService;
    private final CourseMaterialRepository materialRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public List<MaterialResponse> getMaterials(Long courseId) {
        Course course = contextService.verifyEnrollment(courseId);
        return materialRepository.findByCourseOrderByWeekTagAscUploadedAtDesc(course).stream()
                .map(MaterialResponse::from)
                .toList();
    }

    public Resource downloadMaterial(Long materialId) {
        CourseMaterial material = contextService.getEnrolledMaterial(materialId);
        try {
            Path path = resolveStoredPath(material.getFileUrl());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable() || !Files.isRegularFile(path)) {
                throw new IllegalArgumentException("Material file is not available");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid material file path");
        }
    }

    private Path resolveStoredPath(String relativePath) {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(relativePath).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return filePath;
    }
}
