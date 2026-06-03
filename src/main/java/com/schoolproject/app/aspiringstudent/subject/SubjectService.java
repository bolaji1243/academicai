package com.schoolproject.app.aspiringstudent.subject;

import com.schoolproject.app.aspiringstudent.subject.dto.CreateSubjectRequest;
import com.schoolproject.app.aspiringstudent.subject.dto.SubjectResponse;
import com.schoolproject.app.aspiringstudent.subject.dto.UpdateSubjectRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectResponse createSubject(CreateSubjectRequest request) {
        String name = cleanRequired(request.getName(), "Name is required");
        String slug = resolveSlug(request.getSlug(), name);

        if (subjectRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }

        Subject subject = new Subject();
        subject.setName(name);
        subject.setSlug(slug);
        subject.setDescription(cleanOptional(request.getDescription()));

        return SubjectResponse.from(subjectRepository.save(subject));
    }

    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(SubjectResponse::from)
                .toList();
    }

    public SubjectResponse getSubjectById(String id) {
        return SubjectResponse.from(findByPublicId(id));
    }

    public SubjectResponse updateSubject(String id, UpdateSubjectRequest request) {
        Subject subject = findByPublicId(id);

        String name = cleanRequired(request.getName(), "Name is required");
        String slug = resolveSlug(request.getSlug(), name);

        if (subjectRepository.existsBySlugAndPublicIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }

        subject.setName(name);
        subject.setSlug(slug);
        subject.setDescription(cleanOptional(request.getDescription()));

        return SubjectResponse.from(subjectRepository.save(subject));
    }

    public void deleteSubject(String id) {
        subjectRepository.delete(findByPublicId(id));
    }

    private Subject findByPublicId(String id) {
        return subjectRepository.findByPublicId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
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
