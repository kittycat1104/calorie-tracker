package com.myapp.diet.controller;


import com.myapp.diet.Service.CalorieService;
import com.myapp.diet.Service.NutritionService;
import com.myapp.diet.entity.User;
import com.myapp.diet.repository.UserRepository;
import com.myapp.diet.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    @Autowired
    private final CalorieService calorieService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final NutritionService nutritionService;

    public NutritionController(CalorieService calorieService, UserRepository userRepository, NutritionService nutritionService) {
        this.calorieService = calorieService;
        this.userRepository = userRepository;
        this.nutritionService = nutritionService;
    }

    @GetMapping("/check")
    public Map<String, String> getCalorieOpinion(@RequestParam String food) {
        String opinion = calorieService.queryCalorie(food);

        // 回傳 {"意見": "大概100卡路里"}
        Map<String, String> response = new HashMap<>();
        response.put("意見", opinion);
        return response;
    }

    @GetMapping("/diet-detailed-advice")
    public ResponseEntity<String> getDetailedAdvice(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        User user = userRepository.findById(userId).orElseThrow();

        List<CalorieService.DailyIntakeDetailDTO> intakeDetails = calorieService.getRecentIntakeDetails(userId);
        String advice = calorieService.generateDetailedDietAdvice(user, intakeDetails);
        return ResponseEntity.ok(advice);
    }
}