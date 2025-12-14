package com.nutrition.tracker.repository;

import com.nutrition.tracker.entity.Meal;
import com.nutrition.tracker.entity.MealFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MealFoodRepository extends JpaRepository<MealFood, Long> {

    List<MealFood> findByMeal(Meal meal);

    void deleteByMeal(Meal meal);
}
