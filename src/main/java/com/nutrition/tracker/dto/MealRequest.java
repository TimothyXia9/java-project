package com.nutrition.tracker.dto;

import com.nutrition.tracker.entity.Meal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MealRequest {

    @NotNull(message = "Meal type is required")
    private Meal.MealType mealType;

    @NotNull(message = "Meal date is required")
    private LocalDate mealDate;

    private String notes;

    private List<MealFoodRequest> foods;

    @Data
    public static class MealFoodRequest {
        @NotNull(message = "Food ID is required")
        private Long foodId;

        @NotNull(message = "Quantity is required")
        private Double quantity;

        private String quantityUnit;

        private Double servings;
    }
}
