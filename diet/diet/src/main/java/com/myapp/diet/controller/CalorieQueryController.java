//package com.myapp.diet.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/calories")
//public class CalorieQueryController {
//
//    @Autowired
//    private ChatGptService chatGptService;
//
//    @PostMapping("/estimate")
//    public ResponseEntity<?> estimateCalories(@RequestBody Map<String, String> request) {
//        String foodName = request.get("food");
//        if (foodName == null || foodName.isBlank()) {
//            return ResponseEntity.badRequest().body("請提供食物名稱");
//        }
//
//        String prompt = """
//            你是一位營養師，請告訴我以下食物的大致熱量（單位：大卡）：
//            食物：%s
//            只需要回傳純數字卡路里與食物名稱，簡短明確，例如：「雞胸肉 100g 約為 165 卡」。
//            """.formatted(foodName);
//
//        String reply = chatGptService.ask(prompt);
//
//        return ResponseEntity.ok(Map.of("food", foodName, "response", reply));
//    }
//}
//
