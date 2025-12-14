package com.nutrition.tracker.repository;

import com.nutrition.tracker.entity.Meal;
import com.nutrition.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByUserAndMealDate(User user, LocalDate mealDate);

    List<Meal> findByUserAndMealDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Meal> findByUserAndMealType(User user, Meal.MealType mealType);

    List<Meal> findByUserOrderByMealDateDesc(User user);
}
