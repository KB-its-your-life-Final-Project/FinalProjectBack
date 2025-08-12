package com.lighthouse.gemini.controller;

import com.lighthouse.gemini.service.GeminiService;
import com.lighthouse.gemini.service.GeminiChatService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
@Slf4j
public class GeminiController {
    
    private final GeminiService geminiService;
    private final GeminiChatService geminiChatService;
    
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> sendPrompt(@RequestBody Map<String, String> request) {
        try {
            String prompt = request.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.WISHLIST_BAD_REQUEST));
            }
            
            String response = geminiService.sendPrompt(prompt);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @PostMapping("/chat/conversation")
    public ResponseEntity<ApiResponse<String>> sendChatPrompt(@RequestBody Map<String, Object> request) {
        try {
            String prompt = (String) request.get("prompt");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> conversationHistory = 
                (List<Map<String, String>>) request.get("conversationHistory");
            
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.WISHLIST_BAD_REQUEST));
            }
            
            String response = geminiService.sendChatPrompt(prompt, conversationHistory);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("Gemini 채팅 API 호출 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @PostMapping("/question")
    public ResponseEntity<ApiResponse<String>> askQuestion(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.WISHLIST_BAD_REQUEST));
            }
            
            String answer = geminiService.sendPrompt(question);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, answer));
            
        } catch (Exception e) {
            log.error("Gemini 질문-답변 API 호출 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        try {
            String response = geminiService.sendPrompt("안녕하세요. 간단한 테스트입니다.");
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, "AI 서비스가 정상 작동 중입니다."));
        } catch (Exception e) {
            log.error("Gemini 건강 체크 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testResponse() {
        try {
            String prompt = "안녕하세요";
            String response = geminiService.sendPrompt(prompt);
            
            log.info("=== AI 응답 테스트 ===");
            log.info("프롬프트: {}", prompt);
            log.info("AI 응답: {}", response);
            log.info("=====================");
            
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("Gemini 테스트 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    // 새로운 대화 히스토리 지원 엔드포인트들
    
    @PostMapping("/chat/session/start")
    public ResponseEntity<ApiResponse<String>> startChatSession(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = "session_" + System.currentTimeMillis();
            }
            
            String response = geminiChatService.startChat(sessionId);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("채팅 세션 시작 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @PostMapping("/chat/session/message")
    public ResponseEntity<ApiResponse<String>> sendChatMessage(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String message = request.get("message");
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.WISHLIST_BAD_REQUEST));
            }
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.WISHLIST_BAD_REQUEST));
            }
            
            String response = geminiChatService.sendMessage(sessionId, message);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("채팅 메시지 전송 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @GetMapping("/chat/session/history/{sessionId}")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getChatHistory(@PathVariable String sessionId) {
        try {
            List<Map<String, String>> history = geminiChatService.getConversationHistory(sessionId);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, history));
            
        } catch (Exception e) {
            log.error("채팅 히스토리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    @DeleteMapping("/chat/session/history/{sessionId}")
    public ResponseEntity<ApiResponse<String>> clearChatHistory(@PathVariable String sessionId) {
        try {
            geminiChatService.clearConversationHistory(sessionId);
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS, "대화 히스토리가 삭제되었습니다."));
            
        } catch (Exception e) {
            log.error("채팅 히스토리 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
}
