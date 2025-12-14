package com.nutrition.tracker.controller;

import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.service.FoodService;
import com.nutrition.tracker.service.OpenFoodFactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/barcode")
public class BarcodeController {

    @Autowired
    private OpenFoodFactsService openFoodFactsService;

    @Autowired
    private FoodService foodService;

    @GetMapping("/{barcode}")
    public CompletableFuture<ResponseEntity<Food>> getFoodByBarcode(@PathVariable String barcode) {
        Food existingFood = foodService.getFoodByBarcode(barcode);
        if (existingFood != null) {
            return CompletableFuture.completedFuture(ResponseEntity.ok(existingFood));
        }

        return openFoodFactsService.getFoodByBarcode(barcode)
                .thenApply(food -> {
                    if (food == null) {
                        return ResponseEntity.notFound().<Food>build();
                    }
                    Food savedFood = foodService.createFood(food);
                    return ResponseEntity.ok(savedFood);
                })
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }
}
