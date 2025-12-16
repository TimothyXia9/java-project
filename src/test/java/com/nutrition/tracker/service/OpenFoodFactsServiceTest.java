package com.nutrition.tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrition.tracker.entity.Food;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenFoodFactsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OpenFoodFactsService openFoodFactsService;

    private static final String API_URL = "https://world.openfoodfacts.org/api/v0";
    private static final String VALID_BARCODE = "3017620422003"; // Nutella barcode

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openFoodFactsService, "apiUrl", API_URL);
    }

    @Test
    void testGetFoodByBarcode_Success() throws Exception {
        // Prepare mock response
        String jsonResponse = """
            {
                "status": 1,
                "product": {
                    "product_name": "Nutella",
                    "brands": "Ferrero",
                    "generic_name": "Hazelnut spread with cocoa",
                    "categories": "Spreads",
                    "image_url": "https://example.com/nutella.jpg",
                    "nutriments": {
                        "energy-kcal_100g": 539,
                        "proteins_100g": 6.3,
                        "carbohydrates_100g": 57.5,
                        "sugars_100g": 56.3,
                        "fat_100g": 30.9,
                        "fiber_100g": 0,
                        "sodium_100g": 0.107,
                        "cholesterol_100g": 0
                    },
                    "serving_quantity_unit": "g"
                }
            }
            """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);
        when(objectMapper.readTree(jsonResponse)).thenCallRealMethod();

        // Use real ObjectMapper for this test
        ReflectionTestUtils.setField(openFoodFactsService, "objectMapper", new ObjectMapper());

        // Execute
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode(VALID_BARCODE);
        Food result = future.get();

        // Verify
        assertNotNull(result);
        assertEquals("Nutella", result.getName());
        assertEquals("Ferrero", result.getBrand());
        assertEquals(VALID_BARCODE, result.getBarcode());
        assertEquals(Food.FoodSource.OPENFOODFACTS, result.getSource());
        assertEquals(539.0, result.getCalories());
        assertEquals(6.3, result.getProtein());
        assertEquals(57.5, result.getCarbohydrates());
        assertEquals(30.9, result.getFat());
        assertEquals(56.3, result.getSugar());
    }

    @Test
    void testGetFoodByBarcode_NotFound() throws Exception {
        // Prepare mock response for product not found
        String jsonResponse = """
            {
                "status": 0
            }
            """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);
        when(objectMapper.readTree(jsonResponse)).thenCallRealMethod();

        // Use real ObjectMapper
        ReflectionTestUtils.setField(openFoodFactsService, "objectMapper", new ObjectMapper());

        // Execute
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode("9999999999999");
        Food result = future.get();

        // Verify
        assertNull(result);
    }

    @Test
    void testGetFoodByBarcode_InvalidBarcode() throws Exception {
        // Execute with invalid barcode
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode("invalid");
        Food result = future.get();

        // Verify
        assertNull(result);
    }

    @Test
    void testGetFoodByBarcode_NullBarcode() throws Exception {
        // Execute with null barcode
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode(null);
        Food result = future.get();

        // Verify
        assertNull(result);
    }

    @Test
    void testGetFoodByBarcode_EmptyBarcode() throws Exception {
        // Execute with empty barcode
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode("");
        Food result = future.get();

        // Verify
        assertNull(result);
    }

    @Test
    void testGetFoodByBarcode_HttpError() {
        // Mock HTTP error
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Not Found",
                        null,
                        null,
                        null
                ));

        // Execute
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode(VALID_BARCODE);

        // Verify exception is thrown
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void testGetFoodByBarcode_NetworkError() {
        // Mock network error
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Network error"));

        // Execute
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode(VALID_BARCODE);

        // Verify exception is thrown
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void testGetFoodByBarcode_WithAlternativeEnergyField() throws Exception {
        // Test when energy-kcal is not available but energy-kj is
        String jsonResponse = """
            {
                "status": 1,
                "product": {
                    "product_name": "Test Product",
                    "brands": "Test Brand",
                    "nutriments": {
                        "energy-kj_100g": 2255,
                        "proteins_100g": 5.0,
                        "carbohydrates_100g": 50.0,
                        "fat_100g": 25.0
                    },
                    "serving_quantity_unit": "g"
                }
            }
            """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);
        ReflectionTestUtils.setField(openFoodFactsService, "objectMapper", new ObjectMapper());

        // Execute
        CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode(VALID_BARCODE);
        Food result = future.get();

        // Verify - energy-kj should be converted to kcal (2255 kJ / 4.184 ≈ 539 kcal)
        assertNotNull(result);
        assertTrue(result.getCalories() > 530 && result.getCalories() < 550);
    }

    @Test
    void testGetFoodByBarcode_ValidBarcodeFormats() throws Exception {
        // Test different valid barcode lengths
        String[] validBarcodes = {"12345678", "123456789012", "1234567890123", "12345678901234"};

        for (String barcode : validBarcodes) {
            String jsonResponse = String.format("""
                {
                    "status": 1,
                    "product": {
                        "product_name": "Product %s",
                        "brands": "Brand",
                        "nutriments": {
                            "energy-kcal_100g": 100,
                            "proteins_100g": 5.0,
                            "carbohydrates_100g": 10.0,
                            "fat_100g": 3.0
                        },
                        "serving_quantity_unit": "g"
                    }
                }
                """, barcode);

            when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);
            ReflectionTestUtils.setField(openFoodFactsService, "objectMapper", new ObjectMapper());

            CompletableFuture<Food> future = openFoodFactsService.getFoodByBarcode(barcode);
            Food result = future.get();

            assertNotNull(result, "Barcode length " + barcode.length() + " should be valid");
            assertEquals(barcode, result.getBarcode());
        }
    }
}
