package com.lighthouse.common.geocoding.service;

import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.lighthouse.common.external.naver.NaverMapClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodingService {
    private final NaverMapClient naverMapClient;
    private final RestTemplate restTemplate;

    //주소에서 좌표로 변환
    public Map<String, Double> getCoordinateFromAddress(String address) {
        try {
            double lat = Double.parseDouble((String) naverMapClient.getInfoOfAddress(address).get("y"));
            double lng = Double.parseDouble((String) naverMapClient.getInfoOfAddress(address).get("x"));

            return Map.of("lat", lat, "lng", lng);
        }
        catch(Exception e) {
            throw e;
        }
    }

    // 위도/경도를 도로명 주소로 변환 (역지오코딩)
    public String getRoadAddressFromCoordinates(double lat, double lng) {
        try {
            // 네이버 지도 API의 좌표->주소 변환 API 호출
            String url = String.format("https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=%f,%f&orders=roadaddr&output=json", lng, lat);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", naverMapClient.getClientId());
            headers.set("X-NCP-APIGW-API-KEY", naverMapClient.getClientSecret());
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

            Map<String, Object> region = (Map<String, Object>) result.get("region");
            Map<String, Object> land = (Map<String, Object>) result.get("land");

            // 도로명 주소 구성
            StringBuilder roadAddress = new StringBuilder();
            
            // 시도
            if (region.containsKey("area1") && region.get("area1") != null) {
                Map<String, Object> area1 = (Map<String, Object>) region.get("area1");
                if (area1.containsKey("name")) {
                    roadAddress.append(area1.get("name"));
                }
            }
            
            // 시군구
            if (region.containsKey("area2") && region.get("area2") != null) {
                Map<String, Object> area2 = (Map<String, Object>) region.get("area2");
                if (area2.containsKey("name")) {
                    roadAddress.append(" ").append(area2.get("name"));
                }
            }
            
            // 도로명
            if (land != null && land.containsKey("name")) {
                roadAddress.append(" ").append(land.get("name"));
            }
            
            // 건물번호
            if (land != null && land.containsKey("number1")) {
                roadAddress.append(" ").append(land.get("number1"));
            }
            
            // 상세번호
            if (land != null && land.containsKey("number2")) {
                roadAddress.append("-").append(land.get("number2"));
            }

            String resultAddress = roadAddress.toString().trim();
            log.info("역지오코딩 결과: {} -> {}", String.format("lat: %f, lng: %f", lat, lng), resultAddress);
            
            return resultAddress.isEmpty() ? null : resultAddress;

        } catch (Exception e) {
            log.warn("역지오코딩 실패 - lat: {}, lng: {}: {}", lat, lng, e.getMessage());
            return null;
        }
    }
}