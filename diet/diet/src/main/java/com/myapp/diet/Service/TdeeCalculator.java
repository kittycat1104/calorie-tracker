package com.myapp.diet.Service;

public class TdeeCalculator {

    // 計算基礎代謝率 (BMR) 用的是 Mifflin-St Jeor 公式
    public static double calculateBMR(String gender, double weight, double height, int age) {
        if ("male".equalsIgnoreCase(gender)) {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        }
    }

    // 計算 TDEE
    public static int calculateTDEE(String gender, double weight, double height, int age, double activityLevel) {
        double bmr = calculateBMR(gender, weight, height, age);
        return (int) Math.round(bmr * activityLevel);
    }

    // 7700 大卡 ≈ 1 公斤脂肪
    public static int convertWeeklyGoalToDailyDeficit(double weeklyKgGoal) {
        return (int) Math.round(-weeklyKgGoal * 7700 / 7);
    }

    public static int calculateDailyCalorieGoal(int tdee, int dailyDeficit) {
        return tdee + dailyDeficit;
    }
}

