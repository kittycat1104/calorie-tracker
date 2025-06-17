package com.myapp.diet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Double height;

    private Double weight;

    private Integer age;

    private Double activityLevel;

    private String gender;

    private String goal;

    private Integer tdee;

    // 🔥 每週想減/增的公斤數（正負皆可）
    private Double weeklyWeightGoal;

    private Integer calorieDeficit;
//
    private Integer dailyCalorieGoal;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyNutrition> dailyNutritions;

    // User 和 FoodRecord 是一對多關係
    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodRecord> foodRecords;


}
