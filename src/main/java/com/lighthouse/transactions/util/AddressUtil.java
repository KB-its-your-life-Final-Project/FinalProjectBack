package com.lighthouse.transactions.util;

import com.lighthouse.common.geocoding.service.GeoCodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressUtil {
    private final GeoCodingService geoCodingService;

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
    public Map<String, Double> getLatLng(String jibunAddr) {
        if (jibunAddr == null || jibunAddr.isBlank()) {
            return new HashMap<>() {{
                put("lat", 0.0);
                put("lng", 0.0);
            }};
        }
        return geoCodingService.getCoordinateFromAddress(jibunAddr);
    }
}
