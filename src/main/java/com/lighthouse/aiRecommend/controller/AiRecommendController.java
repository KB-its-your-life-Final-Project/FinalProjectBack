package com.lighthouse.aiRecommend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lighthouse.aiRecommend.service.AiRecommendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ai-recommend")
@RequiredArgsConstructor
@Slf4j
public class AiRecommendController {

    private final AiRecommendService aiRecommendService;

    @GetMapping("/{memberId}")
    public ResponseEntity<String> getAiRecommend(@PathVariable Long memberId) {
        try {
            log.info("AI 추천 요청 - memberId: {}", memberId);
            
            String aiResponse = aiRecommendService.getAiRecommend(memberId);
            
            return ResponseEntity.ok(aiResponse);
            
        } catch (Exception e) {
            log.error("AI 추천 처리 중 에러 발생 - memberId: {}", memberId, e);
            return ResponseEntity.internalServerError()
                .body("AI 추천 처리 중 에러 발생: " + e.getMessage());
        }
    }
}
