package com.lighthouse.member.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KakaoOidcClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String KAKAO_OIDC_USERINFO_URL = "https://kapi.kakao.com/v1/oidc/userinfo";

    public static String getKakaoUserId(String kakaoAccessToken) {
        log.info("KakaoOidcClient.getKakaoUserId() 실행 ======");
        log.info("받은 kakaoAccessToken: {}", kakaoAccessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                KAKAO_OIDC_USERINFO_URL, HttpMethod.GET, entity, Map.class
        );
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String kakaoUserId = (String) response.getBody().get("sub");
            log.info("카카오에서 발급한 회원ID: {}", kakaoUserId);
            return kakaoUserId;
        } else {
            throw new RuntimeException("카카오 회원ID(sub) 가져오기 실패");
        }
    }
}
