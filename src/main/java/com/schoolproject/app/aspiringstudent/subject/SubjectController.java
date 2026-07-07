package com.schoolproject.app.aspiringstudent.subject;

import com.schoolproject.app.aspiringstudent.subject.dto.CreateSubjectRequest;
import com.schoolproject.app.aspiringstudent.subject.dto.SubjectResponse;
import com.schoolproject.app.aspiringstudent.subject.dto.UpdateSubjectRequest;
import com.schoolproject.app.dto.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponse createSubject(
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        return subjectService.createSubject(request);
    }

    @GetMapping
    public List<SubjectResponse> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{id}")
    public SubjectResponse getSubjectById(
            @PathVariable String id
    ) {
        return subjectService.getSubjectById(id);
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/{id}")
    public SubjectResponse updateSubject(
            @PathVariable String id,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        return subjectService.updateSubject(id, request);
    }

    @PreAuthorize("hasRole('LECTURER')")
    @DeleteMapping("/{id}")
    public MessageResponse deleteSubject(
            @PathVariable String id
    ) {
        subjectService.deleteSubject(id);

        return new MessageResponse("Subject deleted successfully");
    }
}
