package com.myapp.diet.repository;

import com.myapp.diet.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByName(String name);

    List<Food> findByNameContainingIgnoreCase(String keyword);
}

