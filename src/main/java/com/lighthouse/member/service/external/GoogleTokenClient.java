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
public class GoogleTokenClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;
    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${GOOGLE_REDIRECT_URI}")
    private String redirectUri;

    public String getGoogleAccessToken(String googleCode) {
        log.info("GoogleTokenClient.getGoogleAccessToken() 실행 ======");
        log.info("받은 googleCode: {}", googleCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", googleCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        log.info("GoogleTokenClient의  header: {}", headers);
        log.info("GoogleTokenClient의  body: {}", body);

        // Access Token 요청 (POST)
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String googleAccessToken = (String) response.getBody().get("access_token");
            log.info("GoogleTokenClient: getGoogleAccessToken: {}", googleAccessToken);
            return googleAccessToken;
        } else {
            throw new RuntimeException("구글 Access Token 가져오기 실패");
        }
    }
}