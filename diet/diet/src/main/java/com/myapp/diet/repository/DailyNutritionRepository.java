package com.myapp.diet.repository;

import com.myapp.diet.entity.DailyNutrition;

import com.myapp.diet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface DailyNutritionRepository extends JpaRepository<DailyNutrition, Long> {
    Optional<DailyNutrition> findByUserAndDate(User user, LocalDate date);

    List<DailyNutrition> findByUserAndDateGreaterThanEqual(User profile, LocalDate now);

    List<DailyNutrition> findByUserIdAndDateBetween(Long userId, LocalDate startOfWeek, LocalDate endOfWeek);
}

