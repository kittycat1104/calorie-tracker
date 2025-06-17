package com.myapp.diet.controller;

import com.myapp.diet.Service.NutritionService;
import com.myapp.diet.entity.Food;
import com.myapp.diet.repository.FoodRepository;
import com.myapp.diet.security.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodRepository foodRepository;
    private final NutritionService nutritionService;

    public FoodController(FoodRepository foodRepository, NutritionService nutritionService) {
        this.foodRepository = foodRepository;
        this.nutritionService = nutritionService;
    }

    // 取得所有食物
    @GetMapping
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    // 根據 id 查詢單筆食物
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        return foodRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 新增食物
    @PostMapping
    public ResponseEntity<?> createFood(@RequestBody FoodDTO dto) {
        // 檢查是否有重複名稱
        if (foodRepository.findByName(dto.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Food with the same name already exists.");
        }

        Food food = new Food();
        food.setName(dto.getName());
        food.setCalories(dto.getCalories());

        foodRepository.save(food);

        return ResponseEntity.status(HttpStatus.CREATED).body(food);
    }

    // 更新食物（根據 id）
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFood(@PathVariable Long id, @RequestBody FoodDTO dto) {
        return foodRepository.findById(id).map(food -> {
            // 可選：檢查新名稱是否重複（且不是本身的名稱）
//            if (!food.getName().equals(dto.getName()) &&
//                    foodRepository.findByName(dto.getName()).isPresent()) {
//                return ResponseEntity.badRequest().body("Food with the same name already exists.");
//            }

            food.setName(dto.getName());
            food.setCalories(dto.getCalories());
            foodRepository.save(food);
            return ResponseEntity.ok(food);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 刪除食物
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable Long id) {
        return foodRepository.findById(id).map(food -> {
            foodRepository.delete(food);
            return ResponseEntity.ok("Food deleted");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchFoods(@RequestParam String keyword) {
        List<Food> results = foodRepository.findByNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/weekly-calories")
    public ResponseEntity<List<DailyCaloriesDTO>> getWeeklyCalories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = userDetails.getUser().getId();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<DailyCaloriesDTO> result = nutritionService.getWeeklyCalories(userId,targetDate);
        return ResponseEntity.ok(result);
    }

    @Data
    public static class FoodDTO {
        private String name;
        private Integer calories;
    }

    @Data
    @AllArgsConstructor
    public static class DailyCaloriesDTO{
        private LocalDate date;
        private Integer totalCalories;
    }
}
