package com.nutrition.tracker.repository;

import com.nutrition.tracker.entity.Meal;
import com.nutrition.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    @Query("SELECT DISTINCT m FROM Meal m " +
           "LEFT JOIN FETCH m.mealFoods mf " +
           "LEFT JOIN FETCH mf.food " +
           "WHERE m.user = :user AND m.mealDate = :mealDate")
    List<Meal> findByUserAndMealDateWithFoods(@Param("user") User user, @Param("mealDate") LocalDate mealDate);

    @Query("SELECT DISTINCT m FROM Meal m " +
           "LEFT JOIN FETCH m.mealFoods mf " +
           "LEFT JOIN FETCH mf.food " +
           "WHERE m.user = :user AND m.mealDate BETWEEN :startDate AND :endDate")
    List<Meal> findByUserAndMealDateBetweenWithFoods(@Param("user") User user,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    List<Meal> findByUserAndMealDate(User user, LocalDate mealDate);

    List<Meal> findByUserAndMealDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Meal> findByUserAndMealType(User user, Meal.MealType mealType);

    List<Meal> findByUserOrderByMealDateDesc(User user);
}
