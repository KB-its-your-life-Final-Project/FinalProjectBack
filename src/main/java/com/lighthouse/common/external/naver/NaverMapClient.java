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
public class NaverMapClient {
    @Value("${NAVER_CLIENT_ID}")
    private String clientId;
    @Value("${NAVER_API_KEY}")
    private String clientSecret;


    // HTTP 요청 보냄
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getInfoOfAddress(String address) {

        try {
            //네이버 지도 API의 주소-> 좌표 변환 API 호출
            String url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers); //요청 본문은 비우고 헤더만 담은 요청 객체

            if (address == null || address.trim().isEmpty()) {
                throw new IllegalArgumentException("주소가 비어 있습니다!");
            }

            //HTTP GET 요청 실행
            //반환된 JSON 응답은 Map<String, Object> 형태로 파싱해서 받기
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body == null) {
                throw new RuntimeException("응답이 비어있음");
            }
            if( !body.containsKey("addresses")){
                throw new RuntimeException("주소가 없음");
            }

            List<Map<String, Object>> addresses = (List<Map<String, Object>>) body.get("addresses");

            if (addresses.isEmpty()) {
                throw new RuntimeException("검색된 주소가 없습니다: " + address);
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
            }

            Map<String, Object> data = addresses.get(0);

            return data;
        } 
        catch (Exception e) {
            log.warn("주소 변환 실패: {}", e.getMessage());
            throw new RuntimeException("주소 변환 실패: " + e.getMessage(), e);
        }

    }
    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Map<String, Object> getAddressFromCoordinates(double lat, double lng, String orders) {
        try {
            // 네이버 지도 API의 좌표->주소 변환 API 호출
            String url = String.format("https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=%f,%f&orders=%s&output=json", lng, lat, orders);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // HTTP GET 요청 실행
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.warn("역지오코딩 응답이 비어있음");
                return null;
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("역지오코딩 API 호출 실패: {}", response.getStatusCode());
                return null;
            }

            if (!body.containsKey("results")) {
                log.warn("역지오코딩 결과가 없음");
                return null;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
            if (results.isEmpty()) {
                log.warn("역지오코딩 결과가 비어있음");
                return null;
            }

            Map<String, Object> result = results.get(0);
            if (!result.containsKey("region")) {
                log.warn("역지오코딩 지역 정보가 없음");
                return null;
            }

            return result;

        } catch (Exception e) {
            log.warn("역지오코딩 실패 - lat: {}, lng: {}: {}", lat, lng, e.getMessage());
            return null;
        }
    }
}