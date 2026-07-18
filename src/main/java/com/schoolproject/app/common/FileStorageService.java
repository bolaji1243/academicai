package com.schoolproject.app.common;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Cloudinary cloudinary;

    public String save(String folder, MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String publicId = UUID.randomUUID().toString();
            String resourceType = getResourceType(getExtension(originalFilename));

            Map<?, ?> result = cloudinary.uploader().upload(
                    fileBytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folder,
                            "resource_type", resourceType
                    )
            );

            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public void delete(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
        }
    }

    public void validate(MultipartFile file, long maxSizeBytes, Set<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size cannot exceed " + (maxSizeBytes / (1024 * 1024)) + "MB");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }
    }

    public String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
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
            String url = fileUrl;
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return null;
            String afterUpload = url.substring(uploadIdx + 8);
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
}
