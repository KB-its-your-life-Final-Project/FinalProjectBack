package com.lighthouse.common.external.naver;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class NaverSearchClient {
    @Value("${NAVER_SEARCH_CLIENT_ID}")
    private String clientId;
    @Value("${NAVER_SEARCH_CLIENT_SECRET}")
    private String clientSecret;


    // HTTP 요청 보냄
    private final RestTemplate restTemplate = new RestTemplate();

    public int getCategoryCodeByAddress(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("검색어가 비어 있습니다!");
            }

            String url = "https://openapi.naver.com/v1/search/local.json?query=" + query;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
            }

            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("items")) {
                throw new RuntimeException("응답이 유효하지 않음");
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            if (items.isEmpty()) {
                return 5;
            }
            Map<String, Object> firstItem = items.get(0);
            String category = (String) firstItem.get("category");
            category = category.toLowerCase(); // 소문자 처리

            if (category.contains("아파트")) {
                return 1;
            } else if (category.contains("오피스텔")) {
                return 2;
            } else if (category.contains("연립") || category.contains("다세대")) {
                return 3;
            } else if (category.contains("단독") || category.contains("다가구")) {
                return 4;
            } else {
                return 5;
            }

        } catch (Exception e) {
            log.warn("카테고리 조회 실패: {}", e.getMessage());
            throw new RuntimeException("카테고리 조회 실패: " + e.getMessage(), e);
        }
    }
}