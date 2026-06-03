package com.schoolproject.app.aspiringstudent.service;

import com.schoolproject.app.aspiringstudent.dto.ExamTypeRequest;
import com.schoolproject.app.aspiringstudent.dto.ExamTypeResponse;
import com.schoolproject.app.aspiringstudent.entity.ExamType;
import com.schoolproject.app.aspiringstudent.repository.ExamTypeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExamTypeService {

    private final ExamTypeRepository examTypeRepository;

    public ExamTypeService(ExamTypeRepository examTypeRepository) {
        this.examTypeRepository = examTypeRepository;
    }

    // CREATE
    public ExamTypeResponse createExamType(ExamTypeRequest request) {
        String name = cleanRequired(request.getName(), "Name is required");
        String slug = resolveSlug(request.getSlug(), name);

        if (examTypeRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }

        ExamType examType = new ExamType();
        examType.setName(name);
        examType.setSlug(slug);
        examType.setDescription(cleanOptional(request.getDescription()));

        return ExamTypeResponse.from(examTypeRepository.save(examType));
    }

    // GET ALL
    public List<ExamTypeResponse> getAllExamTypes() {
        return examTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(ExamTypeResponse::from)
                .toList();
    }

    // GET ONE
    public ExamTypeResponse getExamTypeById(String id) {
        return ExamTypeResponse.from(findByPublicId(id));
    }

    // UPDATE
    public ExamTypeResponse updateExamType(String id, ExamTypeRequest request) {
        ExamType examType = findByPublicId(id);

        String name = cleanRequired(request.getName(), "Name is required");
        String slug = resolveSlug(request.getSlug(), name);

        if (examTypeRepository.existsBySlugAndPublicIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }

        examType.setName(name);
        examType.setSlug(slug);
        examType.setDescription(cleanOptional(request.getDescription()));

        return ExamTypeResponse.from(examTypeRepository.save(examType));
    }

    // DELETE
    public void deleteExamType(String id) {
        examTypeRepository.delete(findByPublicId(id));
    }

    private ExamType findByPublicId(String id) {
        return examTypeRepository.findByPublicId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam type not found"));
    }

    private String cleanRequired(String value, String message) {
        String cleaned = cleanOptional(value);

        if (cleaned == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return cleaned;
    }

    private String cleanOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String resolveSlug(String requestedSlug, String name) {
        String slug = cleanOptional(requestedSlug);

        if (slug == null) {
            slug = name;
        }

        slug = slug.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug must contain at least one letter or number");
        }

        return slug;
    }
}
