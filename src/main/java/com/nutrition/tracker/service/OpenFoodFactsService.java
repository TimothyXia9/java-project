package com.nutrition.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrition.tracker.entity.Food;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;

/**
 * Service for integrating with Open Food Facts API to retrieve product information by barcode.
 * Provides asynchronous barcode scanning functionality.
 */
@Service
public class OpenFoodFactsService {

    private static final Logger logger = LoggerFactory.getLogger(OpenFoodFactsService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.openfoodfacts.url}")
    private String apiUrl;

    /**
     * Retrieves food product information from Open Food Facts API using barcode.
     * This method runs asynchronously to prevent blocking the main thread.
     *
     * @param barcode The barcode string (EAN-13, UPC-A, etc.)
     * @return CompletableFuture containing Food entity or null if not found
     */
    @Async
    public CompletableFuture<Food> getFoodByBarcode(String barcode) {
        logger.info("Fetching product information for barcode: {}", barcode);

        try {
            // Validate barcode format
            if (!isValidBarcode(barcode)) {
                logger.warn("Invalid barcode format: {}", barcode);
                return CompletableFuture.completedFuture(null);
            }

            // Build API URL
            String url = String.format("%s/product/%s.json", apiUrl, barcode);
            logger.debug("Calling Open Food Facts API: {}", url);

            // Make API request
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Check if product was found
            int status = root.path("status").asInt();
            if (status != 1) {
                logger.info("Product not found in Open Food Facts database: {}", barcode);
                return CompletableFuture.completedFuture(null);
            }

            // Parse product information
            JsonNode product = root.path("product");
            Food food = parseProductData(product, barcode);

            logger.info("Successfully retrieved product: {} ({})", food.getName(), barcode);
            return CompletableFuture.completedFuture(food);

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error while fetching barcode {}: {} - {}",
                        barcode, e.getStatusCode(), e.getMessage());
            return CompletableFuture.failedFuture(
                new RuntimeException("Failed to fetch product data: " + e.getMessage()));

        } catch (ResourceAccessException e) {
            logger.error("Network error while fetching barcode {}: {}", barcode, e.getMessage());
            return CompletableFuture.failedFuture(
                new RuntimeException("Network error: Unable to reach Open Food Facts API"));

        } catch (Exception e) {
            logger.error("Unexpected error while processing barcode {}: {}", barcode, e.getMessage(), e);
            return CompletableFuture.failedFuture(
                new RuntimeException("Error processing product data: " + e.getMessage()));
        }
    }

    /**
     * Parses product data from Open Food Facts JSON response.
     *
     * @param product JsonNode containing product information
     * @param barcode The product barcode
     * @return Food entity with parsed data
     */
    private Food parseProductData(JsonNode product, String barcode) {
        Food food = new Food();

        // Basic information
        food.setBarcode(barcode);
        food.setName(getTextOrDefault(product, "product_name", "Unknown Product"));
        food.setBrand(getTextOrDefault(product, "brands", null));
        food.setSource(Food.FoodSource.OPENFOODFACTS);

        // Description - combine generic name and categories
        String genericName = product.path("generic_name").asText("");
        String categories = product.path("categories").asText("");
        String description = !genericName.isEmpty() ? genericName : categories;
        food.setDescription(description);

        // Image URL - try different image fields
        String imageUrl = product.path("image_url").asText(null);
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = product.path("image_front_url").asText(null);
        }
        food.setImageUrl(imageUrl);

        // Parse nutritional information
        JsonNode nutriments = product.path("nutriments");
        parseNutriments(food, nutriments);

        // Serving size information
        Double servingSize = nutriments.path("serving_quantity").asDouble(0.0);
        if (servingSize == 0.0) {
            servingSize = 100.0; // Default to 100g
        }
        food.setServingSize(servingSize);
        food.setServingUnit(product.path("serving_quantity_unit").asText("g"));

        return food;
    }

    /**
     * Parses nutritional information from the nutriments JSON node.
     *
     * @param food Food entity to populate
     * @param nutriments JsonNode containing nutritional data
     */
    private void parseNutriments(Food food, JsonNode nutriments) {
        // Energy (try multiple fields)
        Double calories = nutriments.path("energy-kcal_100g").asDouble(0.0);
        if (calories == 0.0) {
            // Calculate from energy-kj if kcal not available
            Double energyKj = nutriments.path("energy-kj_100g").asDouble(0.0);
            calories = energyKj / 4.184; // Convert kJ to kcal
        }
        food.setCalories(calories);

        // Macronutrients (per 100g)
        food.setProtein(nutriments.path("proteins_100g").asDouble(0.0));
        food.setCarbohydrates(nutriments.path("carbohydrates_100g").asDouble(0.0));
        food.setFat(nutriments.path("fat_100g").asDouble(0.0));

        // Additional nutrients
        food.setFiber(nutriments.path("fiber_100g").asDouble(0.0));
        food.setSugar(nutriments.path("sugars_100g").asDouble(0.0));

        // Sodium (convert from mg to g if needed)
        Double sodium = nutriments.path("sodium_100g").asDouble(0.0);
        if (sodium == 0.0) {
            // Try salt field (salt = sodium * 2.5)
            Double salt = nutriments.path("salt_100g").asDouble(0.0);
            sodium = salt / 2.5;
        }
        food.setSodium(sodium);

        food.setCholesterol(nutriments.path("cholesterol_100g").asDouble(0.0));
    }

    /**
     * Gets text value from JsonNode with a default fallback.
     *
     * @param node JsonNode to extract from
     * @param fieldName Field name to extract
     * @param defaultValue Default value if field is missing or empty
     * @return Text value or default
     */
    private String getTextOrDefault(JsonNode node, String fieldName, String defaultValue) {
        String value = node.path(fieldName).asText(null);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Validates barcode format.
     * Accepts common barcode formats: EAN-13, UPC-A, EAN-8, etc.
     *
     * @param barcode Barcode string to validate
     * @return true if valid format
     */
    private boolean isValidBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return false;
        }

        // Remove any whitespace
        barcode = barcode.trim();

        // Check if it's numeric and has a valid length
        // Common barcode lengths: 8 (EAN-8), 12 (UPC-A), 13 (EAN-13), 14 (ITF-14)
        if (!barcode.matches("\\d+")) {
            return false;
        }

        int length = barcode.length();
        return length == 8 || length == 12 || length == 13 || length == 14;
    }
}
