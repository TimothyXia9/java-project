package com.nutrition.tracker.service;

import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private USDAService usdaService;

    public Food createFood(Food food) {
        return foodRepository.save(food);
    }

    public Food getFoodById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found"));
    }

    public List<Food> searchFoodsByName(String name) {
        // Search local database first
        List<Food> localResults = foodRepository.findByNameContainingIgnoreCase(name);

        // If local database has results, return them
        if (!localResults.isEmpty()) {
            return localResults;
        }

        // Otherwise, search USDA API
        try {
            CompletableFuture<List<Food>> usdaFuture = usdaService.searchFood(name);
            List<Food> usdaResults = usdaFuture.get(); // Wait for async result

            // Save USDA results to database for future queries
            for (Food food : usdaResults) {
                try {
                    foodRepository.save(food);
                } catch (Exception e) {
                    // Ignore duplicate save errors
                }
            }

            return usdaResults;
        } catch (Exception e) {
            // If USDA API fails, return empty list
            return new ArrayList<>();
        }
    }

    public Food getFoodByBarcode(String barcode) {
        return foodRepository.findByBarcode(barcode)
                .orElse(null);
    }

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }
}
