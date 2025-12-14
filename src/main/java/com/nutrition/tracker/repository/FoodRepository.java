package com.nutrition.tracker.repository;

import com.nutrition.tracker.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findByBarcode(String barcode);

    Optional<Food> findByFdcId(String fdcId);

    List<Food> findByNameContainingIgnoreCase(String name);

    List<Food> findBySource(Food.FoodSource source);
}
