package com.batch.toCoord.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service //AddressToCoordinate.java에서 주입받아 사용하기
public class AddressGeocodeService {

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;
    @Value("${NAVER_API_KEY}")
    private String clientSecret;

    // HTTP 요청 보냄
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Double> getCoordinates(String address) {
        try{
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
//            System.out.println("응답 상태 코드: " + response.getStatusCode());
//            System.out.println("응답 바디: " + response.getBody());
//            System.out.println("📨 요청 주소 원문: [" + address + "]");
//            System.out.println("📡 호출 URL: " + url);

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

            Map<String, Object> first = addresses.get(0);

            double lat = Double.parseDouble((String) first.get("y"));
            double lng = Double.parseDouble((String) first.get("x"));

            return Map.of("lat", lat, "lng", lng);
        }catch (Exception e) {
            throw new RuntimeException("주소 변환 실패: " + e.getMessage(), e);
        }

    }
}
