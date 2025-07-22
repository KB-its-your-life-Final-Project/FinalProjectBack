package com.lighthouse.member.service.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTokenClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    @Value("${KAKAO_REST_API_KEY}")
    private String clientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;

    public String getKakaoAccessToken(String kakaoCode) {
        log.info("KakaoTokenClient.getAccessToken() 실행 ======");
        log.info("받은 kakaoCode: {}", kakaoCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", kakaoCode);
        log.info("KakaoTokenClient의  header: {}", headers);
        log.info("KakaoTokenClient의  body: {}", body);

        // Access Token 요청 (POST)
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_TOKEN_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String kakaoAccessToken = (String) response.getBody().get("access_token");
            log.info("KakaoTokenClient: kakaoAccessToken: {}", kakaoAccessToken);
            return kakaoAccessToken;
        } else {
            throw new RuntimeException("카카오 Access Token 가져오기 실패");
        }
    }
}
