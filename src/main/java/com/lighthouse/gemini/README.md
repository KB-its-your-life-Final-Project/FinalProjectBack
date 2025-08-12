# Gemini AI 도메인

Google AI Studio (Gemini API)를 사용하여 AI 챗봇 기능을 제공하는 도메인입니다.

## 📁 구조

```
gemini/
├── config/
│   └── GeminiConfig.java          # 설정 클래스
├── controller/
│   └── GeminiController.java      # REST API 컨트롤러
├── dto/
│   ├── GeminiRequestDTO.java      # 요청 DTO
│   └── GeminiResponseDTO.java     # 응답 DTO
├── service/
│   └── GeminiService.java         # 비즈니스 로직
└── README.md                      # 이 파일
```

## 🚀 사용법

### 1. 환경 변수 설정

`.env` 파일에 API 키를 추가하세요:

```env
GEMINI_API_KEY=your-gemini-api-key-here
```

### 2. API 엔드포인트

#### 단일 프롬프트 전송

```http
POST /api/gemini/chat
Content-Type: application/json

{
  "prompt": "안녕하세요, 오늘 날씨는 어때요?"
}
```

#### 대화형 채팅 (히스토리 포함)

```http
POST /api/gemini/chat/conversation
Content-Type: application/json

{
  "prompt": "이전 대화를 기억하고 있어요?",
  "conversationHistory": [
    {
      "role": "user",
      "parts": [{"text": "안녕하세요"}]
    },
    {
      "role": "model",
      "parts": [{"text": "안녕하세요! 무엇을 도와드릴까요?"}]
    }
  ]
}
```

#### 간단한 질문-답변

```http
POST /api/gemini/question
Content-Type: application/json

{
  "question": "Java에서 Spring Boot란 무엇인가요?"
}
```

#### 건강 체크

```http
GET /api/gemini/health
```

### 3. 응답 형식

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "성공",
  "data": "AI 응답 내용"
}
```

## 🔧 설정

### application-local.properties

```properties
# Gemini AI API 설정
gemini.api.key=${GEMINI_API_KEY}
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

## 🧪 테스트

```bash
# 테스트 실행
./gradlew test --tests "com.lighthouse.gemini.service.GeminiServiceTest"
```

## 📝 예시

### JavaScript (Frontend)

```javascript
// 단일 프롬프트 전송
const response = await fetch("/api/gemini/chat", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    prompt: "안녕하세요!",
  }),
});

const result = await response.json();
console.log(result.data); // AI 응답
```

### cURL

```bash
# 단일 프롬프트
curl -X POST http://localhost:8080/api/gemini/chat \
  -H "Content-Type: application/json" \
  -d '{"prompt": "안녕하세요!"}'

# 건강 체크
curl http://localhost:8080/api/gemini/health
```

## ⚠️ 주의사항

1. **API 키 보안**: `.env` 파일을 Git에 커밋하지 마세요
2. **요청 제한**: Gemini API에는 요청 제한이 있을 수 있습니다
3. **에러 처리**: 네트워크 오류나 API 오류에 대한 적절한 처리가 필요합니다

## 🔗 참고 자료

- [Google AI Studio](https://aistudio.google.com/)
- [Gemini API 문서](https://ai.google.dev/docs)
- [Gemini API 가이드](https://ai.google.dev/tutorials)
