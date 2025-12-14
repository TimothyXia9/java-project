package com.nutrition.tracker.service;

import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    public Food createFood(Food food) {
        return foodRepository.save(food);
    }

    public Food getFoodById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found"));
    }

    public List<Food> searchFoodsByName(String name) {
        return foodRepository.findByNameContainingIgnoreCase(name);
    }

    public Food getFoodByBarcode(String barcode) {
        return foodRepository.findByBarcode(barcode)
                .orElse(null);
    }

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }
}
