package com.nutrition.tracker.controller;

import com.nutrition.tracker.dto.MealRequest;
import com.nutrition.tracker.entity.Meal;
import com.nutrition.tracker.service.MealService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    @PostMapping
    public ResponseEntity<Meal> createMeal(@Valid @RequestBody MealRequest request) {
        Meal meal = mealService.createMeal(request);
        return ResponseEntity.ok(meal);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Meal>> getMealsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Meal> meals = mealService.getMealsByDate(date);
        return ResponseEntity.ok(meals);
    }

    @GetMapping("/range")
    public ResponseEntity<List<Meal>> getMealsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Meal> meals = mealService.getMealsByDateRange(startDate, endDate);
        return ResponseEntity.ok(meals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meal> getMeal(@PathVariable Long id) {
        Meal meal = mealService.getMealById(id);
        return ResponseEntity.ok(meal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}
