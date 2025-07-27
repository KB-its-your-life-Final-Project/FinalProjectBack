package com.lighthouse.member.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class KakaoUserClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public static String getKakaoNickname(String kakaoAccessToken) {
        log.info("KakaoUserClient.getKakaoNickname() 실행 ======");
        log.info("받은 kakaoAccessToken: {}", kakaoAccessToken);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + kakaoAccessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 쿼리 파라미터 (필요 시 secure_resource, property_keys 추가)
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAKAO_USER_INFO_URL);

            // 사용자 정보 요청 (GET)
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);

                // 프로필 정보에서 닉네임 추출
                JsonNode profileNode = root.path("kakao_account").path("profile");
                String nickname = profileNode.path("nickname").asText();
                log.info("카카오 프로필에서 닉네임 추출: {}", nickname);
                return nickname;
            } else {
                throw new RuntimeException("카카오 사용자 정보 요청 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("카카오 닉네임 조회 중 예외 발생", e);
            throw new RuntimeException("카카오 닉네임 조회 실패", e);
        }
    }
}