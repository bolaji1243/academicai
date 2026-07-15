package com.schoolproject.app.lecturer.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
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
    private final Cloudinary cloudinary;

    @Transactional
    public MaterialResponse uploadMaterial(Long courseId, CreateMaterialRequest request, MultipartFile file) {
        Course course = contextService.verifyCourseOwnership(courseId);

        validateUpload(request.getFileType(), file);

        String fileUrl = uploadToCloudinary(courseId, file);

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
            deleteFromCloudinary(fileUrl);
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

        deleteFromCloudinary(material.getFileUrl());

        materialRepository.delete(material);
    }

    @Transactional(readOnly = true)
    public void triggerSummary(Long courseId, Long materialId) {
        Course course = contextService.verifyCourseOwnership(courseId);
        CourseMaterial material = materialRepository.findByIdAndCourse(materialId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        String extractedText = "";
        try {
            if (material.getFileType() == FileType.PDF) {
                extractedText = extractTextFromCloudinaryUrl(material.getFileUrl());
            } else {
                extractedText = "Material: " + material.getTitle();
            }
        } catch (Exception e) {
            log.warn("Failed to extract text for summary trigger: {}", e.getMessage());
        }

        if (!extractedText.isBlank()) {
            aiService.generateMaterialSummary(materialId, extractedText);
        }
    }

    private String uploadToCloudinary(Long courseId, MultipartFile file) {
        try {
            String publicId = UUID.randomUUID().toString();
            String folder = "academicai/materials/" + courseId;
            String resourceType = getResourceType(getExtension(file.getOriginalFilename()));

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folder,
                            "resource_type", resourceType
                    )
            );

            return (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload material to Cloudinary", e);
        }
    }

    private void deleteFromCloudinary(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractTextFromCloudinaryUrl(String fileUrl) throws Exception {
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            String text = new Tika().parseToString(inputStream);
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

    private String getResourceType(String extension) {
        return switch (extension) {
            case ".pdf" -> "raw";
            case ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".csv", ".zip", ".rar" -> "raw";
            default -> "image";
        };
    }

    private String extractPublicId(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return null;
        try {
            int uploadIdx = fileUrl.indexOf("/upload/");
            if (uploadIdx == -1) return null;
            String afterUpload = fileUrl.substring(uploadIdx + 8);
            int versionEnd = afterUpload.indexOf("/");
            if (versionEnd == -1) return afterUpload;
            String pathWithExt = afterUpload.substring(versionEnd + 1);
            int lastDot = pathWithExt.lastIndexOf(".");
            return lastDot > -1 ? pathWithExt.substring(0, lastDot) : pathWithExt;
        } catch (Exception e) {
            log.warn("Failed to extract public ID from URL: {}", fileUrl);
            return null;
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
