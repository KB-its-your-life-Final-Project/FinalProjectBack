package com.lighthouse.gemini.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatService {
    
    private final GeminiService geminiService;
    
    // 세션별 대화 히스토리 저장 (실제 프로덕션에서는 Redis나 DB 사용 권장)
    private final Map<String, List<Map<String, String>>> conversationHistory = new ConcurrentHashMap<>();
    
    /**
     * 새로운 채팅 세션을 시작합니다.
     */
    public String startChat(String sessionId) {
        conversationHistory.put(sessionId, new ArrayList<>());
        return "새로운 대화가 시작되었습니다. 무엇을 도와드릴까요?";
    }
    
    /**
     * 대화 히스토리를 포함하여 메시지를 전송합니다.
     */
    public String sendMessage(String sessionId, String message) {
        try {
            // 세션이 없으면 새로 생성
            if (!conversationHistory.containsKey(sessionId)) {
                startChat(sessionId);
            }
            
            List<Map<String, String>> history = conversationHistory.get(sessionId);
            
            // 사용자 메시지를 히스토리에 추가
            history.add(Map.of("role", "user", "text", message));
            
            // AI 응답 생성 (히스토리 포함)
            String response = geminiService.sendChatPrompt(message, history);
            
            // AI 응답을 히스토리에 추가
            history.add(Map.of("role", "assistant", "text", response));
            
            // 히스토리가 너무 길어지면 오래된 메시지 제거 (최대 20개 메시지 유지)
            if (history.size() > 20) {
                history.subList(0, history.size() - 20).clear();
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("채팅 메시지 처리 중 오류 발생", e);
            return "죄송합니다. 메시지 처리 중 오류가 발생했습니다.";
        }
    }
    
    /**
     * 대화 히스토리를 조회합니다.
     */
    public List<Map<String, String>> getConversationHistory(String sessionId) {
        return conversationHistory.getOrDefault(sessionId, new ArrayList<>());
    }
    
    /**
     * 대화 히스토리를 삭제합니다.
     */
    public void clearConversationHistory(String sessionId) {
        conversationHistory.remove(sessionId);
    }
    
    /**
     * 모든 세션의 대화 히스토리를 조회합니다. (관리자용)
     */
    public Map<String, List<Map<String, String>>> getAllConversationHistory() {
        return new HashMap<>(conversationHistory);
    }
}
