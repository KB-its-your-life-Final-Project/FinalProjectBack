package com.lighthouse.gemini.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String modelName;
    
    private final RestTemplate restTemplate;
    
    /**
     * Gemini API에 프롬프트를 전송하고 응답을 받습니다.
     */
    public String sendPrompt(String prompt) {
        // 입력 검증
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new RuntimeException("프롬프트가 비어있습니다.");
        }
        
        try {
            // 요청 본문 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ));
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // HTTP 엔티티 생성
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // API 호출
            String url = String.format("https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s", 
                modelName, apiKey);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                throw new RuntimeException("Gemini API 응답 오류: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            throw new RuntimeException("AI 서비스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 응답에서 텍스트를 추출합니다.
     */
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("응답에 candidates가 없습니다.");
            }
            
            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            Map<String, Object> part = parts.get(0);
            
            return (String) part.get("text");
            
        } catch (Exception e) {
            log.error("응답 파싱 중 오류 발생", e);
            throw new RuntimeException("AI 응답을 처리할 수 없습니다.", e);
        }
    }
    
    /**
     * 대화형 채팅을 위한 메서드 (히스토리 포함)
     */
    public String sendChatPrompt(String prompt, List<Map<String, String>> conversationHistory) {
        try {
            // 대화 히스토리와 새로운 프롬프트를 결합
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", conversationHistory);
            
            // 새로운 사용자 메시지 추가
            Map<String, Object> userMessage = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
            );
            ((List) requestBody.get("contents")).add(userMessage);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // HTTP 엔티티 생성
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // API 호출
            String url = String.format("https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s", 
                modelName, apiKey);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                throw new RuntimeException("Gemini API 응답 오류: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Gemini 채팅 API 호출 중 오류 발생", e);
            throw new RuntimeException("AI 채팅 서비스 오류가 발생했습니다.", e);
        }
    }
}
