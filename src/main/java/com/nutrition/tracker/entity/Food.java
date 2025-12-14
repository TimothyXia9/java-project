package com.nutrition.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String barcode;

    private String fdcId;

    private String brand;

    private Double servingSize;

    private String servingUnit;

    @Column(nullable = false)
    private Double calories;

    private Double protein;

    private Double carbohydrates;

    private Double fat;

    private Double fiber;

    private Double sugar;

    private Double sodium;

    private Double cholesterol;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private FoodSource source;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FoodSource {
        USDA,
        OPENFOODFACTS,
        USER_CREATED,
        AI_RECOGNIZED
    }
}
