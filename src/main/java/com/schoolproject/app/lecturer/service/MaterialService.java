package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.community.entity.NotificationType;
import com.schoolproject.app.community.service.NotificationService;
import com.schoolproject.app.lecturer.dto.request.CreateMaterialRequest;
import com.schoolproject.app.lecturer.dto.response.MaterialResponse;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.enums.FileType;
import com.schoolproject.app.lecturer.exception.ResourceNotFoundException;
import com.schoolproject.app.lecturer.repository.CourseMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024;
    private static final Map<FileType, Set<String>> ALLOWED_EXTENSIONS = Map.of(
            FileType.PDF, Set.of(".pdf"),
            FileType.PPTX, Set.of(".ppt", ".pptx"),
            FileType.DOCX, Set.of(".doc", ".docx"),
            FileType.IMAGE, Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp"),
            FileType.OTHER, Set.of(".txt", ".csv")
    );

    private final LecturerContextService contextService;
    private final CourseMaterialRepository materialRepository;
    private final AiService aiService;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public MaterialResponse uploadMaterial(Long courseId, CreateMaterialRequest request, MultipartFile file) {
        Course course = contextService.verifyCourseOwnership(courseId);

        validateUpload(request.getFileType(), file);

        String fileUrl = saveFile(courseId, file);

        CourseMaterial material;
        try {
            material = new CourseMaterial()
                    .setCourse(course)
                    .setTitle(request.getTitle())
                    .setDescription(request.getDescription())
                    .setFileUrl(fileUrl)
                    .setFileType(request.getFileType())
                    .setWeekTag(request.getWeekTag())
                    .setUploadedAt(LocalDateTime.now());

            material = materialRepository.save(material);
        } catch (RuntimeException e) {
            deleteStoredFile(fileUrl);
            throw e;
        }

        try {
            String extractedText = extractText(file);
            aiService.generateMaterialSummary(material.getId(), extractedText);
        } catch (Exception e) {
            log.warn("Failed to trigger AI summary for material {}: {}", material.getId(), e.getMessage());
        }

        sendResourceUploadedNotification(course, request.getTitle(), String.valueOf(material.getId()));

        return MaterialResponse.from(material);
    }

    @Transactional(readOnly = true)
    public Page<MaterialResponse> getMaterials(Long courseId, Pageable pageable) {
        Course course = contextService.verifyCourseOwnership(courseId);
        Page<CourseMaterial> materials = materialRepository.findByCourse(course, pageable);
        return materials.map(MaterialResponse::from);
    }

    @Transactional
    public void deleteMaterial(Long courseId, Long materialId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        CourseMaterial material = materialRepository.findByIdAndCourse(materialId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        Path filePath = resolveStoredPath(material.getFileUrl());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete physical file for material {}: {}", materialId, e.getMessage());
        }

        materialRepository.delete(material);
    }

    private String saveFile(Long courseId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;
            String relativePath = "materials/" + courseId + "/" + filename;
            Path fullPath = resolveStoredPath(relativePath);
            Files.createDirectories(fullPath.getParent());
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Transactional(readOnly = true)
    public void triggerSummary(Long courseId, Long materialId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        CourseMaterial material = materialRepository.findByIdAndCourse(materialId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        Path filePath = resolveStoredPath(material.getFileUrl());
        String extractedText = "";
        try {
            if (material.getFileType() == FileType.PDF) {
                extractedText = extractTextFromPdf(filePath);
            } else {
                extractedText = filePath.getFileName().toString();
            }
        } catch (Exception e) {
            log.warn("Failed to extract text for summary trigger: {}", e.getMessage());
        }

        if (!extractedText.isBlank()) {
            aiService.generateMaterialSummary(materialId, extractedText);
        }
    }

    private String extractTextFromPdf(Path filePath) throws IOException {
        try (var document = org.apache.pdfbox.Loader.loadPDF(filePath.toFile())) {
            var stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            return text.length() > 3000 ? text.substring(0, 3000) : text;
        }
    }

    private String extractText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Tika tika = new Tika();
            String text = tika.parseToString(inputStream);
            return text.length() > 3000 ? text.substring(0, 3000) : text;
        } catch (Exception e) {
            log.warn("Failed to extract text from file: {}", e.getMessage());
            return "";
        }
    }

    private void validateUpload(FileType fileType, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size cannot exceed 20MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        Set<String> allowedExtensions = ALLOWED_EXTENSIONS.getOrDefault(fileType, Set.of());
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File extension does not match selected file type");
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }

    private Path resolveStoredPath(String relativePath) {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(relativePath).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return filePath;
    }

    private void deleteStoredFile(String fileUrl) {
        try {
            Files.deleteIfExists(resolveStoredPath(fileUrl));
        } catch (IOException e) {
            log.warn("Failed to clean up uploaded file {}: {}", fileUrl, e.getMessage());
        }
    }

    private void sendResourceUploadedNotification(Course course, String materialTitle, String resourceId) {
        try {
            var lecturer = contextService.getCurrentLecturer();
            var user = lecturer.getUser();
            notificationService.notifyCommunityMembers(
                    course.getId(), user, NotificationType.RESOURCE_UPLOADED,
                    "New material: " + materialTitle,
                    "A new material was uploaded to " + course.getTitle(),
                    resourceId
            );
        } catch (Exception e) {
            log.warn("Failed to send resource upload notification for course {}: {}", course.getId(), e.getMessage());
        }
    }
}
