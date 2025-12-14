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

        JsonNode nutrients = foodNode.path("foodNutrients");
        for (JsonNode nutrient : nutrients) {
            String nutrientName = nutrient.path("nutrientName").asText();
            double value = nutrient.path("value").asDouble(0.0);

            switch (nutrientName) {
                case "Energy" -> food.setCalories(value);
                case "Protein" -> food.setProtein(value);
                case "Carbohydrate, by difference" -> food.setCarbohydrates(value);
                case "Total lipid (fat)" -> food.setFat(value);
                case "Fiber, total dietary" -> food.setFiber(value);
                case "Sugars, total including NLEA" -> food.setSugar(value);
                case "Sodium, Na" -> food.setSodium(value);
                case "Cholesterol" -> food.setCholesterol(value);
            }
        }

        food.setServingSize(foodNode.path("servingSize").asDouble(100.0));
        food.setServingUnit(foodNode.path("servingSizeUnit").asText("g"));

        return food;
    }
}
