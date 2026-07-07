package com.schoolproject.app.aspiringstudent.controller;

import com.schoolproject.app.aspiringstudent.dto.ExamTypeRequest;
import com.schoolproject.app.aspiringstudent.dto.ExamTypeResponse;
import com.schoolproject.app.aspiringstudent.service.ExamTypeService;
import com.schoolproject.app.dto.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-types")
public class ExamTypeController {

    private final ExamTypeService examTypeService;

    public ExamTypeController(ExamTypeService examTypeService) {
        this.examTypeService = examTypeService;
    }

    // CREATE
    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamTypeResponse createExamType(
            @Valid @RequestBody ExamTypeRequest request
    ) {
        return examTypeService.createExamType(request);
    }

    // GET ALL
    @GetMapping
    public List<ExamTypeResponse> getAllExamTypes() {
        return examTypeService.getAllExamTypes();
    }

    // GET ONE
    @GetMapping("/{id}")
    public ExamTypeResponse getExamTypeById(
            @PathVariable String id
    ) {
        return examTypeService.getExamTypeById(id);
    }

    // UPDATE
    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/{id}")
    public ExamTypeResponse updateExamType(
            @PathVariable String id,
            @Valid @RequestBody ExamTypeRequest request
    ) {
        return examTypeService.updateExamType(id, request);
    }

    // DELETE
    @PreAuthorize("hasRole('LECTURER')")
    @DeleteMapping("/{id}")
    public MessageResponse deleteExamType(
            @PathVariable String id
    ) {
        examTypeService.deleteExamType(id);

        return new MessageResponse("Exam type deleted successfully");
    }
}
