package com.nutrition.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrition.tracker.entity.Food;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class USDAService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.usda.key}")
    private String apiKey;

    @Value("${api.usda.url}")
    private String apiUrl;

    @Async
    public CompletableFuture<List<Food>> searchFood(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/foods/search")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", query)
                    .queryParam("pageSize", 10)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode foods = root.path("foods");

            List<Food> foodList = new ArrayList<>();
            for (JsonNode foodNode : foods) {
                Food food = parseUSDAFood(foodNode);
                foodList.add(food);
            }

            return CompletableFuture.completedFuture(foodList);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<Food> getFoodById(String fdcId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/food/" + fdcId)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode foodNode = objectMapper.readTree(response);

            Food food = parseUSDAFood(foodNode);
            return CompletableFuture.completedFuture(food);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private Food parseUSDAFood(JsonNode foodNode) {
        Food food = new Food();
        food.setFdcId(foodNode.path("fdcId").asText());
        food.setName(foodNode.path("description").asText());
        food.setBrand(foodNode.path("brandOwner").asText(null));
        food.setSource(Food.FoodSource.USDA);

        // Get original serving size to calculate scaling factor
        double originalServingSize = foodNode.path("servingSize").asDouble(100.0);
        String originalServingUnit = foodNode.path("servingSizeUnit").asText("g");

        // Calculate scaling factor to normalize to 100g
        double scalingFactor = 1.0;
        if ("g".equalsIgnoreCase(originalServingUnit)) {
            scalingFactor = 100.0 / originalServingSize;
        } else if ("mg".equalsIgnoreCase(originalServingUnit)) {
            scalingFactor = 100000.0 / originalServingSize; // 100g = 100000mg
        }
        // For other units (oz, lb, etc.), keep as is for now

        JsonNode nutrients = foodNode.path("foodNutrients");
        for (JsonNode nutrient : nutrients) {
            String nutrientName = nutrient.path("nutrientName").asText();
            double value = nutrient.path("value").asDouble(0.0);

            // Scale all nutrient values to per 100g
            double scaledValue = value * scalingFactor;

            switch (nutrientName) {
                case "Energy" -> food.setCalories(scaledValue);
                case "Protein" -> food.setProtein(scaledValue);
                case "Carbohydrate, by difference" -> food.setCarbohydrates(scaledValue);
                case "Total lipid (fat)" -> food.setFat(scaledValue);
                case "Fiber, total dietary" -> food.setFiber(scaledValue);
                case "Sugars, total including NLEA" -> food.setSugar(scaledValue);
                case "Total Sugars" -> { if (food.getSugar() == null || food.getSugar() == 0.0) food.setSugar(scaledValue); }
                case "Sodium, Na" -> food.setSodium(scaledValue);
                case "Cholesterol" -> food.setCholesterol(scaledValue);
            }
        }

        // Always set serving size to 100g
        food.setServingSize(100.0);
        food.setServingUnit("g");

        return food;
    }
}
