package com.nutrition.tracker.controller;

import com.nutrition.tracker.dto.BarcodeResponse;
import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.service.FoodService;
import com.nutrition.tracker.service.OpenFoodFactsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * REST Controller for barcode scanning functionality.
 * Provides endpoints to scan product barcodes and retrieve nutritional information.
 */
@RestController
@RequestMapping("/api/barcode")
@Tag(name = "Barcode Scanning", description = "APIs for scanning product barcodes and retrieving nutritional information")
public class BarcodeController {

    private static final Logger logger = LoggerFactory.getLogger(BarcodeController.class);
    private static final long API_TIMEOUT_SECONDS = 10;

    @Autowired
    private OpenFoodFactsService openFoodFactsService;

    @Autowired
    private FoodService foodService;

    /**
     * Scans a barcode and retrieves product information.
     * First checks local database, then queries Open Food Facts API if not found locally.
     *
     * @param barcode Product barcode (EAN-13, UPC-A, etc.)
     * @return BarcodeResponse containing product information
     */
    @GetMapping("/{barcode}")
    @Operation(
        summary = "Scan barcode",
        description = "Retrieve product information by scanning a barcode. Supports EAN-13, UPC-A, EAN-8, and other standard formats."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found in database"),
        @ApiResponse(responseCode = "400", description = "Invalid barcode format"),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "504", description = "API timeout")
    })
    public CompletableFuture<ResponseEntity<BarcodeResponse>> scanBarcode(
            @Parameter(description = "Product barcode (8-14 digits)", example = "3017620422003")
            @PathVariable String barcode) {

        logger.info("Barcode scan request received: {}", barcode);

        // Validate barcode format
        if (!isValidBarcodeFormat(barcode)) {
            logger.warn("Invalid barcode format received: {}", barcode);
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(BarcodeResponse.error(barcode, "Invalid barcode format. Must be 8-14 digits."))
            );
        }

        // Check local database first (synchronous, fast)
        Food existingFood = foodService.getFoodByBarcode(barcode);
        if (existingFood != null) {
            logger.info("Product found in local database: {} ({})", existingFood.getName(), barcode);
            return CompletableFuture.completedFuture(
                ResponseEntity.ok(BarcodeResponse.success(existingFood))
            );
        }

        // Query external API (asynchronous)
        logger.info("Product not in local database, querying Open Food Facts API: {}", barcode);

        return openFoodFactsService.getFoodByBarcode(barcode)
                .orTimeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenApply(food -> {
                    if (food == null) {
                        logger.info("Product not found in Open Food Facts: {}", barcode);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(BarcodeResponse.notFound(barcode));
                    }

                    try {
                        // Save to local database for future queries
                        Food savedFood = foodService.createFood(food);
                        logger.info("Product saved to database: {} ({})", savedFood.getName(), barcode);
                        return ResponseEntity.ok(BarcodeResponse.success(savedFood));
                    } catch (Exception e) {
                        logger.error("Error saving food to database: {}", e.getMessage(), e);
                        // Return the food data even if save fails
                        return ResponseEntity.ok(BarcodeResponse.success(food));
                    }
                })
                .exceptionally(e -> {
                    logger.error("Error processing barcode {}: {}", barcode, e.getMessage(), e);

                    // Check for timeout
                    if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                                .body(BarcodeResponse.error(barcode, "API request timeout. Please try again."));
                    }

                    // Generic error response
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(BarcodeResponse.error(barcode, "Failed to retrieve product information: " + e.getMessage()));
                });
    }

    /**
     * Validates barcode format.
     * Accepts numeric strings of length 8-14 (common barcode formats).
     *
     * @param barcode Barcode string to validate
     * @return true if valid format
     */
    private boolean isValidBarcodeFormat(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return false;
        }

        String trimmed = barcode.trim();

        // Check if numeric
        if (!trimmed.matches("\\d+")) {
            return false;
        }

        // Check length (8-14 digits)
        int length = trimmed.length();
        return length >= 8 && length <= 14;
    }
}
