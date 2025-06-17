package com.myapp.diet.controller;

import com.myapp.diet.AOP.UserContextHolder;
import com.myapp.diet.Service.NutritionService;
import com.myapp.diet.entity.DailyNutrition;
import com.myapp.diet.entity.Food;
import com.myapp.diet.entity.FoodRecord;
import com.myapp.diet.entity.User;
import com.myapp.diet.repository.DailyNutritionRepository;
import com.myapp.diet.repository.FoodRecordRepository;
import com.myapp.diet.repository.UserRepository;
import com.myapp.diet.repository.FoodRepository;
import com.myapp.diet.security.CustomUserDetails;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food-record")
public class FoodRecordController {

    @Autowired
    private FoodRecordRepository foodRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyNutritionRepository dailyNutritionRepository;

    @Autowired
    private NutritionService nutritionService;

    @Autowired
    private FoodRepository foodRepository;


    // 新增食物紀錄
    @PostMapping("/{userId}")
    public ResponseEntity<?> addFoodRecord(
            @RequestBody FoodRecordDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId=userDetails.getUser().getId();
//        Long userId = UserContextHolder.getUserId();
        // 找使用者的每日目標卡路里
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 從資料庫找 Food，如果找不到可以回傳錯誤
        Food food = foodRepository.findByName(dto.getFoodName())
                .orElseGet(() -> {
                    Food newFood = new Food();
                    newFood.setName(dto.getFoodName());
                    newFood.setCalories(dto.getCalories() != null ? dto.getCalories() : 0);
                    return foodRepository.save(newFood);
                });
        Integer dailyGoal=user.getDailyCalorieGoal();
        LocalDate date = dto.getDate() != null ? dto.getDate() : LocalDate.now();

        // 新增食物紀錄
        FoodRecord record = new FoodRecord();
        record.setUser(user);
        record.setDate(date);
        record.setFood(food);
        record.setCalories(dto.getCalories());

        foodRecordRepository.save(record);
        // 呼叫 service 抽出的更新方法
        nutritionService.updateDailyNutrition(user, record.getDate());
        Map<String, Object> res = new HashMap<>();
        res.put("food", food.getName());
        res.put("calories", food.getCalories());

        DailyNutrition daily = dailyNutritionRepository.findByUserAndDate(user, record.getDate())
                .orElseThrow(() -> new RuntimeException("Daily nutrition not found"));

        res.put("totalCalories", daily.getTotalCalories());
        res.put("remainingCalories", daily.getRemainingCalories());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{userId}/date/{date}")
    public ResponseEntity<?> getDailyNutritionWithRecords(
//            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = UserContextHolder.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 從 DailyNutrition 找該日彙總資料
        DailyNutrition dailyNutrition = dailyNutritionRepository.findByUserAndDate(user, date)
                .orElse(null);

        // 如果沒有彙總資料，也嘗試用空的回應避免錯誤
        Integer totalCalories = dailyNutrition != null ? dailyNutrition.getTotalCalories() : 0;
        Integer goalCalories = dailyNutrition != null ? dailyNutrition.getGoalCalories() : null;
        Integer remainingCalories = dailyNutrition != null ? dailyNutrition.getRemainingCalories() : null;

        // 找該日食物細節清單
        List<FoodRecord> records = foodRecordRepository.findByUserAndDate(user, date);
        List<Map<String, Object>> recordList = records.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",r.getRecordId());
            m.put("foodName", r.getFood().getName());
            m.put("calories", r.getCalories());
            return m;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("records", recordList);
        response.put("totalCalories", totalCalories);
        response.put("goalCalories", goalCalories);
        response.put("remainingCalories", remainingCalories);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateFoodRecord(
            @PathVariable Long recordId,
            @RequestBody FoodRecordDTO dto) {

        FoodRecord record = foodRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Food record not found"));
        Food food = foodRepository.findByName(dto.getFoodName())
                .orElseGet(() -> {
                    Food newFood = new Food();
                    newFood.setName(dto.getFoodName());
                    newFood.setCalories(dto.getCalories() != null ? dto.getCalories() : 0);
                    return foodRepository.save(newFood);
                });
        record.setFood(food);
        record.setCalories(dto.getCalories());

        foodRecordRepository.save(record);

        nutritionService.updateDailyNutrition(record.getUser(), record.getDate());

        return ResponseEntity.ok("Food record updated");
    }
    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteFoodRecord(@PathVariable Long recordId) {
        FoodRecord record = foodRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Food record not found"));

        User user = record.getUser();
        LocalDate date = record.getDate();

        foodRecordRepository.delete(record);

        // 更新當日的 DailyNutrition 紀錄
        nutritionService.updateDailyNutrition(user, date);

        return ResponseEntity.ok("Food record deleted");
    }



    // DTO 類別
    @Data
    public static class FoodRecordDTO {
        private String foodName;
        private Integer calories;
        private LocalDate date;
    }
}

