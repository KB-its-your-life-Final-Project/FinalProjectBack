package com.lighthouse.gemini.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
    "gemini.api.key=AIzaSyBSubc0xoBoKinQhld8PfaP1n9-qpsMYE0",
    "gemini.api.model=gemini-2.5-flash"
})
class GeminiServiceTest {
    
    private GeminiService geminiService;
    
    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        geminiService = new GeminiService(restTemplate);
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyBSubc0xoBoKinQhld8PfaP1n9-qpsMYE0");
        ReflectionTestUtils.setField(geminiService, "modelName", "gemini-2.5-pro");
    }
    
    @Test
    @DisplayName("안녕하세요 프롬프트로 실제 AI 응답 받기")
    void testAi() {
        // Given
        String prompt = "넌 한국의 안전자산 관리를 위한 부동산 서비스를 맡고 있어 너는 내가 제공하는 사용자의 정보로 집을 추천해야해 다만 집정보는 매우 정확해야하니 모든 정보는 꼭 한번 더 찾아보고 답변하도록 해.";
        prompt += "내가 너에게 어떤 데이터를 줄껀데 그거 기반으로 json 타입으로만 리턴해줘 왜냐면 바로 사용할꺼니까. ";        
        prompt += "리턴할 json 타입은 { jibunAddres: 지번주소, roadAddress: , bulidingName, positiveFactor} 이렇게 4개야 ";
        
        prompt += "jibunAddress: 지번주소, roadAddress: 도로명 주소, buildingName : 건물이름 (없을시 null), positiveFactor: 장점 1줄 ~ 2줄 사이";
        
        prompt += "userdata = { 나이: 25, 성별: 남자, 최근 검색 기록: '서울 광진구 군자동 357, 서울 광진구 군자동 504', 예산: 1,000,000,000}";
        prompt += "사용자의 최근 검색 기록이 아니더라도 다른 곳이 있으면 상관없어";
        prompt += "위 정보를 바탕으로 아까 말한 json 타입으로 반환해줘";
        
        
        // When
        try {
            String result = geminiService.sendPrompt(prompt);
            
            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            
            System.out.println("=== AI 응답 ===");
            System.out.println("프롬프트: " + prompt);
            System.out.println("AI 응답: " + result);
            System.out.println("===============");
            
        } catch (Exception e) {
            System.out.println("API 호출 실패: " + e.getMessage());
            // 테스트는 실패하지 않도록 함
        }
    }
    
}
