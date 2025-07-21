package com.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
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
        //주소를 URL-safe 형태로 인코딩 ('서울시 강남구' -> 서울시%강남구)
        String encoded = UriUtils.encode(address, StandardCharsets.UTF_8);
        //네이버 지도 API의 주소-> 좌표 변환 API 호출
        String url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + encoded;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers); //요청 본문은 비우고 헤더만 담은 요청 객체

        //HTTP GET 요청 실행
        //반환된 JSON 응답은 Map<String, Object> 형태로 파싱해서 받기
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
        );

        Map first = (Map)((List)((Map) response.getBody()).get("addresses")).get(0);
        double lat = Double.parseDouble((String) first.get("y"));
        double lng = Double.parseDouble((String) first.get("x"));

        return Map.of("lat", lat, "lng", lng);
    }
}
