package com.nutrition.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class OpenAIService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.openai.key}")
    private String apiKey;

    @Value("${api.openai.url}")
    private String apiUrl;

    @Value("${api.openai.model}")
    private String model;

    @Async
    public CompletableFuture<String> analyzeImage(String base64Image) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", List.of(
                    Map.of("type", "text", "text", "Analyze this food image and identify all the food items visible. For EACH food item, estimate the realistic weight in grams based on typical serving sizes. For example: a plate of rice is typically 150-200g, a piece of fried chicken is 100-150g, a fish fillet is 150-200g, vegetables are typically 50-100g per serving. Use clear food names, try to identify specific meat cuts or vegetable types when possible. Return ONLY a valid JSON array with this exact format: [{\"foodName\": \"Rice\", \"estimatedPortion\": 180, \"portionUnit\": \"g\"}, {\"foodName\": \"Fried Chicken\", \"estimatedPortion\": 120, \"portionUnit\": \"g\"}]. Do not include any markdown formatting or code blocks, just the raw JSON array."),
                    Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
            ));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // Remove markdown code block formatting if present
            String cleanedContent = content.trim();
            if (cleanedContent.startsWith("```json")) {
                cleanedContent = cleanedContent.substring(7); // Remove ```json
            } else if (cleanedContent.startsWith("```")) {
                cleanedContent = cleanedContent.substring(3); // Remove ```
            }
            if (cleanedContent.endsWith("```")) {
                cleanedContent = cleanedContent.substring(0, cleanedContent.length() - 3); // Remove trailing ```
            }
            cleanedContent = cleanedContent.trim();

            return CompletableFuture.completedFuture(cleanedContent);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
