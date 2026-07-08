package com.schoolproject.app.lecturer.service;

import com.schoolproject.app.lecturer.dto.response.AiQuestionsResponse;
import com.schoolproject.app.lecturer.dto.response.GeneratedQuestionResponse;
import com.schoolproject.app.lecturer.repository.CourseMaterialRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final LecturerContextService contextService;
    private final CourseMaterialRepository materialRepository;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    public String generateAnnouncement(String roughNote) {
        if (groqApiKey.isBlank()) {
            return "Based on your notes: " + roughNote;
        }

        try {
            String systemPrompt = "You are an academic communication assistant for a Nigerian university platform. " +
                    "Convert the lecturer's rough note into a clear, professional, and friendly announcement " +
                    "addressed to students. Keep it concise — maximum 4 sentences. Return plain text only. " +
                    "No preamble, no explanation.";

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "max_tokens", 1000,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", roughNote)
                    )
            );

            return callGroqAndGetText(body);
        } catch (Exception e) {
            log.warn("AI announcement generation failed, using fallback: {}", e.getMessage());
            return "Based on your notes: " + roughNote;
        }
    }

    public AiQuestionsResponse generateAssignmentQuestions(String topic, String level, int count) {
        if (groqApiKey.isBlank()) {
            List<GeneratedQuestionResponse> fallback = List.of(
                    GeneratedQuestionResponse.builder()
                            .question("Explain " + topic + " in detail.")
                            .difficultyLevel(level)
                            .markingGuide("Assess understanding of core concepts.")
                            .build()
            );
            return AiQuestionsResponse.builder()
                    .topic(topic)
                    .level(level)
                    .questions(fallback)
                    .build();
        }

        try {
            String systemPrompt = String.format(
                    "You are an academic question generator for Nigerian university courses. " +
                    "Generate exactly %d assignment questions on the topic: %s for %s students. " +
                    "Return a JSON array only — no markdown, no explanation. " +
                    "Format: [{ question, difficultyLevel, markingGuide }]",
                    count, topic, level);

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "max_tokens", 1000,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content",
                                    String.format("Generate %d questions on %s for %s level.", count, topic, level))
                    )
            );

            String responseText = callGroqAndGetText(body);

            ObjectMapper mapper = new ObjectMapper();
            List<GeneratedQuestionResponse> questions = mapper.readValue(
                    responseText, new TypeReference<List<GeneratedQuestionResponse>>() {});
            return AiQuestionsResponse.builder()
                    .topic(topic)
                    .level(level)
                    .questions(questions)
                    .build();
        } catch (Exception e) {
            log.warn("AI question generation failed, using fallback: {}", e.getMessage());
            List<GeneratedQuestionResponse> fallback = List.of(
                    GeneratedQuestionResponse.builder()
                            .question("Explain " + topic + " in detail.")
                            .difficultyLevel(level)
                            .markingGuide("Assess understanding of core concepts.")
                            .build()
            );
            return AiQuestionsResponse.builder()
                    .topic(topic)
                    .level(level)
                    .questions(fallback)
                    .build();
        }
    }

    @Async
    @Transactional
    public void generateMaterialSummary(Long materialId, String extractedText) {
        if (groqApiKey.isBlank()) {
            return;
        }

        try {
            String text = extractedText.length() > 3000 ? extractedText.substring(0, 3000) : extractedText;

            String systemPrompt = "You are an academic assistant for a Nigerian university platform. " +
                    "Summarise this lecture note in 3 to 4 sentences. " +
                    "Focus on the key topics covered so students know what to expect before reading the full document. " +
                    "Be clear and factual. Return plain text only.";

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "max_tokens", 1000,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", text)
                    )
            );

            String summary = callGroqAndGetText(body);

            materialRepository.findById(materialId).ifPresent(material -> {
                material.setSummary(summary);
                materialRepository.save(material);
                log.info("AI summary generated for material {}", materialId);
            });
        } catch (Exception e) {
            log.warn("AI summary generation failed for material {}: {}", materialId, e.getMessage());
        }
    }

    private String callGroqAndGetText(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions", entity, Map.class);

        if (response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        }
        throw new RuntimeException("Empty response from Groq API");
    }
}
