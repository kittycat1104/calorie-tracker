package com.myapp.diet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food_record")
public class FoodRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;
    // 原本是 String foodName，現在改成關聯 Food 表
    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    private Integer calories;  // 熱量（kcal）
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 對應 User 實體

}
