package com.myapp.diet.controller;


import com.myapp.diet.AOP.UserContextHolder;
import com.myapp.diet.Service.TdeeCalculator;
import com.myapp.diet.entity.DailyNutrition;
import com.myapp.diet.entity.User;
import com.myapp.diet.repository.DailyNutritionRepository;
import com.myapp.diet.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.Binding;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-profile")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyNutritionRepository dailyNutritionRepository;

    @PostMapping("/{userId}")
    public ResponseEntity<?> saveUserProfile(
//            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDTO profileDTO,
            BindingResult bindingResult) {

        Long userId = UserContextHolder.getUserId();
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }


        User profile = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        profile.setAge(profileDTO.getAge());
        profile.setHeight(profileDTO.getHeight());
        profile.setWeight(profileDTO.getWeight());
        profile.setGender(profileDTO.getGender());
        profile.setActivityLevel(profileDTO.getActivityLevel());
        profile.setWeeklyWeightGoal(profileDTO.getWeeklyWeightGoal());

        // 計算 TDEE
        int tdee = TdeeCalculator.calculateTDEE(
                profile.getGender(),
                profile.getWeight(),
                profile.getHeight(),
                profile.getAge(),
                profile.getActivityLevel()
        );
        profile.setTdee(tdee);

        // 計算每日赤字
        int dailyDeficit = TdeeCalculator.convertWeeklyGoalToDailyDeficit(profile.getWeeklyWeightGoal());
        profile.setCalorieDeficit(dailyDeficit);

        // 計算每日卡路里目標
        int dailyGoal = TdeeCalculator.calculateDailyCalorieGoal(tdee, dailyDeficit);
        profile.setDailyCalorieGoal(dailyGoal);

        List<DailyNutrition> list = dailyNutritionRepository
                .findByUserAndDateGreaterThanEqual(profile, LocalDate.now());

        for (DailyNutrition dn : list) {
            dn.setGoalCalories(dailyGoal);
            dn.setRemainingCalories(dailyGoal - dn.getTotalCalories());
        }
        dailyNutritionRepository.saveAll(list);


        userRepository.save(profile);
        UserProfileResponseDTO responseDTO = new UserProfileResponseDTO();
        BeanUtils.copyProperties(profile, responseDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserProfileWithoutCalorieGoal() {
        Long userId = UserContextHolder.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponseDTO responseDTO = new UserProfileResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @Data
    public static class UserProfileDTO {
        @NotNull(message = "年齡不可為空")
        private Integer age;

        @NotNull(message = "身高不可為空")
        private Double height;

        @NotNull(message = "體重不可為空")
        private Double weight;

        @NotNull(message = "性別不可為空")
        private String gender;

        @NotNull(message = "活動量不可為空")
        private Double activityLevel;

        private Double weeklyWeightGoal;
    }

    @Data
    public static class UserProfileResponseDTO
    {   private Long id;
        private String username;
        private String email;
        private Double height;
        private Double weight;
        private Integer age;
        private Double activityLevel;
        private String gender;
        private String goal; // 如果這是字串
        private Integer tdee;
        private Double weeklyWeightGoal;
        private Integer calorieDeficit;
        private Integer dailyCalorieGoal;}

}

