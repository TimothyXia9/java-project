package com.nutrition.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrition.tracker.entity.Food;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

@Service
public class OpenFoodFactsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.openfoodfacts.url}")
    private String apiUrl;

    @Async
    public CompletableFuture<Food> getFoodByBarcode(String barcode) {
        try {
            String url = apiUrl + "/product/" + barcode + ".json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.path("status").asInt() != 1) {
                return CompletableFuture.completedFuture(null);
            }

            JsonNode product = root.path("product");
            Food food = new Food();
            food.setBarcode(barcode);
            food.setName(product.path("product_name").asText());
            food.setBrand(product.path("brands").asText());
            food.setSource(Food.FoodSource.OPENFOODFACTS);
            food.setImageUrl(product.path("image_url").asText(null));

            JsonNode nutriments = product.path("nutriments");
            food.setCalories(nutriments.path("energy-kcal_100g").asDouble(0.0));
            food.setProtein(nutriments.path("proteins_100g").asDouble(0.0));
            food.setCarbohydrates(nutriments.path("carbohydrates_100g").asDouble(0.0));
            food.setFat(nutriments.path("fat_100g").asDouble(0.0));
            food.setFiber(nutriments.path("fiber_100g").asDouble(0.0));
            food.setSugar(nutriments.path("sugars_100g").asDouble(0.0));
            food.setSodium(nutriments.path("sodium_100g").asDouble(0.0));
            food.setCholesterol(nutriments.path("cholesterol_100g").asDouble(0.0));

            food.setServingSize(100.0);
            food.setServingUnit("g");

            return CompletableFuture.completedFuture(food);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
