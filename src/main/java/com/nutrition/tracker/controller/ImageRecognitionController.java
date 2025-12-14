package com.nutrition.tracker.controller;

import com.nutrition.tracker.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/image")
public class ImageRecognitionController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/analyze")
    public CompletableFuture<ResponseEntity<String>> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            return openAIService.analyzeImage(base64Image)
                    .thenApply(ResponseEntity::ok)
                    .exceptionally(e -> {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("401")) {
                            return ResponseEntity.status(400).body("OpenAI API Key is not configured or invalid. Please configure your OPENAI_API_KEY in application.yml or environment variables.");
                        } else if (errorMsg != null && errorMsg.contains("429")) {
                            return ResponseEntity.status(429).body("OpenAI API rate limit exceeded. Please try again later.");
                        }
                        return ResponseEntity.internalServerError().body("Error analyzing image: " + errorMsg);
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage())
            );
        }
    }
}
