package com.parivahan.backend.assistant.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini AI implementation of AiClient.
 *
 * Security guarantees:
 *  - The API key is read from server-side configuration only.
 *  - It is never sent to the frontend or logged.
 *  - This class never accesses the database, JPA repositories, or JWT.
 *  - It only receives a controlled text context from VehicleContextBuilder.
 */
@Slf4j
@Primary
@Component
public class GeminiAiClient implements AiClient {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiAiClient() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String generate(String systemPrompt, String context, String userQuestion) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        // Build the full prompt combining system instructions, vehicle context, and user question
        String fullPrompt = systemPrompt
                + "\n\n--- VEHICLE CONTEXT ---\n" + context
                + "\n\n--- USER QUESTION ---\n" + userQuestion;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", fullPrompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 1024
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return extractText(response.getBody());
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("AI service temporarily unavailable", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Failed to extract text from Gemini response: {}", e.getMessage());
            throw new RuntimeException("Unexpected AI response format", e);
        }
    }
}
