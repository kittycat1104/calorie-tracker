package com.myapp.diet.Service;

import com.myapp.diet.entity.DailyNutrition;
import com.myapp.diet.entity.FoodRecord;
import com.myapp.diet.entity.User;
import com.myapp.diet.repository.DailyNutritionRepository;
import com.myapp.diet.repository.FoodRecordRepository;
import com.myapp.diet.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalorieService {

    private final ChatClient chatClient;
    private final DailyNutritionRepository dailyNutritionRepository;
    private final FoodRecordRepository foodRecordRepository;
    private final UserRepository userRepository;
//    public CalorieService(ChatClient chatClient) {
//        this.chatClient = chatClient;
//    }
    public CalorieService(ChatClient.Builder chatClientBuilder, DailyNutritionRepository dailyNutritionRepository, FoodRecordRepository foodRecordRepository, UserRepository userRepository) {
        this.chatClient = chatClientBuilder.build();
        this.dailyNutritionRepository = dailyNutritionRepository;
        this.foodRecordRepository = foodRecordRepository;
        this.userRepository = userRepository;
    }

    public String queryCalorie(String foodName) {
        // 建立聊天訊息，提示要查詢熱量
        //String prompt = "請告訴我 " + foodName + " 的熱量大約多少卡路里？";
        String systemPrompt = "你是一個營養師，請幫我估算食物的熱量，回答時盡量使用數字，和極少量的解說。";
        String userPrompt = "請告訴我 " + foodName + " 的熱量大約是多少卡路里？";

        Prompt prompt = new Prompt(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        );
        String chatResponse = chatClient.prompt(prompt)
                .call()
                .content();
       return chatResponse;

    }

    public List<DailyIntakeDetailDTO> getRecentIntakeDetails(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<DailyNutrition> nutritionList = dailyNutritionRepository.findByUserIdAndDateBetween(userId, start, today);

        List<DailyIntakeDetailDTO> result = new ArrayList<>();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (DailyNutrition dn : nutritionList) {
            List<FoodRecord> records = foodRecordRepository.findByUserAndDate(user, dn.getDate());

            List<String> foodNames = records.stream()
                    .map(record -> record.getFood().getName()) // 從關聯的 Food 取得名稱
                    .collect(Collectors.toList());

            result.add(new DailyIntakeDetailDTO(
                   dn.getDate(),
                   dn.getTotalCalories(),
                   foodNames));
        }
        return result;
    }

    public String generateDetailedDietAdvice(User user, List<DailyIntakeDetailDTO> intakeDetails) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位營養師，請根據以下使用者資料與最近7天的飲食內容，提供健康飲食建議。\n\n");

        prompt.append("使用者資訊：\n")
                .append("性別：").append(user.getGender()).append("\n")
                .append("身高：").append(user.getHeight()).append(" cm\n")
                .append("體重：").append(user.getWeight()).append(" kg\n")
                .append("TDEE：").append(user.getTdee()).append(" kcal\n")
                .append("每周想減公斤數:").append(user.getWeeklyWeightGoal()).append("\n\n");

        prompt.append("最近 7 天的飲食紀錄：\n");

        for (DailyIntakeDetailDTO day : intakeDetails) {
            prompt.append(day.getDate()).append("：攝取 ")
                    .append(day.getTotalCalories()).append(" kcal，食物包含：")
                    .append(String.join("、", day.getFoodNames())).append("\n");
        }

        prompt.append("\n請提供：\n")
                .append("1. 是否有過量攝取或不健康習慣\n")
                .append("2. 哪些食物攝取太多/太少\n")
                .append("3. 建議的調整方向與替代食物\n")
                .append("4. 一天建議菜單\n");

        Prompt gptPrompt = new Prompt(
                new SystemMessage("你是一位健康飲食顧問，請用條列式提供具體建議。請用適合網頁顯示的 Markdown 格式輸出健康建議。" +
                        "格式要求如下：使用 ### 分段標題，不使用粗體段落標題（例如「過量攝取：」改為標題或正常文字）。列點說明請使用 -，不要巢狀太多層。"),
                new UserMessage(prompt.toString())
        );

        return chatClient.prompt(gptPrompt).call().content();
    }



    @Data
    @AllArgsConstructor
    public class DailyIntakeDetailDTO {
        private LocalDate date;
        private Integer totalCalories;
        private List<String> foodNames;
    }

}

