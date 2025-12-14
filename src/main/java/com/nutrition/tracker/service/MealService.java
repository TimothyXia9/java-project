package com.nutrition.tracker.service;

import com.nutrition.tracker.dto.MealRequest;
import com.nutrition.tracker.entity.Food;
import com.nutrition.tracker.entity.Meal;
import com.nutrition.tracker.entity.MealFood;
import com.nutrition.tracker.entity.User;
import com.nutrition.tracker.repository.FoodRepository;
import com.nutrition.tracker.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Meal createMeal(MealRequest request) {
        User user = userService.getCurrentUser();

        Meal meal = new Meal();
        meal.setUser(user);
        meal.setMealType(request.getMealType());
        meal.setMealDate(request.getMealDate());
        meal.setNotes(request.getNotes());

        if (request.getFoods() != null && !request.getFoods().isEmpty()) {
            for (MealRequest.MealFoodRequest foodRequest : request.getFoods()) {
                Food food = foodRepository.findById(foodRequest.getFoodId())
                        .orElseThrow(() -> new RuntimeException("Food not found"));

                MealFood mealFood = new MealFood();
                mealFood.setMeal(meal);
                mealFood.setFood(food);
                mealFood.setQuantity(foodRequest.getQuantity());
                mealFood.setQuantityUnit(foodRequest.getQuantityUnit());
                mealFood.setServings(foodRequest.getServings());

                meal.getMealFoods().add(mealFood);
            }
        }

        return mealRepository.save(meal);
    }

    public List<Meal> getMealsByDate(LocalDate date) {
        User user = userService.getCurrentUser();
        return mealRepository.findByUserAndMealDate(user, date);
    }

    public List<Meal> getMealsByDateRange(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        return mealRepository.findByUserAndMealDateBetween(user, startDate, endDate);
    }

    public Meal getMealById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        User currentUser = userService.getCurrentUser();
        if (!meal.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to meal");
        }

        return meal;
    }

    @Transactional
    public void deleteMeal(Long id) {
        Meal meal = getMealById(id);
        mealRepository.delete(meal);
    }
}
