package com.nutrition.tracker.dto;

import com.nutrition.tracker.entity.Food;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarcodeResponse {

    private boolean found;
    private String barcode;
    private String message;
    private FoodDTO food;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodDTO {
        private Long id;
        private String name;
        private String brand;
        private String description;
        private String barcode;
        private String imageUrl;
        private String source;

        // Serving information
        private Double servingSize;
        private String servingUnit;

        // Nutritional information (per 100g)
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
        private Double fiber;
        private Double sugar;
        private Double sodium;
        private Double cholesterol;

        public static FoodDTO fromEntity(Food food) {
            return FoodDTO.builder()
                    .id(food.getId())
                    .name(food.getName())
                    .brand(food.getBrand())
                    .description(food.getDescription())
                    .barcode(food.getBarcode())
                    .imageUrl(food.getImageUrl())
                    .source(food.getSource() != null ? food.getSource().toString() : null)
                    .servingSize(food.getServingSize())
                    .servingUnit(food.getServingUnit())
                    .calories(food.getCalories())
                    .protein(food.getProtein())
                    .carbohydrates(food.getCarbohydrates())
                    .fat(food.getFat())
                    .fiber(food.getFiber())
                    .sugar(food.getSugar())
                    .sodium(food.getSodium())
                    .cholesterol(food.getCholesterol())
                    .build();
        }
    }

    public static BarcodeResponse success(Food food) {
        return BarcodeResponse.builder()
                .found(true)
                .barcode(food.getBarcode())
                .message("Product found successfully")
                .food(FoodDTO.fromEntity(food))
                .build();
    }

    public static BarcodeResponse notFound(String barcode) {
        return BarcodeResponse.builder()
                .found(false)
                .barcode(barcode)
                .message("Product not found in database")
                .build();
    }

    public static BarcodeResponse error(String barcode, String errorMessage) {
        return BarcodeResponse.builder()
                .found(false)
                .barcode(barcode)
                .message("Error: " + errorMessage)
                .build();
    }
}
