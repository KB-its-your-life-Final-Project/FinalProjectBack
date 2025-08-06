package com.lighthouse.localinfo.service;

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
    private final WeatherMapper weatherMapper; // WeatherMapper 직접 주입

    /**
     * [수정됨] 기상청 API를 호출하여 날씨 정보만 가져오는 단일 책임 메소드
     * DB 관련 로직은 모두 제거되었습니다.
     */
    public Weather fetchWeatherFromApi(int gridX, int gridY) {
        HttpURLConnection conn = null;
        try {
            String baseTime = getBaseTimeForShortTermForecast();
            String baseDate = getBaseDateForShortTermForecast(baseTime);

            StringBuilder urlBuilder = new StringBuilder(BASE_URL);
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + apiKey);
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("500", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(gridX), "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(gridY), "UTF-8"));

            URL apiUrl = new URL(urlBuilder.toString());
            conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader rd = (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) ?
                    new BufferedReader(new InputStreamReader(conn.getInputStream())) :
                    new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

            String response = sb.toString();
            log.info("[API 요청] 기상청 API 원본 응답: {}", response);

            Weather weather = parseWeatherResponse(response);

            if (weather != null) {
                weather.setBaseDate(baseDate);
                weather.setBaseTime(baseTime);
            }
            return weather;

        } catch (Exception e) {
            log.error("[API 요청] 데이터 처리 중 예기치 않은 오류 발생", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Scheduled(cron = "0 19 5/3 * * ?")
    public void scheduleSeoulWeatherUpdate() {
        log.info("서울 지역 날씨 업데이트를 위한 스케줄링 작업 시작 (ID: 254~278)");

        // ID 254~278 범위의 서울 지역 데이터 조회
        List<Weather> seoulWeatherList = weatherMapper.findByIdRange(254L, 278L);

        if (seoulWeatherList == null || seoulWeatherList.isEmpty()) {
            log.warn("ID 254~278 범위의 날씨 데이터가 없습니다.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Weather existingWeather : seoulWeatherList) {
            try {
                // 각 지역의 격자 좌표로 API 호출
                Weather newWeather = fetchWeatherFromApi(existingWeather.getGridX(), existingWeather.getGridY());

                if (newWeather != null) {
                    updateWeatherData(existingWeather, newWeather);
                    successCount++;
                } else {
                    log.warn("ID: {} 지역 날씨 API 호출 실패", existingWeather.getId());
                    failCount++;
                }

                // API 호출 간격 조절 (서버 부하 방지)
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("ID: {} 지역 날씨 업데이트 중 오류 발생", existingWeather.getId(), e);
                failCount++;
            }
        }

        log.info("서울 지역 날씨 업데이트 완료 - 성공: {}개, 실패: {}개", successCount, failCount);
    }

    /**
     * 기존 날씨 데이터와 새로운 API 데이터를 비교하여 업데이트
     */
    private void updateWeatherData(Weather existingWeather, Weather newWeather) {
        boolean minTempUpdated = false;
        boolean maxTempUpdated = false;

        // 최저온도가 더 낮아졌으면 업데이트
        if (newWeather.getMinTemperature() != null &&
                (existingWeather.getMinTemperature() == null ||
                        newWeather.getMinTemperature() < existingWeather.getMinTemperature())) {
            Integer oldMinTemp = existingWeather.getMinTemperature();
            existingWeather.setMinTemperature(newWeather.getMinTemperature());
            minTempUpdated = true;
            log.info("ID: {} 최저온도 업데이트: {}°C -> {}°C",
                    existingWeather.getId(), oldMinTemp, newWeather.getMinTemperature());
        }

        // 최고온도가 더 높아졌으면 업데이트
        if (newWeather.getMaxTemperature() != null &&
                (existingWeather.getMaxTemperature() == null ||
                        newWeather.getMaxTemperature() > existingWeather.getMaxTemperature())) {
            Integer oldMaxTemp = existingWeather.getMaxTemperature();
            existingWeather.setMaxTemperature(newWeather.getMaxTemperature());
            maxTempUpdated = true;
            log.info("ID: {} 최고온도 업데이트: {}°C -> {}°C",
                    existingWeather.getId(), oldMaxTemp, newWeather.getMaxTemperature());
        }

        // 현재 온도는 항상 업데이트
        existingWeather.setTemperature(newWeather.getTemperature());
        existingWeather.setSkyCondition(newWeather.getSkyCondition());
        existingWeather.setBaseDate(newWeather.getBaseDate());
        existingWeather.setBaseTime(newWeather.getBaseTime());

        // DB 업데이트
        weatherMapper.updateWeather(existingWeather);

        if (minTempUpdated || maxTempUpdated) {
            log.info("ID: {} 날씨 정보 업데이트 완료 (최저/최고온도 변경): 현재{}°C",
                    existingWeather.getId(), existingWeather.getTemperature());
        } else {
            log.info("ID: {} 날씨 정보 업데이트 완료 (현재온도만 변경): {}°C",
                    existingWeather.getId(), existingWeather.getTemperature());
        }
    }

    private Weather parseWeatherResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode header = root.path("response").path("header");
            if (!"00".equals(header.path("resultCode").asText())) {
                log.warn("날씨 API 응답 오류 - 코드: {}, 메시지: {}", header.path("resultCode").asText(), header.path("resultMsg").asText());
                return null;
            }

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (!itemsNode.isArray() || itemsNode.size() == 0) {
                log.warn("날씨 API 응답에 아이템이 없습니다.");
                return null;
            }

            Weather weather = new Weather();
            String latestFcstTime = itemsNode.get(0).path("fcstTime").asText();

            for (JsonNode item : itemsNode) {
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                String fcstTime = item.path("fcstTime").asText();

                if (fcstTime.equals(latestFcstTime)) {
                    if ("TMP".equals(category)) {
                        weather.setTemperature(Double.valueOf(fcstValue).intValue());
                    } else if ("SKY".equals(category)) {
                        weather.setSkyCondition(mapSkyCodeToCondition(Integer.parseInt(fcstValue)));
                    }
                }
                if ("TMX".equals(category)) {
                    weather.setMaxTemperature(Double.valueOf(fcstValue).intValue());
                } else if ("TMN".equals(category)) {
                    weather.setMinTemperature(Double.valueOf(fcstValue).intValue());
                }
            }

            if (weather.getTemperature() == null || weather.getSkyCondition() == null) {
                log.warn("필수 날씨 데이터(기온, 하늘상태)가 누락되어 null 반환");
                return null;
            }
            return weather;

        } catch (Exception e) {
            log.error("날씨 정보 파싱 중 오류 발생", e);
            return null;
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
        if (now.isBefore(LocalTime.of(2, 10))) return "2300";
        if (now.isBefore(LocalTime.of(5, 10))) return "0200";
        if (now.isBefore(LocalTime.of(8, 10))) return "0500";
        if (now.isBefore(LocalTime.of(11, 10))) return "0800";
        if (now.isBefore(LocalTime.of(14, 10))) return "1100";
        if (now.isBefore(LocalTime.of(17, 10))) return "1400";
        if (now.isBefore(LocalTime.of(20, 10))) return "1700";
        if (now.isBefore(LocalTime.of(23, 10))) return "2000";
        return "2300";
    }

    private String getBaseDateForShortTermForecast(String baseTime) {
        LocalDate today = LocalDate.now();
        if ("2300".equals(baseTime) && LocalTime.now().isBefore(LocalTime.of(2, 10))) {
            return today.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}