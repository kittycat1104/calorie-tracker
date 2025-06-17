package com.myapp.diet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_food")
public class DailyNutrition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DailyId;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    private FoodRecord foodRecord;
    private LocalDate date;
    private Integer totalCalories;
    private Integer goalCalories;
    private Integer remainingCalories;
    private LocalDateTime createdAt = LocalDateTime.now();


}
