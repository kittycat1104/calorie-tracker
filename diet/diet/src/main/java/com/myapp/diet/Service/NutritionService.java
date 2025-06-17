package com.myapp.diet.Service;

import com.myapp.diet.controller.FoodController;
import com.myapp.diet.entity.DailyNutrition;
import com.myapp.diet.entity.FoodRecord;
import com.myapp.diet.entity.User;
import com.myapp.diet.repository.DailyNutritionRepository;
import com.myapp.diet.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NutritionService {

    private final FoodRecordRepository foodRecordRepository;
    private final DailyNutritionRepository dailyNutritionRepository;

    public NutritionService(FoodRecordRepository foodRecordRepository,
                            DailyNutritionRepository dailyNutritionRepository) {
        this.foodRecordRepository = foodRecordRepository;
        this.dailyNutritionRepository = dailyNutritionRepository;
    }

    public void updateDailyNutrition(User user, LocalDate date) {
        List<FoodRecord> records = foodRecordRepository.findByUserAndDate(user, date);
        int totalCalories = records.stream().mapToInt(FoodRecord::getCalories).sum();

        DailyNutrition dailyNutrition = dailyNutritionRepository
                .findByUserAndDate(user, date)
                .orElseGet(() -> {
                    DailyNutrition d = new DailyNutrition();
                    d.setUser(user);
                    d.setDate(date);
                    return d;
                });

        dailyNutrition.setTotalCalories(totalCalories);

        Integer goal = user.getDailyCalorieGoal();
        if (goal != null) {
            dailyNutrition.setGoalCalories(goal);
            dailyNutrition.setRemainingCalories(goal - totalCalories);
        } else {
            dailyNutrition.setGoalCalories(null);
            dailyNutrition.setRemainingCalories(null);
        }

        dailyNutritionRepository.save(dailyNutrition);
        System.out.println("user.hi = " + user.getDailyNutritions());
    }

    public List<FoodController.DailyCaloriesDTO> getWeeklyCalories(Long userId,LocalDate targetDate) {
//        LocalDate today = LocalDate.now();
        // 當週週一（或你要週日開頭也可調整）
        LocalDate startOfWeek = targetDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<DailyNutrition> records = dailyNutritionRepository.findByUserIdAndDateBetween(userId, startOfWeek, endOfWeek);

        // 建立日期對應的 map
        Map<LocalDate, Integer> caloriesMap = records.stream()
                .collect(Collectors.toMap(
                        DailyNutrition::getDate,
                        d -> Optional.ofNullable(d.getTotalCalories()).orElse(0)
                ));

        // 回傳完整週期的資料（即使某天沒資料也補 0）
        List<FoodController.DailyCaloriesDTO> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            Integer calories = caloriesMap.getOrDefault(date, 0);
            result.add(new FoodController.DailyCaloriesDTO(date, calories));
        }

        return result;
    }
}

