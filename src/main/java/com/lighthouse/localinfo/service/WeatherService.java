package com.lighthouse.localinfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.localinfo.entity.Weather;
import com.lighthouse.localinfo.mapper.WeatherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    @Value("${DATA_GO_KR_API_KEY}")
    private String apiKey;

    private final String BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private final WeatherMapper weatherMapper;

    // 3시간마다 실행 (매일 02:00, 05:00, 08:00, 11:00, 14:00, 17:00, 20:00, 23:00)
    @Scheduled(cron = "0 0 2,5,8,11,14,17,20,23 * * ?")
    public void updateWeatherDataScheduled() {
        log.info("🕐 스케줄된 날씨 데이터 업데이트 시작");

        try {
            List<Weather> allRegions = weatherMapper.findAll();

            if (allRegions.isEmpty()) {
                log.warn("⚠️ 업데이트할 지역 데이터가 없습니다.");
                return;
            }

            log.info("총 {}개 지역의 날씨 데이터를 업데이트합니다.", allRegions.size());

            for (Weather region : allRegions) {
                try {
                    log.info("지역 {} ({}, {}) 날씨 데이터 업데이트 중...",
                            region.getRegion(), region.getGridX(), region.getGridY());

                    Weather updatedWeather = fetchCommon(region.getGridX(), region.getGridY(),
                            "스케줄된 업데이트 - " + region.getRegion());

                    if (updatedWeather != null) {
                        // 현재 온도는 항상 업데이트
                        region.setTemperature(updatedWeather.getTemperature());
                        region.setSkyCondition(updatedWeather.getSkyCondition());
                        region.setBaseDate(updatedWeather.getBaseDate());
                        region.setBaseTime(updatedWeather.getBaseTime());

                        // 최고/최저 온도는 비교 후 업데이트
                        updateMaxMinTemperature(region, updatedWeather);

                        weatherMapper.updateWeather(region);
                        log.info("✅ 지역 {} 날씨 데이터 업데이트 완료", region.getRegion());
                    } else {
                        log.warn("⚠️ 지역 {} 날씨 데이터 업데이트 실패 - null 반환", region.getRegion());
                    }

                    // API 호출 간격 조절 (서버 부하 방지)
                    Thread.sleep(1000);

                } catch (Exception e) {
                    log.error("❌ 지역 {} 날씨 데이터 업데이트 실패: {}", region.getRegion(), e.getMessage());
                }
            }

            log.info(" 모든 지역 날씨 데이터 스케줄 업데이트 완료");

        } catch (Exception e) {
            log.error("❌ 스케줄된 날씨 데이터 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 최고/최저 온도를 비교하여 업데이트하는 메서드
     */
    private void updateMaxMinTemperature(Weather existingWeather, Weather newWeather) {
        // 최고 온도 업데이트 (더 높은 값으로)
        if (newWeather.getMaxTemperature() != null) {
            if (existingWeather.getMaxTemperature() == null ||
                    newWeather.getMaxTemperature() > existingWeather.getMaxTemperature()) {
                existingWeather.setMaxTemperature(newWeather.getMaxTemperature());
                log.debug("최고 온도 업데이트: {}°C", newWeather.getMaxTemperature());
            }
        }

        // 최저 온도 업데이트 (더 낮은 값으로)
        if (newWeather.getMinTemperature() != null) {
            if (existingWeather.getMinTemperature() == null ||
                    newWeather.getMinTemperature() < existingWeather.getMinTemperature()) {
                existingWeather.setMinTemperature(newWeather.getMinTemperature());
                log.debug("최저 온도 업데이트: {}°C", newWeather.getMinTemperature());
            }
        }
    }

    public Weather getWeatherFromKMA(int gridX, int gridY) {
        return fetchCommon(gridX, gridY, "날씨 정보");
    }

    private Weather fetchCommon(int gridX, int gridY, String logPrefix) {
        try {
            // 현재 시간 기준으로 base_date와 base_time 계산
            String baseTime = getBaseTimeForShortTermForecast();
            String baseDate = getBaseDateForShortTermForecast(baseTime);

            // 기상청 샘플 코드 방식으로 URL 구성
            StringBuilder urlBuilder = new StringBuilder(BASE_URL);
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + apiKey);
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(String.valueOf(gridX), "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(String.valueOf(gridY), "UTF-8"));

            String url = urlBuilder.toString();
            log.info("{} API 요청 URL: {}", logPrefix, url);

            // HttpURLConnection으로 API 호출 (기상청 샘플 방식)
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            log.info("Response code: {}", conn.getResponseCode());

            // 응답 읽기
            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

            String response = sb.toString();
            log.debug("API 응답: {}", response);

            Weather weather = parseWeatherResponse(response);

            if (weather != null) {
                weather.setBaseDate(baseDate);
                weather.setBaseTime(baseTime);
            }

            return weather;

        } catch (Exception e) {
            log.error("❌ {} 데이터 처리 중 예기치 않은 오류 발생: {}", logPrefix, e.getMessage(), e);
            return null; // 목데이터 대신 null 반환
        }
    }

    private Weather parseWeatherResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            // 응답 헤더 확인
            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();
            String resultMsg = header.path("resultMsg").asText();

            if (!"00".equals(resultCode)) {
                log.warn("❌ 날씨 API 응답 오류 - 코드: {}, 메시지: {}", resultCode, resultMsg);
                return null; // 목데이터 대신 null 반환
            }

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (!itemsNode.isArray() || itemsNode.size() == 0) {
                log.warn("날씨 API 응답에 아이템이 없거나 형식이 올바르지 않습니다.");
                return null; // 목데이터 대신 null 반환
            }

            Weather weather = new Weather();
            Integer latestTmp = null;
            String latestSky = null;
            String latestFcstTime = null;

            // 각 아이템을 순회하며 날씨 정보 추출
            for (JsonNode item : itemsNode) {
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                String fcstTime = item.path("fcstTime").asText();

                switch (category) {
                    case "TMP": // 1시간 기온
                        if (latestFcstTime == null || fcstTime.compareTo(latestFcstTime) >= 0) {
                            latestFcstTime = fcstTime;
                            try {
                                latestTmp = Double.valueOf(fcstValue).intValue();
                            } catch (NumberFormatException e) {
                                log.warn("TMP 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                    case "SKY": // 하늘 상태
                        if (latestFcstTime == null || fcstTime.compareTo(latestFcstTime) >= 0) {
                            latestFcstTime = fcstTime;
                            try {
                                latestSky = mapSkyCodeToCondition(Integer.parseInt(fcstValue));
                            } catch (NumberFormatException e) {
                                log.warn("SKY 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                    case "TMX": // 일 최고 기온
                        if (weather.getMaxTemperature() == null) {
                            try {
                                weather.setMaxTemperature(Double.valueOf(fcstValue).intValue());
                            } catch (NumberFormatException e) {
                                log.warn("TMX 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                    case "TMN": // 일 최저 기온
                        if (weather.getMinTemperature() == null) {
                            try {
                                weather.setMinTemperature(Double.valueOf(fcstValue).intValue());
                            } catch (NumberFormatException e) {
                                log.warn("TMN 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                }
            }

            // 파싱된 값들을 Weather 엔티티에 설정
            if (latestTmp != null) weather.setTemperature(latestTmp);
            if (latestSky != null) weather.setSkyCondition(latestSky);

            // 필수 값이 없으면 null 반환 (프론트엔드에서 처리)
            if (weather.getTemperature() == null || weather.getMaxTemperature() == null ||
                    weather.getMinTemperature() == null || weather.getSkyCondition() == null) {
                log.warn("필수 날씨 데이터가 누락되어 null 반환");
                return null;
            }

            log.info("날씨 데이터 파싱 완료: {}", weather);
            return weather;

        } catch (JsonProcessingException e) {
            log.error("❌ 날씨 정보 JSON 파싱 실패: {}", e.getMessage(), e);
            return null; // 목데이터 대신 null 반환
        } catch (Exception e) {
            log.error("❌ 날씨 정보 파싱 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            return null; // 목데이터 대신 null 반환
        }
    }

    private String mapSkyCodeToCondition(int skyCode) {
        switch (skyCode) {
            case 1: return "맑음";
            case 3: return "구름많음";
            case 4: return "흐림";
            default: return "알 수 없음";
        }
    }

    private String getBaseTimeForShortTermForecast() {
        LocalTime now = LocalTime.now();

        // KMA API 데이터 제공 시간: 02:00, 05:00, 08:00, 11:00, 14:00, 17:00, 20:00, 23:00
        if (now.isBefore(LocalTime.of(2, 10))) {
            return "2300"; // 전날 23:00
        } else if (now.isBefore(LocalTime.of(5, 10))) {
            return "0200";
        } else if (now.isBefore(LocalTime.of(8, 10))) {
            return "0500";
        } else if (now.isBefore(LocalTime.of(11, 10))) {
            return "0800";
        } else if (now.isBefore(LocalTime.of(14, 10))) {
            return "1100";
        } else if (now.isBefore(LocalTime.of(17, 10))) {
            return "1400";
        } else if (now.isBefore(LocalTime.of(20, 10))) {
            return "1700";
        } else if (now.isBefore(LocalTime.of(23, 10))) {
            return "2000";
        } else {
            return "2300";
        }
    }

    private String getBaseDateForShortTermForecast(String baseTime) {
        LocalDate today = LocalDate.now();

        // 23:00인 경우 전날 데이터 사용
        if ("2300".equals(baseTime)) {
            return today.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else {
            return today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
    }
}