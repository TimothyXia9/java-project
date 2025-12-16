package com.nutrition.tracker.controller;

import com.nutrition.tracker.dto.BarcodeResponse;
import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.service.FoodService;
import com.nutrition.tracker.service.OpenFoodFactsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarcodeControllerTest {

    @Mock
    private OpenFoodFactsService openFoodFactsService;

    @Mock
    private FoodService foodService;

    @InjectMocks
    private BarcodeController barcodeController;

    private Food testFood;
    private static final String VALID_BARCODE = "3017620422003";

    @BeforeEach
    void setUp() {
        testFood = new Food();
        testFood.setId(1L);
        testFood.setBarcode(VALID_BARCODE);
        testFood.setName("Nutella");
        testFood.setBrand("Ferrero");
        testFood.setSource(Food.FoodSource.OPENFOODFACTS);
        testFood.setCalories(539.0);
        testFood.setProtein(6.3);
        testFood.setCarbohydrates(57.5);
        testFood.setFat(30.9);
        testFood.setServingSize(100.0);
        testFood.setServingUnit("g");
    }

    @Test
    void testScanBarcode_FoundInLocalDatabase() throws Exception {
        // Setup
        when(foodService.getFoodByBarcode(VALID_BARCODE)).thenReturn(testFood);

        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode(VALID_BARCODE);
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isFound());
        assertEquals(VALID_BARCODE, response.getBody().getBarcode());
        assertEquals("Nutella", response.getBody().getFood().getName());

        // OpenFoodFactsService should not be called since product is in local DB
        verify(openFoodFactsService, never()).getFoodByBarcode(anyString());
    }

    @Test
    void testScanBarcode_NotInLocalDB_FoundInAPI() throws Exception {
        // Setup
        when(foodService.getFoodByBarcode(VALID_BARCODE)).thenReturn(null);
        when(openFoodFactsService.getFoodByBarcode(VALID_BARCODE))
                .thenReturn(CompletableFuture.completedFuture(testFood));
        when(foodService.createFood(any(Food.class))).thenReturn(testFood);

        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode(VALID_BARCODE);
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isFound());
        assertEquals(VALID_BARCODE, response.getBody().getBarcode());

        // Verify that product was saved to database
        verify(foodService).createFood(any(Food.class));
        verify(openFoodFactsService).getFoodByBarcode(VALID_BARCODE);
    }

    @Test
    void testScanBarcode_NotFound() throws Exception {
        // Setup
        when(foodService.getFoodByBarcode(VALID_BARCODE)).thenReturn(null);
        when(openFoodFactsService.getFoodByBarcode(VALID_BARCODE))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode(VALID_BARCODE);
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isFound());
        assertEquals(VALID_BARCODE, response.getBody().getBarcode());
        assertTrue(response.getBody().getMessage().contains("not found"));
    }

    @Test
    void testScanBarcode_InvalidFormat_Letters() throws Exception {
        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode("ABC123");
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isFound());
        assertTrue(response.getBody().getMessage().contains("Invalid barcode format"));

        // No service calls should be made
        verify(foodService, never()).getFoodByBarcode(anyString());
        verify(openFoodFactsService, never()).getFoodByBarcode(anyString());
    }

    @Test
    void testScanBarcode_InvalidFormat_TooShort() throws Exception {
        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode("1234567");
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isFound());
    }

    @Test
    void testScanBarcode_InvalidFormat_TooLong() throws Exception {
        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode("123456789012345");
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isFound());
    }

    @Test
    void testScanBarcode_APIError() throws Exception {
        // Setup
        when(foodService.getFoodByBarcode(VALID_BARCODE)).thenReturn(null);
        when(openFoodFactsService.getFoodByBarcode(VALID_BARCODE))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API Error")));

        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode(VALID_BARCODE);
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isFound());
        assertTrue(response.getBody().getMessage().contains("Failed to retrieve"));
    }

    @Test
    void testScanBarcode_SaveFailsButReturnsData() throws Exception {
        // Setup - simulate save failure but API call success
        when(foodService.getFoodByBarcode(VALID_BARCODE)).thenReturn(null);
        when(openFoodFactsService.getFoodByBarcode(VALID_BARCODE))
                .thenReturn(CompletableFuture.completedFuture(testFood));
        when(foodService.createFood(any(Food.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode(VALID_BARCODE);
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify - should still return OK with the food data
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isFound());
        assertEquals("Nutella", response.getBody().getFood().getName());
    }

    @Test
    void testScanBarcode_ValidFormats() throws Exception {
        // Test various valid barcode formats
        String[] validBarcodes = {
            "12345678",      // EAN-8
            "123456789012",  // UPC-A
            "1234567890123", // EAN-13
            "12345678901234" // ITF-14
        };

        for (String barcode : validBarcodes) {
            Food food = new Food();
            food.setBarcode(barcode);
            food.setName("Product " + barcode);
            food.setCalories(100.0);

            when(foodService.getFoodByBarcode(barcode)).thenReturn(food);

            CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                    barcodeController.scanBarcode(barcode);
            ResponseEntity<BarcodeResponse> response = future.get();

            assertEquals(HttpStatus.OK, response.getStatusCode(),
                    "Barcode length " + barcode.length() + " should be valid");
            assertTrue(response.getBody().isFound());
        }
    }

    @Test
    void testScanBarcode_NullBarcode() throws Exception {
        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode(null);
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isFound());
    }

    @Test
    void testScanBarcode_EmptyBarcode() throws Exception {
        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode("");
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isFound());
    }

    @Test
    void testScanBarcode_WhitespaceBarcode() throws Exception {
        // Execute
        CompletableFuture<ResponseEntity<BarcodeResponse>> future =
                barcodeController.scanBarcode("   ");
        ResponseEntity<BarcodeResponse> response = future.get();

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isFound());
    }
}
