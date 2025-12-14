package com.nutrition.tracker.controller;

import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping
    public ResponseEntity<List<Food>> getAllFoods() {
        List<Food> foods = foodService.getAllFoods();
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFood(@PathVariable Long id) {
        Food food = foodService.getFoodById(id);
        return ResponseEntity.ok(food);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchFoods(@RequestParam String name) {
        List<Food> foods = foodService.searchFoodsByName(name);
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Food> getFoodByBarcode(@PathVariable String barcode) {
        Food food = foodService.getFoodByBarcode(barcode);
        if (food == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(food);
    }

    @PostMapping
    public ResponseEntity<Food> createFood(@RequestBody Food food) {
        Food createdFood = foodService.createFood(food);
        return ResponseEntity.ok(createdFood);
    }
}
