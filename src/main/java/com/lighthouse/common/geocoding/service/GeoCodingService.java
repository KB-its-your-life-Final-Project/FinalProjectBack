package com.lighthouse.common.geocoding.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import com.lighthouse.common.external.naver.NaverMapClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodingService {
    private final NaverMapClient naverMapClient;

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
}