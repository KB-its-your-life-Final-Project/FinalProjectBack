package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.WeatherDTO;
import com.lighthouse.localinfo.mapper.WeatherMapper;
import com.lighthouse.localinfo.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final WeatherMapper weatherMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${data.go.kr.api.key}")
    private String apiKey;
    
    public WeatherDTO selectByRegion(String region) {
        // DB에서 격자 좌표 조회
        WeatherVO weatherVO = weatherMapper.selectByRegion(region);
        
        // 현재 시간 계산
        LocalDateTime now = LocalDateTime.now();
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getBaseTime(now);
        
        // API 호출
        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", "1")
                .queryParam("numOfRows", "1000")
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", String.valueOf(weatherVO.getGridX()))
                .queryParam("ny", String.valueOf(weatherVO.getGridY()))
                .build()
                .toUriString();
        
        String jsonResponse = restTemplate.getForObject(url, String.class);
        return parseWeatherResponse(jsonResponse, region);
    }
    
    private WeatherDTO parseWeatherResponse(String jsonResponse, String region) {
        WeatherDTO.WeatherDTOBuilder builder = WeatherDTO.builder().region(region);
        
        // 기온
        String tempValue = extractValue(jsonResponse, "TMP");
        if (tempValue != null) {
            builder.temperature(tempValue + "°C");
        }
        
        // 최저기온
        String tmnValue = extractValue(jsonResponse, "TMN");
        if (tmnValue != null) {
            builder.minTemperature(tmnValue + "°C");
        }
        
        // 최고기온
        String tmxValue = extractValue(jsonResponse, "TMX");
        if (tmxValue != null) {
            builder.maxTemperature(tmxValue + "°C");
        }
        
        // 하늘상태
        String skyValue = extractValue(jsonResponse, "SKY");
        if (skyValue != null) {
            String skyCondition = getSkyCondition(skyValue);
            builder.skyCondition(skyCondition);
        }
        
        return builder.build();
    }
    
    private String extractValue(String jsonResponse, String category) {
        try {
            String pattern = "\"category\":\"" + category + "\",\"fcstValue\":\"";
            int startIndex = jsonResponse.indexOf(pattern);
            if (startIndex != -1) {
                startIndex += pattern.length();
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return jsonResponse.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            // 나중에 추가할 정
        }
        return null;
    }
    
    private String getBaseTime(LocalDateTime now) {
        int hour = now.getHour();
        int minute = now.getMinute();
        
        if (minute < 45) {
            hour = hour - 1;
        }
        
        if (hour < 0) {
            hour = 23;
        }
        
        return String.format("%02d00", hour);
    }
    
    private String getSkyCondition(String skyCode) {
        switch (skyCode) {
            case "1": return "맑음 ☀️";
            case "3": return "구름많음 ⛅️";
            case "4": return "흐림 ☁️";
            default: return "알 수 없음";
        }
    }
} 