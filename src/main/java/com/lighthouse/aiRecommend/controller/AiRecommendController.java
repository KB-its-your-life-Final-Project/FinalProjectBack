package com.lighthouse.aiRecommend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lighthouse.aiRecommend.service.AiRecommendService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ai-recommend")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "AI Recommend", description = "회원별 맞춤 추천 데이터를 생성 및 조회")
public class AiRecommendController {

    private final AiRecommendService aiRecommendService;

    @GetMapping("/{memberId}")
    @ApiOperation(
            value = "회원 맞춤 AI 추천 조회",
            notes = "회원 ID를 기반으로 AI가 분석한 맞춤 추천 결과를 알려줍니다.")
    public ResponseEntity<ApiResponse<String>> getAiRecommend(@PathVariable Long memberId) {
        try {
            // log.info("AI 추천 요청 - memberId: {}", memberId);
            
            String aiResponse = aiRecommendService.getAiRecommend(memberId);
            
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.AI_RECOMMEND_SUCCESS, aiResponse));
            
        } catch (Exception e) {
            log.error("AI 추천 처리 중 에러 발생 - memberId: {}", memberId, e);
            return ResponseEntity.ok()
            .body(ApiResponse.error(ErrorCode.AI_RECOMMEND_FAIL));
        }
    }
}
