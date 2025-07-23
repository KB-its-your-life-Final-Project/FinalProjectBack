package com.lighthouse.member.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
public class GoogleUserClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public static Map getGoogleUserInfo(String googleAccessToken) {
        log.info("GoogleUserClient.getGoogleUserInfo() 실행 ======");
        log.info("받은 googleAccessToken: {}", googleAccessToken);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(googleAccessToken);

            // 쿼리 파라미터 (필요 시 다른 값 추가)
            UriComponentsBuilder googleUriBuilder = UriComponentsBuilder.fromHttpUrl(GOOGLE_USER_INFO_URL);

            // 사용자 정보 요청 (GET)
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    googleUriBuilder.toUriString(),
                    HttpMethod.GET,
                    request,
                    Map.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfoMap = response.getBody();
                String googleId = (String) userInfoMap.get("id");
                String email = (String) userInfoMap.get("email");
                String name = (String) userInfoMap.get("name");
                log.info("구글 사용자정보에서 googleId 추출: {}", googleId);
                log.info("구글 사용자정보에서 email 추출: {}", email);
                log.info("구글 사용자정보에서 name 추출: {}", name);
                return userInfoMap;
            } else {
                throw new RuntimeException("구글 사용자 정보 요청 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 중 예외 발생", e);
            throw new RuntimeException("구글 사용자 정보 조회 실패", e);
        }
    }
}