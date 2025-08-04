package com.lighthouse.localinfo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.localinfo.dto.ReverseGeocodeResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReverseGeocodeService {

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_API_KEY}")
    private String clientSecret;

    private final String REVERSE_GEOCODE_ENDPOINT = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReverseGeocodeResponseDTO reverseGeocode(double longitude, double latitude) {
        try {
            // UriComponentsBuilder를 사용한 URL 구성
            URI uri = UriComponentsBuilder
                    .fromUriString(REVERSE_GEOCODE_ENDPOINT) // 변경된 상수 사용
                    .queryParam("coords", longitude + "," + latitude) // 경도,위도 순서 중요!
                    .queryParam("output", "json")
                    // orders 파라미터에 admcode, addr, roadaddr 추가합니다.
                    .queryParam("orders", "legalcode,admcode,addr,roadaddr")
                    .build(true)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("네이버 역지오코딩 API 요청 URL: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String jsonResponse = response.getBody();

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode status = root.path("status");
            if (status.path("code").asInt() != 0) {
                log.error("❌ 네이버 역지오코딩 API 응답 오류 - 코드: {}, 메시지: {}",
                        status.path("code").asText(), status.path("message").asText());
                return null;
            }

            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0); // 첫 번째 결과
                JsonNode region = firstResult.path("region");

                JsonNode area1 = region.path("area1"); // 시도
                JsonNode area2 = region.path("area2"); // 시군구
                JsonNode area3 = region.path("area3"); // 읍면동 (법정동)
                JsonNode area4 = region.path("area4"); // 리 (없을 수 있음)

                String legalCode = null;
                // 네이버 API 응답 구조에 따라 legalCode는 region.area3.coords.center.code에 있습니다.
                // results[0].code는 전체 주소의 코드를 의미할 수 있습니다.
                if (area3.has("coords") && area3.path("coords").has("center") && area3.path("coords").path("center").path("crs").has("code")) {
                    legalCode = area3.path("coords").path("center").path("crs").path("code").asText();
                } else {
                    log.warn("법정동 코드(legalCode)를 파싱할 수 없습니다. 응답 구조 확인 필요.");
                }

                // 응답에서 주소 전체를 가져올 수도 있습니다.
                String fullAddressName = "";
                JsonNode roadAddress = firstResult.path("roadAddress");
                if (roadAddress.has("fullAddress")) {
                    fullAddressName = roadAddress.path("fullAddress").asText();
                } else { // 도로명 없으면 지번으로 조합
                    fullAddressName = area1.path("name").asText() + " " + area2.path("name").asText() + " " + area3.path("name").asText() + (area4.has("name") && !area4.path("name").asText().isEmpty() ? " " + area4.path("name").asText() : "");
                }

                ReverseGeocodeResponseDTO dto = new ReverseGeocodeResponseDTO();
                dto.setLatitude(latitude);
                dto.setLongitude(longitude);
                dto.setAddressName(fullAddressName);
                dto.setLegalDongName(area3.path("name").asText()); // 읍면동 이름 (법정동명)
                dto.setLegalDongCode(legalCode);

                log.info("역지오코딩 성공: {}", dto);
                return dto;
            } else {
                log.warn("네이버 역지오코딩 API 응답에 유효한 결과가 없습니다.");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ 네이버 역지오코딩 API 호출/파싱 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}