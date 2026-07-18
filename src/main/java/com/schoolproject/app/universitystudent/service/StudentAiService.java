package com.schoolproject.app.universitystudent.service;

import com.schoolproject.app.common.TextExtractor;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.Assignment;
import com.schoolproject.app.lecturer.entity.Course;
import com.schoolproject.app.lecturer.entity.CourseMaterial;
import com.schoolproject.app.lecturer.repository.AssignmentRepository;
import com.schoolproject.app.lecturer.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAiService {

    private static final int MAX_CONTEXT_CHARS = 12000;

    private final StudentContextService contextService;
    private final RestTemplate restTemplate;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    public String summarise(Long materialId) {
        CourseMaterial material = contextService.getEnrolledMaterial(materialId);
        String text = extractMaterialText(material);
        return callGroq(
                "Summarise this course material in a short, clear student-friendly summary. Return plain text only.",
                text);
    }

    public String ask(Long materialId, String question) {
        CourseMaterial material = contextService.getEnrolledMaterial(materialId);
        String text = extractMaterialText(material);
        return callGroq(
                "Answer the student's question using only the supplied material. If the answer is not in the material, say that plainly. Return plain text only.",
                "Material:\n" + text + "\n\nQuestion:\n" + question);
    }

    public String practiceQuestions(Long materialId, int count) {
        CourseMaterial material = contextService.getEnrolledMaterial(materialId);
        String text = extractMaterialText(material);
        return callGroq(
                "Generate exactly " + count + " practice questions from the supplied material. Include brief answers. Return plain text only.",
                text);
    }

    public String explainAssignment(Long assignmentId) {
        Assignment assignment = contextService.getEnrolledAssignment(assignmentId);
        return callGroq(
                "Explain what this assignment is asking the student to do. Do not write or outline the answer. Return plain text only.",
                "Title: " + assignment.getTitle() +
                        "\nDeadline: " + assignment.getDeadline() +
                        "\nInstructions:\n" + assignment.getInstructions());
    }

    public String studyPlanner() {
        User student = contextService.getCurrentStudent();
        List<Course> courses = enrollmentRepository.findByStudent(student).stream()
                .map(enrollment -> enrollment.getCourse())
                .toList();
        if (courses.isEmpty()) {
            return "You are not enrolled in any courses yet.";
        }

        var start = LocalDate.now().atStartOfDay();
        var end = LocalDate.now().plusDays(7).atTime(LocalTime.MAX);
        var assignments = assignmentRepository.findByCourseInAndDeadlineBetween(courses, start, end);
        StringBuilder prompt = new StringBuilder("Create a simple day by day study plan for the next 7 days.\nCourses:\n");
        courses.forEach(course -> prompt.append("- ")
                .append(course.getCourseCode())
                .append(": ")
                .append(course.getTitle())
                .append("\n"));
        prompt.append("\nUpcoming deadlines and tests:\n");
        assignments.forEach(assignment -> prompt.append("- ")
                .append(assignment.getTitle())
                .append(" for ")
                .append(assignment.getCourse().getCourseCode())
                .append(" due ")
                .append(assignment.getDeadline())
                .append("\n"));

        return callGroq(
                "You are a practical academic study planner. Return a concise day by day plan for one week in plain text only.",
                prompt.toString());
    }

    private static final int MAX_BYTES_TO_PARSE = 512 * 1024;

    private String extractMaterialText(CourseMaterial material) {
        String fileUrl = material.getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("Material has no file attached");
        }

        try (InputStream raw = new URL(fileUrl).openStream()) {
            String text = TextExtractor.extract(raw);
            if (text.isBlank()) {
                throw new IllegalArgumentException("No readable text found in material: " + material.getTitle());
            }
            return text;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract text from material {} (url={}): {}", material.getId(), fileUrl, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to extract text from material: " + e.getMessage());
        }
    }

    private String callGroq(String systemPrompt, String userPrompt) {
        if (groqApiKey.isBlank()) {
            throw new IllegalArgumentException("Groq API key is not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "max_tokens", 1000,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions",
                new HttpEntity<>(body, headers),
                Map.class);

        if (response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null && !content.isBlank()) {
                        return content;
                    }
                }
            }
        }
        throw new RuntimeException("Empty response from Groq API");
    }
}
