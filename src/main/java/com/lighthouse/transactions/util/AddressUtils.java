package com.lighthouse.transactions.util;

import com.lighthouse.toCoord.service.AddressGeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AddressUtils {
    private final AddressGeocodeService geocodeService;

    @Autowired
    public AddressUtils(AddressGeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }

    /**
     * 지번주소 생성 (읍면동 + 지번)
     */
    public static String getJibunAddr(String umdNm, String jibun) {
        return String.format("%s %s",
                umdNm != null ? umdNm.trim() : "",
                jibun != null ? jibun.trim() : ""
        ).trim();
    }

    /**
     * 위도, 경도 맵 생성
     */
    public Map<String, Double> getLatLong(String jibunAddr) {
        if (jibunAddr == null || jibunAddr.isBlank()) {
            return new HashMap<>() {{
                put("latitude", 0.0);
                put("longitude", 0.0);
            }};
        }
        return geocodeService.getCoordinates(jibunAddr);
    }

    /**
     * 위도(latitude) 반환
     */
    public Double getLatitude(String jibunAddr) {
        Map<String, Double> coordinates = getLatLong(jibunAddr);
        return coordinates.getOrDefault("latitude", 0.0);
    }

    /**
     * 경도(longitude) 반환
     */
    public Double getLongitude(String jibunAddr) {
        Map<String, Double> coordinates = getLatLong(jibunAddr);
        return coordinates.getOrDefault("longitude", 0.0);
    }

}
