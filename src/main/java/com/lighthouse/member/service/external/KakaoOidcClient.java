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

    public static String getKakaoUserId(String accessToken) {
        String url = "https://kapi.kakao.com/v1/oidc/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
        );
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("sub");
        } else {
            throw new RuntimeException("카카오 사용자 ID(sub)를 가져오는 데 실패했습니다.");
        }
    }
}
